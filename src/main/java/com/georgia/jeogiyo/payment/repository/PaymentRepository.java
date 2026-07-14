package com.georgia.jeogiyo.payment.repository;

import com.georgia.jeogiyo.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, PaymentRepositoryCustom {

    Optional<Payment> findByPaymentIdAndIsDeletedFalse(UUID paymentId);

    Optional<Payment> findByPaymentId(UUID paymentId);

    // OrderService에서 주문 취소 시 결제도 같이 취소한다
    Optional<Payment> findByOrder_OrderIdAndIsDeletedFalse(UUID orderId);

    boolean existsByOrder_OrderId(UUID orderId);
}