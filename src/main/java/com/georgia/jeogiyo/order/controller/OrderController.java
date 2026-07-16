package com.georgia.jeogiyo.order.controller;

import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.util.PageUtil;
import com.georgia.jeogiyo.order.dto.request.OrderCancelRequest;
import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.request.OrderStatusUpdateRequest;
import com.georgia.jeogiyo.order.dto.response.*;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Order", description = "주문 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "CUSTOMER가 새로운 주문을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 또는 서비스 가능 지역 아님"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (CUSTOMER 아님, 본인 배송지 아님)"),
            @ApiResponse(responseCode = "404", description = "가게, 상품, 또는 배송지를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "영업 중이 아닌 가게 또는 주문 불가능한 상품")
    })
    @PostMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<OrderCreateResponse>> createOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("주문이 생성되었습니다.", response));
    }

    @Operation(summary = "주문 상세 조회", description = "주문 한 건을 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인 주문(또는 본인 가게 주문)이 아님"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable UUID orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(userDetails.getUsername(), orderId);
        return ResponseEntity.ok(CommonResponse.success("주문 정보를 조회했습니다.", response));
    }

    @Operation(summary = "주문 목록 조회", description = "로그인한 사용자의 권한 범위에 맞는 주문 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/orders")
    public ResponseEntity<CommonResponse<PageResponse<OrderSearchResponse>>> searchOrders(
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        Pageable pageable = PageUtil.toPageable(page, size, sort);
        PageResponse<OrderSearchResponse> response = orderService.searchOrders(userDetails.getUsername(), orderStatus, pageable);
        return ResponseEntity.ok(CommonResponse.success("주문 목록을 조회했습니다.", response));
    }

    @Operation(summary = "가게별 주문 목록 조회", description = "OWNER 또는 MASTER가 특정 가게의 주문 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게별 주문 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인 가게가 아니거나 권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음")
    })
    @GetMapping("/stores/{storeId}/orders")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    public ResponseEntity<CommonResponse<PageResponse<OrderStoreSearchResponse>>> searchOrdersByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        Pageable pageable = PageUtil.toPageable(page, size, sort);
        PageResponse<OrderStoreSearchResponse> response = orderService.searchOrdersByStore(userDetails.getUsername(), storeId, orderStatus, pageable);
        return ResponseEntity.ok(CommonResponse.success("가게 주문 목록을 조회했습니다.", response));
    }

    @Operation(summary = "주문 상태 변경", description = "OWNER 또는 MASTER가 주문 상태를 변경합니다. (결제 완료된 주문만 수락 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인 가게가 아니거나 권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문 또는 가게를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "허용되지 않은 상태 변경 또는 결제 미완료")
    })
    @PatchMapping("/orders/{orderId}/orderstatus")
    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    public ResponseEntity<CommonResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(userDetails.getUsername(), orderId, request);
        return ResponseEntity.ok(CommonResponse.success("주문 상태가 변경되었습니다.", response));
    }

    @Operation(summary = "주문 취소", description = "CUSTOMER 또는 MASTER가 주문을 취소합니다. CUSTOMER는 주문 후 5분 이내 ORDER_REQUESTED 상태에서만 취소 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 취소 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인 주문이 아니거나 권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "취소할 수 없는 상태의 주문")
    })
    @PatchMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','MASTER')")
    public ResponseEntity<CommonResponse<OrderCancelResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderCancelRequest request
    ) {
        OrderCancelResponse response = orderService.cancelOrder(userDetails.getUsername(), orderId, request);
        return ResponseEntity.ok(CommonResponse.success("주문이 취소되었습니다.", response));
    }
}