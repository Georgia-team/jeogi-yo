package com.georgia.jeogiyo.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 상품 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ProductCreateRequest {

    @NotNull(message = "카테고리 설정은 필수입니다.")
    private UUID categoryId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    /*
    * useAiDescription = false → 사용자가 입력한 description 저장
    * useAiDescription = true → AI가 생성한 설명으로 description 저장
    * */
    private String description;

    // Product 테이블에 저장하지 않는 AI 설명 생성 옵션
    private Boolean useAiDescription = false;

    // Product 테이블에 저장하지 않는 AI 요청 프롬프트(Gemini에 보내는 요청값)
    private String aiPrompt;

    @NotNull(message = "상품 가격은 필수입니다.")
    @Min(value = 1, message = "상품 가격은 0보다 커야 합니다.")
    private Integer price;

    @NotNull(message = "상품 재고는 필수입니다.")
    @Min(value = 0, message = "상품 재고는 0 이상이어야 합니다.")
    private Integer stock;

    private Boolean isHidden = false;

}