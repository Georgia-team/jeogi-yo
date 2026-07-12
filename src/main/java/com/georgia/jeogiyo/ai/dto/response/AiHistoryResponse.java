package com.georgia.jeogiyo.ai.dto.response;

import com.georgia.jeogiyo.ai.entity.AiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;


/**
 * I 응답 이력 상세 조회 응답
 * */
@Getter
@Builder
@AllArgsConstructor
public class AiHistoryResponse {

    private UUID aiHistoryId;

    private UUID productId;

    private UUID userId;

    private String requestText;

    private String responseText;

    private String modelName;

    private AiStatus aiStatus;

    private String errorMessage;

}