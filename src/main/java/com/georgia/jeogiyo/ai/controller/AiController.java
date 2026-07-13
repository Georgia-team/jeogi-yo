package com.georgia.jeogiyo.ai.controller;

import com.georgia.jeogiyo.ai.dto.request.AiDescriptionRequest;
import com.georgia.jeogiyo.ai.dto.response.AiDescriptionResponse;
import com.georgia.jeogiyo.ai.dto.response.AiHistoryResponse;
import com.georgia.jeogiyo.ai.entity.AiStatus;
import com.georgia.jeogiyo.ai.service.AiService;
import com.georgia.jeogiyo.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "AI", description = "AI 상품 설명 및 AI 이력 API")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 상품 설명 생성", description = "OWNER가 본인 가게 상품에 대해 Gemini로 상품 설명을 생성하고 이력을 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 설명 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "OWNER 권한 없음")
    })
    @PostMapping("/products/{productId}/ai-description")
    public ResponseEntity<AiDescriptionResponse> createAiDescription(
            @Parameter(description = "상품 ID", example = "44444444-4444-4444-4444-444444444441")
            @PathVariable UUID productId,
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody AiDescriptionRequest request
    ) {
        AiDescriptionResponse response = aiService.createAiDescription(productId, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "AI 이력 상세 조회", description = "MASTER가 AI 응답 이력 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 이력 조회 성공"),
            @ApiResponse(responseCode = "403", description = "MASTER 권한 없음"),
            @ApiResponse(responseCode = "404", description = "AI 이력 없음")
    })
    @GetMapping("/ai-histories/{aiHistoryId}")
    public ResponseEntity<AiHistoryResponse> getAiHistory(
            @Parameter(description = "AI 이력 ID", example = "55555555-5555-5555-5555-555555555551")
            @PathVariable UUID aiHistoryId,
            @Parameter(hidden = true) Authentication authentication
            ) {
        AiHistoryResponse response = aiService.getAiHistory(aiHistoryId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "AI 이력 목록 검색", description = "MASTER가 AI 이력을 상태, 상품, 사용자 조건으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 이력 검색 성공"),
            @ApiResponse(responseCode = "403", description = "MASTER 권한 없음")
    })
    @GetMapping("/ai-histories")
    public ResponseEntity<PageResponse<AiHistoryResponse>> searchAiHistories(
            @Parameter(description = "AI 처리 상태", example = "SUCCESS")
            @RequestParam(required = false) AiStatus aiStatus,
            @Parameter(description = "상품 ID", example = "44444444-4444-4444-4444-444444444441")
            @RequestParam(required = false) UUID productId,
            @Parameter(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111112")
            @RequestParam(required = false) UUID userId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 허용값 10, 30, 50", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 방향", example = "desc")
            @RequestParam(defaultValue = "desc") String sort,
            @Parameter(hidden = true) Authentication authentication
    ) {
        PageResponse<AiHistoryResponse> response = aiService.searchAiHistories(
                aiStatus,
                productId,
                userId,
                page,
                size,
                sort,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }
}