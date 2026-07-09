package com.georgia.jeogiyo.product.dto.request;

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

    private UUID categoryId;

    private String productName;

    private String description;

    @Min(value = 1, message = "상품 가격은 0보다 커야 합니다.")
    private Integer price;

    @Min(value = 0, message = "상품 재고는 0 이상이어야 합니다.")
    private Integer stock;

    // 상품 숨김 여부(기본값 false)
    private Boolean isHidden;
}