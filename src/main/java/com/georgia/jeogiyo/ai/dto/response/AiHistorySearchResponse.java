package com.georgia.jeogiyo.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AiHistorySearchResponse {
    /**
     * AI 응답 이력 목록 조회 응답 DTO
     * >> commit xxx
     *
     * TODO
     * - 공통 PageResponse<T> 도입 시 삭제 예정 : page, size등 관리
     * - Search API는 ApiResponse<PageResponse<T>> 구조로 변경 예정
     * - QueryDSL 검색 기능 구현(ServiceImpl) 시 함께 리팩토링 예정
     */

    private List<AiHistoryResponse> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;
}