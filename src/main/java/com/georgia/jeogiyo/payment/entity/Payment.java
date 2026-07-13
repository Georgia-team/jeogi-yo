package com.georgia.jeogiyo.payment.entity;

import com.georgia.jeogiyo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    // 주문 1건당 결제 1건만 허용하므로 order_id는 unique로 관리한다.
    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    // 현재 프로젝트는 PG 연동 없이 결제 요청 시 즉시 SUCCESS로 저장한다.
    // PG 연동이 추가되면 READY, FAIL 흐름을 이 생성 정책과 함께 재검토해야 한다.
    public Payment(UUID orderId, UUID userId, PaymentMethod paymentMethod, Integer amount) {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("결제 금액은 0 이상이어야 합니다.");
        }

        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.amount = amount;
        this.approvedAt = LocalDateTime.now();
    }

    // 결제 취소는 SUCCESS 상태에서만 가능하다.
    // 주문 상태가 취소 가능한지는 PaymentService에서 먼저 검증한다.
    public void cancel(String cancelReason) {
        if (this.paymentStatus != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("결제 성공 상태에서만 취소할 수 있습니다.");
        }

        this.paymentStatus = PaymentStatus.CANCEL;
        this.canceledAt = LocalDateTime.now();
        this.cancelReason = cancelReason;
    }

    public boolean isPaidBy(UUID userId) {
        return this.userId.equals(userId);
    }
}