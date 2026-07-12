package com.georgia.jeogiyo.ai.repository;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.entity.AiStatus;
import com.georgia.jeogiyo.ai.entity.QAiHistory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AiHistoryRepositoryImpl implements AiHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<AiHistory> findActiveById(UUID aiHistoryId) {
        QAiHistory aiHistory = QAiHistory.aiHistory;

        AiHistory result = queryFactory
                .selectFrom(aiHistory)
                .leftJoin(aiHistory.product).fetchJoin()
                .leftJoin(aiHistory.user).fetchJoin()
                .where(
                        aiHistory.aiHistoryId.eq(aiHistoryId),
                        aiHistory.isDeleted.isFalse()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<AiHistory> searchAiHistories(
            AiStatus aiStatus,
            UUID productId,
            UUID userId,
            Pageable pageable
    ) {
        QAiHistory aiHistory = QAiHistory.aiHistory;

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(aiHistory.isDeleted.isFalse());

        if (aiStatus != null) {
            condition.and(aiHistory.aiStatus.eq(aiStatus));
        }

        if (productId != null) {
            condition.and(aiHistory.product.productId.eq(productId));
        }

        if (userId != null) {
            condition.and(aiHistory.user.userId.eq(userId));
        }

        List<AiHistory> content = queryFactory
                .selectFrom(aiHistory)
                .leftJoin(aiHistory.product).fetchJoin()
                .leftJoin(aiHistory.user).fetchJoin()
                .where(condition)
                .orderBy(createdAtOrder(pageable, aiHistory))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(aiHistory.count())
                .from(aiHistory)
                .where(condition);

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                countQuery::fetchOne
        );
    }

    private OrderSpecifier<?> createdAtOrder(Pageable pageable, QAiHistory aiHistory) {
        boolean ascending = pageable.getSort()
                .getOrderFor("createdAt") != null
                && pageable.getSort().getOrderFor("createdAt").isAscending();

        return new OrderSpecifier<>(
                ascending ? Order.ASC : Order.DESC,
                aiHistory.createdAt
        );
    }
}