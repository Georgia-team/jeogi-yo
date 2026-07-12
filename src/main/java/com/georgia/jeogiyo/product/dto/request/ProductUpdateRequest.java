package com.georgia.jeogiyo.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 상품 정보 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ProductUpdateRequest {

    @Schema(description = "카테고리 ID", example = "22222222-2222-2222-2222-222222222221")
    private UUID categoryId;

    @Schema(description = "상품명", example = "수정된 후라이드 치킨")
    private String productName;

    @Schema(description = "상품 설명", example = "더 바삭하게 개선된 대표 치킨 메뉴입니다.")
    private String description;

    @Schema(description = "상품 가격", example = "19000")
    @Min(value = 1, message = "상품 가격은 0보다 커야 합니다.")
    private Integer price;

    @Schema(description = "재고 수량", example = "50")
    @Min(value = 0, message = "상품 재고는 0 이상이어야 합니다.")
    private Integer stock;

    // 상품 숨김 여부(기본값 false)
    @Schema(description = "숨김 여부", example = "false")
    private Boolean isHidden;
}