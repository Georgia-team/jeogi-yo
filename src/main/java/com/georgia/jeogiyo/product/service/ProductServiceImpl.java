package com.georgia.jeogiyo.product.service;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.repository.AiHistoryRepository;
import com.georgia.jeogiyo.ai.service.AiGeminiService;
import com.georgia.jeogiyo.ai.service.AiHistoryRecorder;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.util.PageUtil;
import com.georgia.jeogiyo.product.dto.request.ProductCreateRequest;
import com.georgia.jeogiyo.product.dto.request.ProductUpdateRequest;
import com.georgia.jeogiyo.product.dto.response.ProductResponse;
import com.georgia.jeogiyo.product.dto.response.ProductSearchResponse;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    // loginId는 Controller에서 JWT 인증 객체(Authentication)로부터 전달받는다.

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final UserFinder userFinder;
    private final AiGeminiService aiGeminiService;
    private final AiHistoryRepository aiHistoryRepository;
    private final AiHistoryRecorder aiHistoryRecorder;
    private final EntityManager entityManager;

    @Override
    public ProductResponse createProduct(UUID storeId, String loginId, ProductCreateRequest request) {

        User user = userFinder.getOwnerUserByLoginId(loginId);

        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND_STORE));

        if (!store.getOwner().getUserId().equals(user.getUserId())) {
            throw new BusinessException(GlobalErrorCode.FORBIDDEN_PRODUCT);
        }

        Category category = categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND_CATEGORY));

        if (!Boolean.TRUE.equals(request.getUseAiDescription())
                && (request.getDescription() == null || request.getDescription().isBlank())) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        String description = request.getDescription();
        String requestText = null;

        if (Boolean.TRUE.equals(request.getUseAiDescription())) {
            if (request.getAiPrompt() == null || request.getAiPrompt().isBlank()) {
                throw new BusinessException(GlobalErrorCode.INVALID_INPUT_VALUE);
            }

            requestText = request.getAiPrompt() + "\n답변을 최대한 간결하게 50자 이하로";

            try {
                description = aiGeminiService.generateDescription(requestText);
            } catch (BusinessException e) {
                aiHistoryRecorder.recordFail(
                        user,
                        null,
                        requestText,
                        "gemini-2.5-flash-lite",
                        e.getMessage()
                );

                log.error("AI product description generation failed. loginId={}, storeId={}", loginId, storeId, e);
                throw e;
            } catch (Exception e) {
                aiHistoryRecorder.recordFail(
                        user,
                        null,
                        requestText,
                        "gemini-2.5-flash-lite",
                        e.getMessage()
                );

                log.error("AI product description generation failed. loginId={}, storeId={}", loginId, storeId, e);
                throw new BusinessException(GlobalErrorCode.AI_GENERATION_FAILED);
            }
        }

        Product product = new Product(
                store,
                category,
                request.getProductName(),
                description,
                request.getPrice(),
                request.getStock(),
                request.getIsHidden()
        );

        Product savedProduct = productRepository.save(product);

        log.info("Product created. productId={}, storeId={}, useAiDescription={}",
                savedProduct.getProductId(), storeId, request.getUseAiDescription());

        if (Boolean.TRUE.equals(request.getUseAiDescription())) {
            AiHistory aiHistory = AiHistory.success(
                    user,
                    savedProduct,
                    requestText,
                    description,
                    "gemini-2.5-flash-lite"
            );

            aiHistoryRepository.save(aiHistory);
        }

        return toResponse(savedProduct);
    }
    private void validateOwnerOrMaster(User user, Product product) {
        boolean isMaster = user.getRole() == Role.MASTER;
        boolean isOwnerOfStore = user.getRole() == Role.OWNER
                && product.getStore().getOwner().getUserId().equals(user.getUserId());

        if (!isMaster && !isOwnerOfStore) {
            throw new BusinessException(GlobalErrorCode.FORBIDDEN_PRODUCT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId, String loginId) {

        User user = userFinder.getUserByLoginId(loginId);
        Product product = findProduct(productId);
        validateReadableProduct(user, product);

        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSearchResponse> searchProducts(
            UUID storeId,
            UUID categoryId,
            String keyword,
            int page,
            int size,
            String sort,
            String loginId
    ) {
        User user = userFinder.getUserByLoginId(loginId);

        storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND_STORE));

        if (categoryId != null) {
            categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId)
                    .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND_CATEGORY));
        }

        Pageable pageable = PageUtil.toPageable(page, size, sort);

        Page<Product> productPage = productRepository.searchProducts(
                storeId,
                categoryId,
                keyword,
                user.getRole(),
                user.getUserId(),
                pageable
        );

        return PageResponse.from(productPage, this::toSearchResponse);
    }

    @Override
    public ProductResponse updateProduct(UUID productId, String loginId, ProductUpdateRequest request) {
        User user = userFinder.getUserByLoginId(loginId);
        Product product = findProduct(productId);
        validateOwnerOrMaster(user, product);

        Category category = request.getCategoryId() != null
                ? categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND_CATEGORY))
                : null;

        product.update(
                category,
                request.getProductName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock(),
                request.getIsHidden()
        );
        // 변경 감지 flush 확인
        entityManager.flush();

        log.info("Product updated. productId={}", product.getProductId());

        return toResponse(product);
    }

    @Override
    public void deleteProduct(UUID productId, String loginId) {
        User user = userFinder.getUserByLoginId(loginId);
        Product product = findProduct(productId);

        validateOwnerOrMaster(user, product);

        product.softDelete(loginId);

        log.info("Product soft deleted. productId={}, deletedBy={}", product.getProductId(), loginId);
    }

    private Product findProduct(UUID productId) {

        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND_PRODUCT));

        // 상품 자체가 isDeleted=false여도, 부모 가게가 isDeleted=true면 상품 수정/삭제/AI 생성 방지
        if (product.getStore().isDeleted()) {
            throw new BusinessException(GlobalErrorCode.ALREADY_DELETED_STORE);
        }

        return product;
    }

    private void validateReadableProduct(User user, Product product) {
        if (!Boolean.TRUE.equals(product.getIsHidden())) {
            return;
        }

        boolean isMaster = user.getRole() == Role.MASTER;
        boolean isOwnerOfStore = user.getRole() == Role.OWNER
                && product.getStore().getOwner().getUserId().equals(user.getUserId());

        if (!isMaster && !isOwnerOfStore) {
            throw new BusinessException(GlobalErrorCode.HIDDEN_PRODUCT_NOT_READABLE);
        }
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .storeId(product.getStore().getStoreId())
                .categoryId(product.getCategory().getCategoryId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .isHidden(product.getIsHidden())
                .build();
    }

    private ProductSearchResponse toSearchResponse(Product product) {
        return ProductSearchResponse.builder()
                .productId(product.getProductId())
                .storeId(product.getStore().getStoreId())
                .categoryId(product.getCategory().getCategoryId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .stock(product.getStock())
                .isHidden(product.getIsHidden())
                .build();
    }


}
