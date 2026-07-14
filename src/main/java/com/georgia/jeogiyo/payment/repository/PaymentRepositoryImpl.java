package com.georgia.jeogiyo.payment.repository;

import com.georgia.jeogiyo.payment.entity.Payment;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import com.georgia.jeogiyo.payment.entity.QPayment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Payment> searchPayments(
            PaymentStatus paymentStatus,
            UUID userId,
            boolean includeDeleted,
            Pageable pageable
    ) {
        QPayment payment = QPayment.payment;

        BooleanBuilder condition = new BooleanBuilder();

        if (!includeDeleted) {
            condition.and(payment.isDeleted.isFalse());
        }

        if (paymentStatus != null) {
            condition.and(payment.paymentStatus.eq(paymentStatus));
        }

        if (userId != null) {
            condition.and(payment.user.userId.eq(userId));
        }

        OrderSpecifier<?> orderSpecifier = pageable.getSort().stream()
                .findFirst()
                .map(order -> order.isAscending() ? payment.createdAt.asc() : payment.createdAt.desc())
                .orElse(payment.createdAt.desc());

        List<Payment> content = queryFactory
                .selectFrom(payment)
                .leftJoin(payment.order).fetchJoin()
                .leftJoin(payment.user).fetchJoin()
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifier)
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(payment.count())
                .from(payment)
                .where(condition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}