package com.georgia.jeogiyo.payment.dto.response;

import com.georgia.jeogiyo.payment.entity.PaymentMethod;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * 결제 생성 응답 DTO
 * - 결제 생성 직후 클라이언트에 반환되는 응답이다.
 * - 현재 PG 연동 없이 요청 즉시 SUCCESS 상태로 저장된다.
 * - 감사 필드(createdAt, createdBy 등)는 응답에 노출하지 않는다.
 */
@Getter
@Builder
public class PaymentCreateResponse {

    @Schema(description = "결제 ID", example = "77777777-7777-7777-7777-777777777771")
    private UUID paymentId;

    @Schema(description = "주문 ID", example = "66666666-6666-6666-6666-666666666661")
    private UUID orderId;

    @Schema(description = "결제 사용자 ID", example = "11111111-1111-1111-1111-111111111112")
    private UUID userId;

    @Schema(description = "결제 수단", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "결제 상태", example = "SUCCESS")
    private PaymentStatus paymentStatus;

    @Schema(description = "결제 금액", example = "18000")
    private Integer amount;

    @Schema(description = "결제 승인 시각", example = "2026-07-13T12:00:00")
    private LocalDateTime approvedAt;
}