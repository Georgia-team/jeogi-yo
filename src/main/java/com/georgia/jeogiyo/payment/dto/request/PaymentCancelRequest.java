package com.georgia.jeogiyo.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 결제 취소 요청 DTO
 * - CUSTOMER가 본인 결제를 취소하거나 MASTER가 결제를 취소할 때 사용한다.
 * - 취소 사유는 p_payment.cancel_reason에 저장된다.
 * - 실제 취소 가능 여부는 결제 상태와 주문 상태를 Service에서 검증한다.
 */
@Getter
@NoArgsConstructor
public class PaymentCancelRequest {

    @Schema(
            description = "결제 취소 사유",
            example = "고객 요청으로 인한 주문 취소",
            maxLength = 255
    )
    @NotBlank(message = "결제 취소 사유는 필수입니다.")
    @Size(max = 255, message = "결제 취소 사유는 255자 이하로 입력해야 합니다.")
    private String cancelReason;
}