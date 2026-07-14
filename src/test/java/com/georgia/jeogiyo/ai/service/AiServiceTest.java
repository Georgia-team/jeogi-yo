package com.georgia.jeogiyo.ai.service;

import com.georgia.jeogiyo.ai.dto.request.AiDescriptionRequest;
import com.georgia.jeogiyo.ai.dto.response.AiDescriptionResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistoryResponse;
import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.entity.AiStatus;
import com.georgia.jeogiyo.ai.repository.AiHistoryRepository;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.support.DomainTestFixture;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.georgia.jeogiyo.support.DomainTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * AiServiceImpl 단위 테스트입니다.
 *
 * - 실제 Gemini API를 호출하지 않고 AiGeminiService를 mock 처리합니다.
 * - API 비용/쿼터와 네트워크 상태에 영향받지 않고 AI 설명 생성 흐름을 검증합니다.
 * - AI 성공/실패 이력 저장, 상품 description 반영, 권한 검증, soft delete 차단을 검증합니다.
 * - MASTER의 AI 이력 상세 조회/검색 권한과 페이지 요청 보정도 검증합니다.
 * - 공통 예외 처리 적용에 따라 BusinessException과 GlobalErrorCode 기준으로 검증합니다.
 *
 * TODO Gemini 모델명 또는 응답 파싱 정책이 변경되면 AiGeminiServiceImpl 테스트를 함께 보완해야 합니다.
 */
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiGeminiService aiGeminiService;

    @Mock
    private AiHistoryRepository aiHistoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserFinder userFinder;

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(
                aiGeminiService,
                aiHistoryRepository,
                productRepository,
                userFinder
        );
    }

    @Test
    @DisplayName("OWNER는 AI 상품 설명을 생성하고 SUCCESS 이력을 저장할 수 있다")
    void createAiDescription_owner_success() {
        // given: OWNER 소유 상품과 Gemini mock 응답이 있다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product product = DomainTestFixture.product(store, category);
        AiDescriptionRequest request = DomainTestFixture.aiDescriptionRequest("김치찌개 설명을 작성해줘");
        String responseText = "AI가 생성한 김치찌개 설명입니다.";

        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));
        given(aiGeminiService.generateDescription(anyString())).willReturn(responseText);
        given(aiHistoryRepository.save(any(AiHistory.class))).willAnswer(invocation -> {
            AiHistory aiHistory = invocation.getArgument(0);
            DomainTestFixture.markPersisted(aiHistory, DomainTestFixture.AI_HISTORY_ID);
            return aiHistory;
        });

        // when: OWNER가 AI 설명 생성을 요청한다.
        AiDescriptionResponse response = aiService.createAiDescription(PRODUCT_ID, OWNER_LOGIN_ID, request);

        // then: 상품 설명이 AI 응답으로 바뀌고 SUCCESS 이력이 응답된다.
        assertThat(response.getAiHistoryId()).isEqualTo(DomainTestFixture.AI_HISTORY_ID);
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getResponseText()).isEqualTo(responseText);
        assertThat(response.getAiStatus()).isEqualTo(AiStatus.SUCCESS);
        assertThat(response.getErrorMessage()).isNull();
        assertThat(product.getDescription()).isEqualTo(responseText);
    }

    @Test
    @DisplayName("Gemini 호출 실패 시 FAIL 이력을 저장하고 실패 응답을 반환한다")
    void createAiDescription_geminiFail_saveFailHistory() {
        // given: Gemini mock이 예외를 던진다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product product = DomainTestFixture.product(store, category);
        AiDescriptionRequest request = DomainTestFixture.aiDescriptionRequest("상품 설명을 작성해줘");

        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));
        given(aiGeminiService.generateDescription(anyString())).willThrow(new RuntimeException("Gemini API 오류"));
        given(aiHistoryRepository.save(any(AiHistory.class))).willAnswer(invocation -> {
            AiHistory aiHistory = invocation.getArgument(0);
            DomainTestFixture.markPersisted(aiHistory, DomainTestFixture.AI_HISTORY_ID);
            return aiHistory;
        });

        // when: AI 설명 생성을 요청한다.
        AiDescriptionResponse response = aiService.createAiDescription(PRODUCT_ID, OWNER_LOGIN_ID, request);

        // then: 예외를 밖으로 던지지 않고 FAIL 이력 응답을 반환한다.
        assertThat(response.getAiStatus()).isEqualTo(AiStatus.FAIL);
        assertThat(response.getResponseText()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo("Gemini API 오류");
    }

    @Test
    @DisplayName("OWNER가 아니면 AI 상품 설명을 생성할 수 없다")
    void createAiDescription_customer_fail() {
        // given: CUSTOMER loginId로 OWNER 권한 조회를 시도하면 권한 예외가 발생한다.
        AiDescriptionRequest request =
                DomainTestFixture.aiDescriptionRequest("상품 설명을 작성해줘");

        given(userFinder.getOwnerUserByLoginId(CUSTOMER_LOGIN_ID))
                .willThrow(new BusinessException(GlobalErrorCode.FORBIDDEN));

        // when & then: OWNER 권한 검증에서 실패하므로 상품 조회, Gemini 호출, AI 이력 저장까지 진행되지 않는다.
        assertThatThrownBy(() -> aiService.createAiDescription(PRODUCT_ID, CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.FORBIDDEN.getMessage());

        verifyNoInteractions(productRepository, aiGeminiService, aiHistoryRepository);
    }

    @Test
    @DisplayName("삭제된 상품은 AI 설명 생성 대상이 될 수 없다")
    void createAiDescription_deletedProduct_fail() {
        // given: repository 메서드가 isDeleted=false 조건으로 조회하므로 삭제된 상품은 Optional.empty로 표현한다.
        User owner = DomainTestFixture.owner();
        AiDescriptionRequest request = DomainTestFixture.aiDescriptionRequest("상품 설명을 작성해줘");

        //given(userFinder.getUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.empty());

        // when & then: 삭제된 상품은 없는 상품처럼 처리한다.
        assertThatThrownBy(() -> aiService.createAiDescription(PRODUCT_ID, OWNER_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.NOT_FOUND_PRODUCT.getMessage());

        verifyNoInteractions(aiGeminiService, aiHistoryRepository);
    }

    @Test
    @DisplayName("삭제된 가게의 상품은 AI 설명 생성 대상이 될 수 없다")
    void createAiDescription_deletedStoreProduct_fail() {
        // given: 상품은 active지만 부모 가게가 soft delete 되어 있다.
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        store.softDelete(OWNER_LOGIN_ID);
        Product product = DomainTestFixture.product(store, category);
        AiDescriptionRequest request = DomainTestFixture.aiDescriptionRequest("상품 설명을 작성해줘");

        //given(userFinder.getUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(userFinder.getOwnerUserByLoginId(OWNER_LOGIN_ID)).willReturn(owner);
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

        // when & then: 부모 가게가 삭제되었으면 AI 설명 생성도 막는다.
        assertThatThrownBy(() -> aiService.createAiDescription(PRODUCT_ID, OWNER_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.ALREADY_DELETED_STORE.getMessage());

        verifyNoInteractions(aiGeminiService, aiHistoryRepository);
    }

    @Test
    @DisplayName("AI 이력 검색은 page 음수와 허용되지 않는 size를 보정한다")
    void searchAiHistories_pageAndSize_normalized() {

        User master = DomainTestFixture.master();

        given(userFinder.getMasterUserByLoginId(MASTER_LOGIN_ID))
                .willReturn(master);

        // given: AI 이력 검색 repository가 빈 페이지를 반환한다.
        given(aiHistoryRepository.searchAiHistories(eq(AiStatus.SUCCESS), eq(PRODUCT_ID), isNull(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        // when: page=-1, size=20처럼 정책 밖의 요청이 들어온다.
        PageResponse<AiHistoryResponse> response = aiService.searchAiHistories(
                AiStatus.SUCCESS,
                PRODUCT_ID,
                null,
                -1,
                20,
                "desc",
                MASTER_LOGIN_ID
        );

        // then: page는 0, size는 10으로 보정되어 repository에 전달된다.
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(aiHistoryRepository).should()
                .searchAiHistories(eq(AiStatus.SUCCESS), eq(PRODUCT_ID), isNull(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(response.getPage()).isZero();
    }

    @Test
    @DisplayName("MASTER는 AI 이력을 상세 조회할 수 있다")
    void getAiHistory_master_success() {
        User master = DomainTestFixture.master();
        User owner = DomainTestFixture.owner();
        Category category = DomainTestFixture.category();
        Store store = DomainTestFixture.store(owner, category);
        Product product = DomainTestFixture.product(store, category);
        AiHistory aiHistory = DomainTestFixture.aiHistory(owner, product);

        given(userFinder.getMasterUserByLoginId(MASTER_LOGIN_ID)).willReturn(master);
        given(aiHistoryRepository.findActiveById(AI_HISTORY_ID)).willReturn(Optional.of(aiHistory));

        AiHistoryResponse response = aiService.getAiHistory(AI_HISTORY_ID, MASTER_LOGIN_ID);

        assertThat(response.getAiHistoryId()).isEqualTo(AI_HISTORY_ID);
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    @DisplayName("MASTER가 아니면 AI 이력을 상세 조회할 수 없다")
    void getAiHistory_nonMaster_fail() {
        given(userFinder.getMasterUserByLoginId(OWNER_LOGIN_ID))
                .willThrow(new BusinessException(GlobalErrorCode.FORBIDDEN));

        assertThatThrownBy(() -> aiService.getAiHistory(AI_HISTORY_ID, OWNER_LOGIN_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.FORBIDDEN.getMessage());

        verifyNoInteractions(aiHistoryRepository);
    }

    @Test
    @DisplayName("MASTER가 아니면 AI 이력을 검색할 수 없다")
    void searchAiHistories_nonMaster_fail() {
        given(userFinder.getMasterUserByLoginId(OWNER_LOGIN_ID))
                .willThrow(new BusinessException(GlobalErrorCode.FORBIDDEN));

        assertThatThrownBy(() -> aiService.searchAiHistories(null, null, null, 0, 10, "desc", OWNER_LOGIN_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.FORBIDDEN.getMessage());

        verifyNoInteractions(aiHistoryRepository);
    }
}
