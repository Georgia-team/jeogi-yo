package com.georgia.jeogiyo.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StoreSearchPageResponse {

    private List<StoreSearchResponse> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;
}
