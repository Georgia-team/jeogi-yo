package com.georgia.jeogiyo.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * 상품 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ProductCreateRequest {

    @Schema(description = "카테고리 ID", example = "22222222-2222-2222-2222-222222222221")
    @NotNull(message = "카테고리 설정은 필수입니다.")
    private UUID categoryId;

    @Schema(description = "상품명", example = "후라이드 치킨")
    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    /*
    * useAiDescription = false → 사용자가 입력한 description 저장
    * useAiDescription = true → AI가 생성한 설명으로 description 저장
    * */
    @Schema(description = "상품 설명", example = "바삭하게 튀긴 대표 치킨 메뉴입니다.")
    private String description;

    // Product 테이블에 저장하지 않는 AI 설명 생성 옵션
    @Schema(description = "AI 설명 사용 여부", example = "false")
    private Boolean useAiDescription = false;

    // Product 테이블에 저장하지 않는 AI 요청 프롬프트(Gemini에 보내는 요청값)
    @Schema(description = "AI 설명 생성 프롬프트", example = "매콤하고 바삭한 치킨 설명을 작성해줘")
    private String aiPrompt;

    @Schema(description = "상품 가격", example = "18000")
    @NotNull(message = "상품 가격은 필수입니다.")
    @Min(value = 1, message = "상품 가격은 0보다 커야 합니다.")
    private Integer price;

    @Schema(description = "재고 수량", example = "100")
    @NotNull(message = "상품 재고는 필수입니다.")
    @Min(value = 0, message = "상품 재고는 0 이상이어야 합니다.")
    private Integer stock;

    @Schema(description = "숨김 여부", example = "false")
    private Boolean isHidden = false;

}