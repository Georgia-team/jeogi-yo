package com.georgia.jeogiyo.payment.service;

import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.util.PageUtil;
import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.repository.OrderRepository;
import com.georgia.jeogiyo.order.service.OrderService;
import com.georgia.jeogiyo.payment.dto.request.PaymentCancelRequest;
import com.georgia.jeogiyo.payment.dto.request.PaymentCreateRequest;
import com.georgia.jeogiyo.payment.dto.response.PaymentCancelResponse;
import com.georgia.jeogiyo.payment.dto.response.PaymentCreateResponse;
import com.georgia.jeogiyo.payment.dto.response.PaymentResponse;
import com.georgia.jeogiyo.payment.dto.response.PaymentSearchResponse;
import com.georgia.jeogiyo.payment.entity.Payment;
import com.georgia.jeogiyo.payment.entity.PaymentMethod;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import com.georgia.jeogiyo.payment.repository.PaymentRepository;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserFinder userFinder;
    private final EntityManager entityManager;
    private final OrderService orderService;

    @Override
    public PaymentCreateResponse createPayment(UUID orderId, String loginId, PaymentCreateRequest request) {
        User user = userFinder.getUserByLoginId(loginId);
        validateCustomer(user);

        Order order = findOrderById(orderId);
        validateOrderOwner(order, user);
        validatePayableOrder(order);

        if (request.getPaymentMethod() != PaymentMethod.CARD) {
            throw new IllegalArgumentException("현재 CARD 결제만 지원합니다.");
        }

        // 주문 1건에는 결제 1건만 허용한다. DB에서도 p_payment.order_id unique로 한 번 더 막는다.
        if (paymentRepository.existsByOrder_OrderId(orderId)) {
            throw new IllegalArgumentException("이미 결제 이력이 존재하는 주문입니다.");
        }

        Payment payment = new Payment(
                order,
                user,
                request.getPaymentMethod(),
                order.getTotalPrice()
        );

        Payment saved = paymentRepository.save(payment);

        log.info("Payment created. paymentId={}, orderId={}, userId={}",
                saved.getPaymentId(), saved.getOrderId(), saved.getUserId());

        return toCreateResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId, String loginId) {
        User user = userFinder.getUserByLoginId(loginId);
        Payment payment = findPaymentForRead(paymentId, user);

        validateReadable(user, payment);

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentSearchResponse> searchPayments(
            PaymentStatus paymentStatus,
            int page,
            int size,
            String sort,
            String loginId
    ) {
        User user = userFinder.getUserByLoginId(loginId);
        validateCustomerOrMaster(user);

        Pageable pageable = PageUtil.toPageable(page, size, sort);

        // CUSTOMER는 본인 결제만, MASTER는 관리 목적상 전체 결제를 조회한다.
        boolean includeDeleted = user.getRole() == Role.MASTER;
        UUID userId = user.getRole() == Role.MASTER ? null : user.getUserId();

        Page<Payment> paymentPage =
                paymentRepository.searchPayments(paymentStatus, userId, includeDeleted, pageable);

        return PageResponse.from(paymentPage, this::toSearchResponse);
    }

    @Override
    public PaymentCancelResponse cancelPayment(UUID paymentId, String loginId, PaymentCancelRequest request) {
        User user = userFinder.getUserByLoginId(loginId);
        validateCustomerOrMaster(user);

        Payment payment = findPaymentById(paymentId);
        validateReadable(user, payment);

        Order order = findOrderById(payment.getOrderId());
        validateCancelableOrder(order);

        payment.cancel(request.getCancelReason());
        orderService.cancelByPayment(order.getOrderId(), loginId);
        entityManager.flush();

        log.info("Payment canceled. paymentId={}, orderId={}, canceledBy={}",
                payment.getPaymentId(), order.getOrderId(), loginId);

        return PaymentCancelResponse.builder()
                .paymentId(payment.getPaymentId())
                .paymentStatus(payment.getPaymentStatus())
                .canceledAt(payment.getCanceledAt())
                .cancelReason(payment.getCancelReason())
                .build();
    }

    @Override
    public void deletePayment(UUID paymentId, String loginId) {
        userFinder.getMasterUserByLoginId(loginId);

        Payment payment = findPaymentById(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.CANCEL) {
            throw new IllegalArgumentException("취소 상태의 결제만 삭제할 수 있습니다.");
        }

        payment.softDelete(loginId);

        log.info("Payment soft deleted. paymentId={}, deletedBy={}", payment.getPaymentId(), loginId);
    }

    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findByPaymentIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
    }

    private Payment findPaymentForRead(UUID paymentId, User user) {
        if (user.getRole() == Role.MASTER) {
            return paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        }

        return findPaymentById(paymentId);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    private void validateOrderOwner(Order order, User user) {
        if (!order.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("본인 주문만 결제할 수 있습니다.");
        }
    }

    // 현재 정책상 주문 요청 상태에서만 결제를 허용한다.
    // OrderStatus에 결제 완료/주문 취소 상태가 추가되면 조건을 재검토해야 한다.
    private void validatePayableOrder(Order order) {
        if (order.getOrderStatus() != OrderStatus.ORDER_REQUESTED) {
            throw new IllegalArgumentException("결제 가능한 주문 상태가 아닙니다.");
        }
    }

    // 조리/배송이 진행된 주문의 결제 취소를 막기 위한 주문 상태 검증이다.
    private void validateCancelableOrder(Order order) {
        if (order.getOrderStatus() != OrderStatus.ORDER_REQUESTED) {
            throw new IllegalArgumentException("현재 주문 상태에서는 결제를 취소할 수 없습니다.");
        }
    }

    private void validateReadable(User user, Payment payment) {
        if (user.getRole() == Role.MASTER) {
            return;
        }

        if (user.getRole() != Role.CUSTOMER || !payment.isPaidBy(user.getUserId())) {
            throw new IllegalArgumentException("본인 결제만 처리할 수 있습니다.");
        }
    }

    private void validateCustomer(User user) {
        if (user.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("CUSTOMER만 결제할 수 있습니다.");
        }
    }

    private void validateCustomerOrMaster(User user) {
        if (user.getRole() != Role.CUSTOMER && user.getRole() != Role.MASTER) {
            throw new IllegalArgumentException("결제 처리 권한이 없습니다.");
        }
    }

    private PaymentCreateResponse toCreateResponse(Payment payment) {
        return PaymentCreateResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .approvedAt(payment.getApprovedAt())
                .build();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .approvedAt(payment.getApprovedAt())
                .canceledAt(payment.getCanceledAt())
                .cancelReason(payment.getCancelReason())
                .build();
    }

    private PaymentSearchResponse toSearchResponse(Payment payment) {
        return PaymentSearchResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}