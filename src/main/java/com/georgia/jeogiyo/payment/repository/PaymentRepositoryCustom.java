package com.georgia.jeogiyo.payment.repository;

import com.georgia.jeogiyo.payment.entity.Payment;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentRepositoryCustom {

    Page<Payment> searchPayments(
            PaymentStatus paymentStatus,
            UUID userId,
            boolean includeDeleted,
            Pageable pageable
    );
}