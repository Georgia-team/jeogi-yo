package com.georgia.jeogiyo.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 상품 설명 생성 요청
 */
@Getter
@NoArgsConstructor
public class AiDescriptionRequest {


    @NotBlank(message = "AI 요청 문장은 필수입니다.")
    private String requestText;

}