package com.georgia.jeogiyo.product.repository;

import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.entity.QProduct;
import com.georgia.jeogiyo.user.entity.Role;
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
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(
            UUID storeId,
            UUID categoryId,
            String keyword,
            Role role,
            String loginId,
            Pageable pageable
    ) {
        QProduct product = QProduct.product;

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(product.isDeleted.isFalse());
        condition.and(product.store.storeId.eq(storeId));

        if (categoryId != null) {
            condition.and(product.category.categoryId.eq(categoryId));
        }

        if (StringUtils.hasText(keyword)) {
            condition.and(product.productName.containsIgnoreCase(keyword.trim()));
        }

        applyVisibilityCondition(condition, product, role, loginId);

        List<Product> content = queryFactory
                .selectFrom(product)
                .leftJoin(product.store).fetchJoin()
                .leftJoin(product.category).fetchJoin()
                .leftJoin(product.store.owner).fetchJoin()
                .where(condition)
                .orderBy(createdAtOrder(pageable, product))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(condition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private void applyVisibilityCondition(
            BooleanBuilder condition,
            QProduct product,
            Role role,
            String loginId
    ) {
        if (role == Role.MASTER) {
            return;
        }

        if (role == Role.OWNER) {
            condition.and(
                    product.isHidden.isFalse()
                            .or(product.store.owner.loginId.eq(loginId))
            );
            return;
        }

        condition.and(product.isHidden.isFalse());
    }

    private OrderSpecifier<?> createdAtOrder(Pageable pageable, QProduct product) {
        boolean ascending = pageable.getSort()
                .getOrderFor("createdAt") != null
                && pageable.getSort().getOrderFor("createdAt").isAscending();

        return new OrderSpecifier<>(
                ascending ? Order.ASC : Order.DESC,
                product.createdAt
        );
    }
}
