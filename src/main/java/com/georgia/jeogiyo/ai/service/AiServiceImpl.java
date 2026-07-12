package com.georgia.jeogiyo.ai.service;

import com.georgia.jeogiyo.ai.dto.request.AiDescriptionRequest;
import com.georgia.jeogiyo.ai.dto.response.AiDescriptionResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistoryResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistorySearchResponse;
import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.entity.AiStatus;
import com.georgia.jeogiyo.ai.repository.AiHistoryRepository;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiServiceImpl implements AiService {

    private static final String MODEL_NAME = "gemini-2.5-flash-lite";
    private static final String PROMPT_SUFFIX = "\n답변을 최대한 간결하게 50자 이하로";

    private final AiGeminiService aiGeminiService;

    private final AiHistoryRepository aiHistoryRepository;
    private final ProductRepository productRepository;
    private final UserFinder userFinder;

    @Override
    public AiDescriptionResponse createAiDescription(UUID productId, String loginId, AiDescriptionRequest request) {
        User user = userFinder.getOwnerUserByLoginId(loginId);

        Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (product.getStore().isDeleted()) {
            throw new IllegalArgumentException("삭제된 가게의 상품은 AI 설명을 생성할 수 없습니다.");
        }

        // 현재 loginId로 User를 찾고, 상품의 가게 owner와 비교
        validateOwner(user, product);

        String requestText = request.getRequestText() + PROMPT_SUFFIX;

        try {
            String responseText = aiGeminiService.generateDescription(requestText);

            product.updateDescription(responseText);

            AiHistory aiHistory = AiHistory.success(
                    user,
                    product,
                    requestText,
                    responseText,
                    MODEL_NAME
            );

            AiHistory saved = aiHistoryRepository.save(aiHistory);

            log.info("AI history saved. aiHistoryId={}, productId={}, userId={}, status={}",
                    saved.getAiHistoryId(),
                    saved.getProduct().getProductId(),
                    saved.getUser().getUserId(),
                    saved.getAiStatus());

            return AiDescriptionResponse.builder()
                    .aiHistoryId(saved.getAiHistoryId())
                    .userId(saved.getUser().getUserId())
                    .productId(saved.getProduct().getProductId())
                    .requestText(saved.getRequestText())
                    .responseText(saved.getResponseText())
                    .modelName(saved.getModelName())
                    .aiStatus(saved.getAiStatus())
                    .errorMessage(saved.getErrorMessage()) // 성공하면 null
                    .build();

        } catch (Exception e) {
            AiHistory aiHistory = AiHistory.fail(
                    user,
                    product,
                    requestText,
                    MODEL_NAME,
                    e.getMessage()
            );

            AiHistory saved = aiHistoryRepository.save(aiHistory);

            return AiDescriptionResponse.builder()
                    .aiHistoryId(saved.getAiHistoryId())
                    .userId(saved.getUser().getUserId())
                    .productId(saved.getProduct().getProductId())
                    .requestText(saved.getRequestText())
                    .responseText(saved.getResponseText())
                    .modelName(saved.getModelName())
                    .aiStatus(saved.getAiStatus())
                    .errorMessage(saved.getErrorMessage()) // 실패하면 있음
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AiHistoryResponse getAiHistory(UUID aiHistoryId, String loginId) {

        userFinder.getMasterUserByLoginId(loginId); // MASTER 검증
        AiHistory aiHistory = aiHistoryRepository.findActiveById(aiHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("AI 이력을 찾을 수 없습니다."));

        return toHistoryResponse(aiHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public AiHistorySearchResponse searchAiHistories(AiStatus aiStatus, UUID productId, UUID userId, int page, int size, String sort, String loginId) {

        userFinder.getMasterUserByLoginId(loginId); // MASTER 검증

        int validatedSize = (size == 10 || size == 30 || size == 50) ? size : 10;
        Sort.Direction direction = "asc".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                validatedSize,
                Sort.by(direction, "createdAt")
        );

        Page<AiHistory> aiHistoryPage =
                aiHistoryRepository.searchAiHistories(aiStatus, productId, userId, pageable);

        List<AiHistoryResponse> content = aiHistoryPage.getContent().stream()
                .map(this::toHistoryResponse)
                .toList();

        return AiHistorySearchResponse.builder()
                .content(content)
                .page(aiHistoryPage.getNumber())
                .size(aiHistoryPage.getSize())
                .totalElements(aiHistoryPage.getTotalElements())
                .totalPages(aiHistoryPage.getTotalPages())
                .build();
    }

    private void validateOwner(User user, Product product) {
        if (!product.getStore().getOwner().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("해당 상품의 가게 OWNER만 AI 설명을 생성할 수 있습니다.");
        }
    }

    private AiHistoryResponse toHistoryResponse(AiHistory aiHistory) {
        return AiHistoryResponse.builder()
                .aiHistoryId(aiHistory.getAiHistoryId())
                .productId(aiHistory.getProduct().getProductId())
                .userId(aiHistory.getUser().getUserId())
                .requestText(aiHistory.getRequestText())
                .responseText(aiHistory.getResponseText())
                .modelName(aiHistory.getModelName())
                .aiStatus(aiHistory.getAiStatus())
                .errorMessage(aiHistory.getErrorMessage())
                .build();
    }
}