package com.georgia.jeogiyo.order.repository;

import com.georgia.jeogiyo.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderIdAndIsDeletedFalse(UUID orderId);
    List<Order> findByUserIdAndIsDeletedFalse(UUID userId);
}