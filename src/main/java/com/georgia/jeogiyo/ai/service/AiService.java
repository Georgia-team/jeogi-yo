package com.georgia.jeogiyo.ai.service;

import com.georgia.jeogiyo.ai.dto.request.AiDescriptionRequest;
import com.georgia.jeogiyo.ai.dto.response.AiDescriptionResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistoryResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistorySearchResponse;
import com.georgia.jeogiyo.ai.entity.AiStatus;

import java.util.UUID;

public interface AiService {

    AiDescriptionResponse createAiDescription(UUID productId, String loginId, AiDescriptionRequest request);

    AiHistoryResponse getAiHistory(UUID aiHistoryId);

    AiHistorySearchResponse searchAiHistories(AiStatus aiStatus, UUID productId, int page, int size, String sort);
}