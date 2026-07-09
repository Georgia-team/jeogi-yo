package com.georgia.jeogiyo.store.repository;

import com.georgia.jeogiyo.store.entity.QStore;
import com.georgia.jeogiyo.store.entity.Store;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Store> searchStores(
            UUID categoryId,
            String keyword,
            Pageable pageable
    ) {
        QStore store = QStore.store;

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(store.isDeleted.isFalse());

        if (categoryId != null) {
            condition.and(store.category.categoryId.eq(categoryId));
        }

        if (StringUtils.hasText(keyword)) {
            condition.and(store.storeName.containsIgnoreCase(keyword.trim()));
        }

        List<Store> content = queryFactory
                .selectFrom(store)
                .leftJoin(store.owner).fetchJoin()
                .leftJoin(store.category).fetchJoin()
                .where(condition)
                .orderBy(createdAtOrder(pageable, store))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(store.count())
                .from(store)
                .where(condition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?> createdAtOrder(Pageable pageable, QStore store) {
        boolean ascending = pageable.getSort()
                .getOrderFor("createdAt") != null
                && pageable.getSort().getOrderFor("createdAt").isAscending();

        return new OrderSpecifier<>(
                ascending ? Order.ASC : Order.DESC,
                store.createdAt
        );
    }
}
