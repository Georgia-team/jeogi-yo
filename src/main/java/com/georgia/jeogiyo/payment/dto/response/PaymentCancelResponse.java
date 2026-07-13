package com.georgia.jeogiyo.payment.dto.response;

import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * 결제 취소 응답 DTO
 * - 결제 취소 성공 후 변경된 결제 상태와 취소 정보를 반환한다.
 * - 결제 상태는 SUCCESS에서 CANCEL로 변경된다.
 * - 주문 상태 변경은 Order 도메인 정책 확정 후 별도로 연동한다.
 */
@Getter
@Builder
public class PaymentCancelResponse {
    @Schema(description = "결제 ID", example = "77777777-7777-7777-7777-777777777771")
    private UUID paymentId;

    @Schema(description = "결제 상태", example = "CANCEL")
    private PaymentStatus paymentStatus;

    @Schema(description = "결제 취소 시각", example = "2026-07-13T12:10:00")
    private LocalDateTime canceledAt;

    @Schema(description = "결제 취소 사유", example = "고객 요청으로 인한 주문 취소")
    private String cancelReason;
}