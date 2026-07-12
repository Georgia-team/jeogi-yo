package com.georgia.jeogiyo.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 상품 목록 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class ProductSearchResponse {

    private UUID productId;

    private UUID storeId;

    private UUID categoryId;

    private String productName;

    private Integer price;

    private Integer stock;

    private Boolean isHidden;

}
