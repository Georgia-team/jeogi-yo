package com.georgia.jeogiyo.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 상품 설명 생성 요청
 */
@Getter
@NoArgsConstructor
public class AiDescriptionRequest {

    @Schema(description = "AI 상품 설명 생성 요청 문구", example = "신메뉴 치킨을 고객이 먹고 싶게 설명해줘")
    @NotBlank(message = "AI 요청 문장은 필수입니다.")
    private String requestText;

}