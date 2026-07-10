package com.georgia.jeogiyo.ai.dto.response;

import com.georgia.jeogiyo.ai.entity.AiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI 상품 설명 생성 응답
 * */
@Getter
@Builder
@AllArgsConstructor
public class AiDescriptionResponse {

    private UUID aiHistoryId;

    private UUID userId;

    private UUID productId;

    private String requestText;

    private String responseText;

    private String modelName;

    private AiStatus aiStatus;

    private String errorMessage;

    private LocalDateTime createdAt;
}