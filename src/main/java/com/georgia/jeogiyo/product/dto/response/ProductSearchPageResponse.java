package com.georgia.jeogiyo.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductSearchPageResponse {

    private List<ProductSearchResponse> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;
}
