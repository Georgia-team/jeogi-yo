package com.georgia.jeogiyo.payment.dto.response;

import com.georgia.jeogiyo.payment.entity.PaymentMethod;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * 결제 상세 조회 응답 DTO
 * - 결제 상세 조회 시 반환된다.
 * - CUSTOMER는 본인 결제만 조회할 수 있고 MASTER는 전체 결제를 조회할 수 있다.
 * - 취소된 결제라면 canceledAt, cancelReason이 함께 반환된다.
 */
@Getter
@Builder
public class PaymentResponse {

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

    @Schema(description = "결제 취소 시각", example = "2026-07-13T12:10:00")
    private LocalDateTime canceledAt;

    @Schema(description = "결제 취소 사유", example = "고객 요청으로 인한 주문 취소")
    private String cancelReason;
}