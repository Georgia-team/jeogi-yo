package com.georgia.jeogiyo.product.service;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.repository.AiHistoryRepository;
import com.georgia.jeogiyo.ai.service.AiGeminiService;
import com.georgia.jeogiyo.ai.service.AiHistoryRecorder;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.product.dto.request.ProductCreateRequest;
import com.georgia.jeogiyo.product.dto.request.ProductUpdateRequest;
import com.georgia.jeogiyo.product.dto.response.ProductResponse;
import com.georgia.jeogiyo.product.dto.response.ProductSearchResponse;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.support.DomainTestFixture;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.georgia.jeogiyo.support.DomainTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * ProductServiceImpl 단위 테스트입니다.
 *
 * - 상품 도메인의 핵심 규칙을 DB 없이 검증합니다.
 * - 등록/수정/삭제 권한, AI 사용/미사용 등록 조건, 숨김 상품 조회 권한,
 *   삭제된 상품과 삭제된 가게의 상품 차단이 주요 검증 대상입니다.
 * - 공통 예외 처리 적용에 따라 BusinessException과 GlobalErrorCode 기준으로 검증합니다.
 * - AI 설명 생성 포함 상품 등록은 실제 Gemini를 호출하지 않고 AiGeminiService를 mock 처리합니다.
 * - Service 단위 테스트에서는 loginId를 직접 전달해 비즈니스 로직을 검증하고,
 *   인증 사용자 추출 흐름은 Controller/통합 테스트에서 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private AiGeminiService aiGeminiService;

    @Mock
    private AiHistoryRepository aiHistoryRepository;

    @Mock
    private AiHistoryRecorder aiHistoryRecorder;

    @Mock
    private EntityManager entityManager;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(
                productRepository,
                storeRepository,
                categoryRepository,
                userFinder,
                aiGeminiService,
                aiHistoryRepository,
                aiHistoryRecorder,
                entityManager
        );
    }

    @Test
    @DisplayName("OWNER는 본인 가게에 AI 미사용 상품을 등록할 수 있다")
    void createProduct_withoutAi_owner_success() {
        // given: OWNER 본인 소유 가게와 카테고리가 있고, 상품 설명을 직접 입력한다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        ProductCreateRequest request = DomainTestFixture.productCreateRequest(CATEGORY_ID, false);

        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(storeRepository.findByStoreIdAndIsDeletedFalse(STORE_ID)).willReturn(Optional.of(store));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(CATEGORY_ID)).willReturn(Optional.of(category));
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            DomainTestFixture.markPersisted(product, PRODUCT_ID);
            return product;
        });

        // when: 상품을 등록한다.
        ProductResponse response = productService.createProduct(STORE_ID, OWNER_LOGIN_ID, request);

        // then: Product가 저장되고 AI 호출/이력 저장은 발생하지 않는다.
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getStoreId()).isEqualTo(STORE_ID);
        assertThat(response.getDescription()).isEqualTo("직접 입력한 상품 설명");
        assertThat(response.getIsHidden()).isFalse();
        verifyNoInteractions(aiGeminiService, aiHistoryRepository, aiHistoryRecorder);
    }

    @Test
    @DisplayName("OWNER는 AI 설명을 사용해 상품을 등록할 수 있다")
    void createProduct_withAi_owner_success() {
        // given: AI 프롬프트가 있고 Gemini mock이 상품 설명을 반환한다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        ProductCreateRequest request = DomainTestFixture.productCreateRequest(CATEGORY_ID, true);
        String aiDescription = "AI가 생성한 테스트 상품 설명입니다.";

        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(storeRepository.findByStoreIdAndIsDeletedFalse(STORE_ID)).willReturn(Optional.of(store));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(CATEGORY_ID)).willReturn(Optional.of(category));
        given(aiGeminiService.generateDescription(anyString())).willReturn(aiDescription);
        given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            DomainTestFixture.markPersisted(product, PRODUCT_ID);
            return product;
        });
        given(aiHistoryRepository.save(any(AiHistory.class))).willAnswer(invocation -> {
            AiHistory aiHistory = invocation.getArgument(0);
            DomainTestFixture.markPersisted(aiHistory, DomainTestFixture.AI_HISTORY_ID);
            return aiHistory;
        });

        // when: AI 설명 사용 옵션으로 상품을 등록한다.
        ProductResponse response = productService.createProduct(STORE_ID, OWNER_LOGIN_ID, request);

        // then: 상품 설명은 AI 응답으로 저장되고 AI 성공 이력이 남는다.
        assertThat(response.getDescription()).isEqualTo(aiDescription);
        then(aiGeminiService).should().generateDescription(anyString());
        then(aiHistoryRepository).should().save(any(AiHistory.class));
    }

    @Test
    @DisplayName("CUSTOMER는 상품을 등록할 수 없다")
    void createProduct_customer_fail() {
        // given: CUSTOMER loginId로 OWNER 권한 조회를 시도하면 권한 예외가 발생한다.
        ProductCreateRequest request = DomainTestFixture.productCreateRequest(CATEGORY_ID, false);

        given(userFinder.getOwnerUserByLoginId(CUSTOMER_LOGIN_ID))
                .willThrow(new BusinessException(GlobalErrorCode.FORBIDDEN));

        // when & then: OWNER 권한 검증에서 실패하므로 가게/카테고리/상품 저장 로직까지 가지 않는다.
        assertThatThrownBy(() -> productService.createProduct(STORE_ID, CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.FORBIDDEN.getMessage());

        verifyNoInteractions(storeRepository, categoryRepository, productRepository,
                aiGeminiService, aiHistoryRepository, aiHistoryRecorder);
    }

    @Test
    @DisplayName("AI 미사용 등록에서는 description이 필수다")
    void createProduct_withoutDescriptionAndWithoutAi_fail() {
        // given: useAiDescription=false인데 description이 비어 있다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        ProductCreateRequest request = DomainTestFixture.productCreateRequest(CATEGORY_ID, false);
        ReflectionTestUtils.setField(request, "description", "");

        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(storeRepository.findByStoreIdAndIsDeletedFalse(STORE_ID)).willReturn(Optional.of(store));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(CATEGORY_ID)).willReturn(Optional.of(category));

        // when & then: 직접 설명이 없으면 상품 등록을 막는다.
        assertThatThrownBy(() -> productService.createProduct(STORE_ID, OWNER_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("AI 사용 등록에서는 aiPrompt가 필수다")
    void createProduct_withAiButNoPrompt_fail() {
        // given: useAiDescription=true인데 aiPrompt가 비어 있다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        ProductCreateRequest request = DomainTestFixture.productCreateRequest(CATEGORY_ID, true);
        ReflectionTestUtils.setField(request, "aiPrompt", "");

        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(storeRepository.findByStoreIdAndIsDeletedFalse(STORE_ID)).willReturn(Optional.of(store));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(CATEGORY_ID)).willReturn(Optional.of(category));

        // when & then: AI 프롬프트가 없으면 Gemini 호출 전에 실패한다.
        assertThatThrownBy(() -> productService.createProduct(STORE_ID, OWNER_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        verifyNoInteractions(aiGeminiService);
    }

    @Test
    @DisplayName("숨김 상품은 CUSTOMER가 조회할 수 없다")
    void getHiddenProduct_customer_fail() {
        // given: 숨김 상품과 CUSTOMER 사용자가 있다.
        User owner = DomainTestFixture.owner();
        User customer = DomainTestFixture.customer();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product hiddenProduct = DomainTestFixture.product(store, category, true);

        given(userFinder.getUserByLoginId(CUSTOMER_LOGIN_ID)).willReturn(customer);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(hiddenProduct));

        // when & then: CUSTOMER는 숨김 상품을 볼 수 없다.
        assertThatThrownBy(() -> productService.getProduct(PRODUCT_ID, CUSTOMER_LOGIN_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.FORBIDDEN_PRODUCT.getMessage());
    }

    @Test
    @DisplayName("숨김 상품은 MASTER가 조회할 수 있다")
    void getHiddenProduct_master_success() {
        // given: 숨김 상품과 MASTER 사용자가 있다.
        User owner = DomainTestFixture.owner();
        User master = DomainTestFixture.master();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product hiddenProduct = DomainTestFixture.product(store, category, true);

        given(userFinder.getUserByLoginId(MASTER_LOGIN_ID)).willReturn(master);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(hiddenProduct));

        // when: MASTER가 숨김 상품을 조회한다.
        ProductResponse response = productService.getProduct(PRODUCT_ID, MASTER_LOGIN_ID);

        // then: MASTER는 숨김 상품도 조회할 수 있다.
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getIsHidden()).isTrue();
    }

    @Test
    @DisplayName("OWNER는 본인 상품을 수정할 수 있다")
    void updateProduct_owner_success() {
        // given: OWNER 본인 가게의 상품과 수정 요청이 있다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product product = DomainTestFixture.product(store, category);
        ProductUpdateRequest request = DomainTestFixture.productUpdateRequest(CATEGORY_ID);

        given(userFinder.getUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(CATEGORY_ID)).willReturn(Optional.of(category));

        // when: OWNER가 상품 정보를 수정한다.
        ProductResponse response = productService.updateProduct(PRODUCT_ID, OWNER_LOGIN_ID, request);

        // then: 수정값이 반영되고 updatedAt 응답 반영을 위해 flush가 호출된다.
        assertThat(response.getProductName()).isEqualTo("테스트 상품 수정");
        assertThat(response.getDescription()).isEqualTo("수정된 상품 설명");
        assertThat(response.getPrice()).isEqualTo(15000);
        assertThat(response.getStock()).isEqualTo(50);
        then(entityManager).should().flush();
    }

    @Test
    @DisplayName("MASTER는 다른 OWNER의 상품도 수정할 수 있다")
    void updateProduct_master_success() {
        // given: MASTER와 OWNER 소유 상품이 있다.
        User owner = DomainTestFixture.owner();
        User master = DomainTestFixture.master();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product product = DomainTestFixture.product(store, category);
        ProductUpdateRequest request = DomainTestFixture.productUpdateRequest(null);

        given(userFinder.getUserByLoginId(MASTER_LOGIN_ID)).willReturn(master);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

        // when: MASTER가 상품을 수정한다.
        ProductResponse response = productService.updateProduct(PRODUCT_ID, MASTER_LOGIN_ID, request);

        // then: 소유자와 무관하게 수정 가능하다.
        assertThat(response.getProductName()).isEqualTo("테스트 상품 수정");
        then(entityManager).should().flush();
    }

    @Test
    @DisplayName("삭제된 가게의 상품은 수정할 수 없다")
    void updateProduct_deletedStoreProduct_fail() {
        // given: 상품 자체는 삭제되지 않았지만 부모 가게가 soft delete 되어 있다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        store.softDelete(OWNER_LOGIN_ID);
        Product product = DomainTestFixture.product(store, category);
        ProductUpdateRequest request = DomainTestFixture.productUpdateRequest(null);

        given(userFinder.getUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

        // when & then: 부모 가게가 삭제되었으면 상품 처리도 막는다.
        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, OWNER_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.ALREADY_DELETED_STORE.getMessage());

        verify(entityManager, never()).flush();
    }

    @Test
    @DisplayName("OWNER는 본인 상품을 soft delete 할 수 있다")
    void deleteProduct_owner_success() {
        // given: OWNER 본인 가게의 상품이 있다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product product = DomainTestFixture.product(store, category);

        given(userFinder.getUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

        // when: OWNER가 상품을 삭제한다.
        productService.deleteProduct(PRODUCT_ID, OWNER_LOGIN_ID);

        // then: 물리 삭제가 아니라 BaseEntity soft delete 값이 채워진다.
        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getDeletedAt()).isNotNull();
        assertThat(product.getDeletedBy()).isEqualTo(OWNER_LOGIN_ID);
    }

    @Test
    @DisplayName("상품 검색은 page 음수와 허용되지 않는 size를 보정한다")
    void searchProducts_pageAndSize_normalized() {
        // given: 상품 검색 repository가 빈 페이지를 반환한다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);

        given(userFinder.getUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(storeRepository.findByStoreIdAndIsDeletedFalse(STORE_ID)).willReturn(Optional.of(store));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(CATEGORY_ID)).willReturn(Optional.of(category));
        given(productRepository.searchProducts(
                eq(STORE_ID),
                eq(CATEGORY_ID),
                eq("테스트"),
                eq(Role.OWNER),
                eq(owner.getUserId()),
                any(Pageable.class)
        )).willReturn(new PageImpl<>(List.of()));

        // when: page=-1, size=20처럼 정책 밖의 요청이 들어온다.
        PageResponse<ProductSearchResponse> response = productService.searchProducts(
                STORE_ID,
                CATEGORY_ID,
                "테스트",
                -1,
                20,
                "desc",
                OWNER_LOGIN_ID
        );

        // then: page는 0, size는 10으로 보정되어 repository에 전달된다.
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(productRepository).should().searchProducts(
                eq(STORE_ID),
                eq(CATEGORY_ID),
                eq("테스트"),
                eq(Role.OWNER),
                eq(owner.getUserId()),
                pageableCaptor.capture()
        );

        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(response.getPage()).isZero();
    }
}
