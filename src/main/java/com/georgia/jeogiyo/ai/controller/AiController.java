package com.georgia.jeogiyo.ai.controller;

import com.georgia.jeogiyo.ai.dto.request.AiDescriptionRequest;
import com.georgia.jeogiyo.ai.dto.response.AiDescriptionResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistoryResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistorySearchResponse;
import com.georgia.jeogiyo.ai.entity.AiStatus;
import com.georgia.jeogiyo.ai.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AiController {

    private final AiService aiService;

    @PostMapping("/products/{productId}/ai-description")
    public ResponseEntity<AiDescriptionResponse> createAiDescription(
            @PathVariable UUID productId,
            @RequestParam String loginId, // TODO JWT 적용 후 @AuthenticationPrincipal에서 loginId 추출
            @Valid @RequestBody AiDescriptionRequest request
    ) {
        AiDescriptionResponse response = aiService.createAiDescription(productId, loginId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ai-histories/{aiHistoryId}")
    public ResponseEntity<AiHistoryResponse> getAiHistory(
            @PathVariable UUID aiHistoryId
    ) {
        AiHistoryResponse response = aiService.getAiHistory(aiHistoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ai-histories")
    public ResponseEntity<AiHistorySearchResponse> searchAiHistories(
            @RequestParam(required = false) AiStatus aiStatus,
            @RequestParam(required = false) UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        AiHistorySearchResponse response = aiService.searchAiHistories(aiStatus, productId, page, size, sort);
        return ResponseEntity.ok(response);
    }
}