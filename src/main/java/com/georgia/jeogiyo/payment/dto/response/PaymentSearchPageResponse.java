package com.georgia.jeogiyo.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/*
 * 결제 목록 검색 페이지 응답 DTO
 * - 결제 목록 검색 결과와 페이지 정보를 함께 반환한다.
 * - CUSTOMER는 본인 결제 목록만 조회하고 MASTER는 전체 결제 목록을 조회한다.
 * - page가 음수이면 0, size가 10/30/50이 아니면 10으로 보정된다.
 */
@Getter
@Builder
public class PaymentSearchPageResponse {

    @Schema(description = "결제 목록")
    private List<PaymentSearchResponse> content;

    @Schema(description = "현재 페이지 번호", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "전체 데이터 수", example = "25")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "3")
    private int totalPages;
}