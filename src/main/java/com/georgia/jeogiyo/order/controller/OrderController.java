package com.georgia.jeogiyo.order.controller;


import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.response.OrderCreateResponse;
import com.georgia.jeogiyo.order.dto.response.OrderDetailResponse;
import com.georgia.jeogiyo.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @RequestParam String loginId, // TODO JWT
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(loginId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @PathVariable UUID orderId,
            @RequestParam String loginId
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(loginId, orderId);
        return ResponseEntity.ok(response);
    }
}