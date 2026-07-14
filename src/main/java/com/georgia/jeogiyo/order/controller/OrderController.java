package com.georgia.jeogiyo.order.controller;

import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.util.PageUtil;
import com.georgia.jeogiyo.global.security.UserDetailsImpl;
import com.georgia.jeogiyo.order.dto.request.OrderCancelRequest;
import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.request.OrderStatusUpdateRequest;
import com.georgia.jeogiyo.order.dto.response.*;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse<>(true, "주문이 생성되었습니다.", response));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(new CommonResponse<>(true, "주문 정보를 조회했습니다.", response));
    }

    @GetMapping("/orders")
    public ResponseEntity<CommonResponse<PageResponse<OrderSearchResponse>>> searchOrders(
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Pageable pageable = PageUtil.toPageable(page, size, sort);
        PageResponse<OrderSearchResponse> response = orderService.searchOrders(userDetails.getUsername(), orderStatus, pageable);
        return ResponseEntity.ok(new CommonResponse<>(true, "주문 목록을 조회했습니다.", response));
    }

    @GetMapping("/stores/{storeId}/orders")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    public ResponseEntity<CommonResponse<PageResponse<OrderStoreSearchResponse>>> searchOrdersByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Pageable pageable = PageUtil.toPageable(page, size, sort);
        PageResponse<OrderStoreSearchResponse> response = orderService.searchOrdersByStore(userDetails.getUsername(), storeId, orderStatus, pageable);
        return ResponseEntity.ok(new CommonResponse<>(true, "가게 주문 목록을 조회했습니다.", response));
    }

    @PatchMapping("/orders/{orderId}/orderstatus")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    public ResponseEntity<CommonResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(userDetails.getUsername(), orderId, request);
        return ResponseEntity.ok(new CommonResponse<>(true, "주문 상태가 변경되었습니다.", response));
    }

    @PatchMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','MASTER')")
    public ResponseEntity<CommonResponse<OrderCancelResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody OrderCancelRequest request
    ) {
        OrderCancelResponse response = orderService.cancelOrder(userDetails.getUsername(), orderId, request);
        return ResponseEntity.ok(new CommonResponse<>(true, "주문이 취소되었습니다.", response));
    }
}