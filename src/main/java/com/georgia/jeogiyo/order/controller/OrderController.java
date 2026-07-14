package com.georgia.jeogiyo.order.controller;

import com.georgia.jeogiyo.order.dto.request.OrderCancelRequest;
import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.request.OrderStatusUpdateRequest;
import com.georgia.jeogiyo.order.dto.response.*;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.service.OrderService;
import com.georgia.jeogiyo.user.entity.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @Secured(Role.Authority.CUSTOMER)
    public ResponseEntity<OrderCreateResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(authentication.getName(), orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<OrderSearchResponse> searchOrders(
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            Authentication authentication
    ) {
        OrderSearchResponse response = orderService.searchOrders(authentication.getName(), orderStatus, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stores/{storeId}/orders")
    @Secured({Role.Authority.OWNER, Role.Authority.MASTER})
    public ResponseEntity<OrderStoreSearchResponse> searchOrdersByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            Authentication authentication
    ) {
        OrderStoreSearchResponse response = orderService.searchOrdersByStore(authentication.getName(), storeId, orderStatus, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/orders/{orderId}/orderstatus")
    @Secured({Role.Authority.OWNER, Role.Authority.MASTER})
    public ResponseEntity<OrderStatusUpdateResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            Authentication authentication,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(authentication.getName(), orderId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/orders/{orderId}/cancel")
    @Secured({Role.Authority.CUSTOMER, Role.Authority.MASTER})
    public ResponseEntity<OrderCancelResponse> cancelOrder(
            @PathVariable UUID orderId,
            Authentication authentication,
            @RequestBody OrderCancelRequest request
    ) {
        OrderCancelResponse response = orderService.cancelOrder(authentication.getName(), orderId, request);
        return ResponseEntity.ok(response);
    }
}