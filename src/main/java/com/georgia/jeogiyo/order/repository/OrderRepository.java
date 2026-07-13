package com.georgia.jeogiyo.order.repository;

import com.georgia.jeogiyo.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, OrderRepositoryCustom {
    Optional<Order> findByOrderIdAndIsDeletedFalse(UUID orderId);
}