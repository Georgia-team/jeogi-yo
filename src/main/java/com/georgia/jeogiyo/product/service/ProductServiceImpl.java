package com.georgia.jeogiyo.product.service;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.repository.AiHistoryRepository;
import com.georgia.jeogiyo.ai.service.AiGeminiService;
import com.georgia.jeogiyo.ai.service.AiHistoryRecorder;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import com.georgia.jeogiyo.product.dto.request.ProductCreateRequest;
import com.georgia.jeogiyo.product.dto.request.ProductUpdateRequest;
import com.georgia.jeogiyo.product.dto.response.ProductResponse;
import com.georgia.jeogiyo.product.dto.response.ProductSearchPageResponse;
import com.georgia.jeogiyo.product.dto.response.ProductSearchResponse;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.service.UserFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    // TODO JWT 적용 후 OWNER/MASTER 권한 검증
    // TODO OWNER는 본인이 소유한 가게의 상품만 등록/수정/삭제 가능하도록 검증

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final UserFinder userFinder;
    private final AiGeminiService aiGeminiService;
    private final AiHistoryRepository aiHistoryRepository;
    private final AiHistoryRecorder aiHistoryRecorder;

    @Override
    public ProductResponse createProduct(UUID storeId, String loginId, ProductCreateRequest request) { // TODO JWT

        User user = userFinder.getUserByLoginId(loginId);

        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        if (user.getRole() != Role.OWNER || !store.getOwner().getLoginId().equals(loginId)) {
            throw new IllegalArgumentException("본인 가게의 상품만 등록할 수 있습니다.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        if (!Boolean.TRUE.equals(request.getUseAiDescription())
                && (request.getDescription() == null || request.getDescription().isBlank())) {
            throw new IllegalArgumentException("AI 설명 생성을 사용하지 않으면 상품 설명은 필수입니다.");
        }

        String description = request.getDescription();
        String requestText = null;

        if (Boolean.TRUE.equals(request.getUseAiDescription())) {
            if (request.getAiPrompt() == null || request.getAiPrompt().isBlank()) {
                throw new IllegalArgumentException("AI 프롬프트는 필수입니다.");
            }

            // TODO GeminiService 호출
            // TODO AiHistory 저장
            requestText = request.getAiPrompt() + "\n답변을 최대한 간결하게 50자 이하로";

            try {
                description = aiGeminiService.generateDescription(requestText);
            } catch (Exception e) {
                aiHistoryRecorder.recordFail(
                        user,
                        null,
                        requestText,
                        "gemini-2.5-flash-lite",
                        e.getMessage()
                );

                throw new IllegalStateException("AI 상품 설명 생성에 실패했습니다.", e);
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
                && product.getStore().getOwner().getLoginId().equals(user.getLoginId());

        if (!isMaster && !isOwnerOfStore) {
            throw new IllegalArgumentException("본인 가게의 상품만 처리할 수 있습니다.");
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
    public ProductSearchPageResponse searchProducts(
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
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        }

        Pageable pageable = createPageable(page, size, sort);

        Page<Product> productPage = productRepository.searchProducts(
                storeId,
                categoryId,
                keyword,
                user.getRole(),
                user.getLoginId(),
                pageable
        );

        return ProductSearchPageResponse.builder()
                .content(productPage.getContent().stream()
                        .map(this::toSearchResponse)
                        .toList())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();
    }

    @Override
    public ProductResponse updateProduct(UUID productId, String loginId, ProductUpdateRequest request) {
        User user = userFinder.getUserByLoginId(loginId);
        Product product = findProduct(productId);
        validateOwnerOrMaster(user, product);

        Category category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."))
                : null;

        product.update(
                category,
                request.getProductName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock(),
                request.getIsHidden()
        );

        return toResponse(product);
    }

    @Override
    public void deleteProduct(UUID productId, String loginId) {
        User user = userFinder.getUserByLoginId(loginId);
        Product product = findProduct(productId);

        validateOwnerOrMaster(user, product);

        product.softDelete(loginId);
    }

    private Product findProduct(UUID productId) {
        return productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    private void validateReadableProduct(User user, Product product) {
        if (!Boolean.TRUE.equals(product.getIsHidden())) {
            return;
        }

        boolean isMaster = user.getRole() == Role.MASTER;
        boolean isOwnerOfStore = user.getRole() == Role.OWNER
                && product.getStore().getOwner().getLoginId().equals(user.getLoginId());

        if (!isMaster && !isOwnerOfStore) {
            throw new IllegalArgumentException("숨김 처리된 상품은 조회할 수 없습니다.");
        }
    }

    private Pageable createPageable(int page, int size, String sort) {
        int validatedSize = (size == 10 || size == 30 || size == 50) ? size : 10;
        Sort.Direction direction = "asc".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                Math.max(page, 0),
                validatedSize,
                Sort.by(direction, "createdAt")
        );
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
                .createdAt(product.getCreatedAt())
                .isDeleted(product.isDeleted())
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
                .createdAt(product.getCreatedAt())
                .build();
    }


}
