package com.georgia.jeogiyo.payment.dto.response;

import com.georgia.jeogiyo.payment.entity.PaymentMethod;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * 결제 목록 검색 단건 응답 DTO
 * - 결제 목록 검색 결과의 각 결제 항목을 표현한다.
 * - 목록 화면에서는 상세 취소 사유보다 결제 식별 정보, 상태, 금액, 승인 시각 중심으로 반환한다.
 */
@Getter
@Builder
public class PaymentSearchResponse {

    @Schema(description = "결제 ID", example = "77777777-7777-7777-7777-777777777771")
    private UUID paymentId;

    @Schema(description = "주문 ID", example = "66666666-6666-6666-6666-666666666661")
    private UUID orderId;

    @Schema(description = "결제 수단", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "결제 상태", example = "SUCCESS")
    private PaymentStatus paymentStatus;

    @Schema(description = "결제 금액", example = "18000")
    private Integer amount;

    @Schema(description = "결제 승인 시각", example = "2026-07-13T12:00:00")
    private LocalDateTime approvedAt;
}