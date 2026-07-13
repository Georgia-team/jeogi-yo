package com.georgia.jeogiyo.payment.dto.request;

import com.georgia.jeogiyo.payment.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 결제 생성 요청 DTO
 *
 * - CUSTOMER가 본인 주문에 대해 결제를 요청할 때 사용한다.
 * - 현재 프로젝트 정책상 결제 수단은 CARD만 지원한다.
 * - 결제 금액은 요청으로 받지 않고 주문 totalPrice를 기준으로 서버에서 결정한다.
 */
@Getter
@NoArgsConstructor
public class PaymentCreateRequest {

    @Schema(
            description = "결제 수단. 현재 프로젝트 정책상 CARD만 지원합니다.",
            example = "CARD",
            allowableValues = {"CARD"}
    )
    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;
}