package com.georgia.jeogiyo.payment.repository;

import com.georgia.jeogiyo.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderIdAndIsDeletedFalse(UUID orderId);
}