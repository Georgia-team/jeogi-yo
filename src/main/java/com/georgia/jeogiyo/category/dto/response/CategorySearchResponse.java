package com.georgia.jeogiyo.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategorySearchResponse {

    private final List<CategorySearchItemResponse> content;

    private final int page;

    private final int size;

    private final long totalElements;

    private final int totalPages;
}