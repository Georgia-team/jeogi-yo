package com.georgia.jeogiyo.payment.service;

import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.payment.dto.request.PaymentCancelRequest;
import com.georgia.jeogiyo.payment.dto.request.PaymentCreateRequest;
import com.georgia.jeogiyo.payment.dto.response.PaymentCancelResponse;
import com.georgia.jeogiyo.payment.dto.response.PaymentCreateResponse;
import com.georgia.jeogiyo.payment.dto.response.PaymentResponse;
import com.georgia.jeogiyo.payment.dto.response.PaymentSearchResponse;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;

import java.util.UUID;

public interface PaymentService {

    PaymentCreateResponse createPayment(UUID orderId, String loginId, PaymentCreateRequest request);

    PaymentResponse getPayment(UUID paymentId, String loginId);

    PageResponse<PaymentSearchResponse> searchPayments(
            PaymentStatus paymentStatus,
            int page,
            int size,
            String sort,
            String loginId
    );

    PaymentCancelResponse cancelPayment(UUID paymentId, String loginId, PaymentCancelRequest request);

    void deletePayment(UUID paymentId, String loginId);
}