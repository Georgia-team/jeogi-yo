package com.georgia.jeogiyo.review.repository;

import com.georgia.jeogiyo.review.entity.QReview;
import com.georgia.jeogiyo.review.entity.Review;
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
import java.util.UUID;

/*
 * Review Querydsl Repository 구현체
 *
 * ReviewRepositoryCustom에 선언된 searchReviews 메서드를
 * Querydsl을 사용하여 실제로 구현한다.
 *
 * 주요 기능:
 * 1. 특정 가게의 리뷰 조회
 * 2. 평점 조건 동적 적용
 * 3. 삭제된 리뷰 제외
 * 4. 생성일 기준 정렬
 * 5. 페이징 처리
 * 6. 연관 엔티티 Fetch Join을 통한 N+1 문제 방지
 */
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    /*
     * Querydsl 쿼리를 생성하고 실행하기 위한 객체
     *
     * QuerydslConfig에서 Bean으로 등록된 JPAQueryFactory가
     * 생성자를 통해 주입된다.
     */
    private final JPAQueryFactory queryFactory;

    /*
     * 가게별 리뷰 목록 검색
     *
     * storeId와 isDeleted=false 조건은 항상 적용한다.
     * rating은 값이 전달된 경우에만 조건에 추가한다.
     */
    @Override
    public Page<Review> searchReviews(
            UUID storeId,
            Integer rating,
            Pageable pageable
    ) {
        /*
         * Querydsl이 Review 엔티티를 기준으로 생성한 Q 클래스
         */
        QReview review = QReview.review;

        /*
         * 동적 검색 조건을 저장하는 객체
         */
        BooleanBuilder condition = new BooleanBuilder();

        /*
         * 특정 가게의 리뷰만 조회
         *
         * Review 엔티티가 Store를 직접 참조하고 있으므로
         * review.store.storeId 형태로 조건을 작성한다.
         */
        condition.and(
                review.store.storeId.eq(storeId)
        );

        /*
         * Soft Delete 처리된 리뷰 제외
         *
         * isDeleted가 false인 리뷰만 조회한다.
         */
        condition.and(
                review.isDeleted.isFalse()
        );

        /*
         * 평점 검색 조건
         *
         * rating이 null이 아니면 해당 평점의 리뷰만 조회한다.
         * rating이 null이면 평점 조건을 적용하지 않는다.
         */
        if (rating != null) {
            condition.and(
                    review.rating.eq(rating)
            );
        }

        /*
         * 실제 리뷰 목록 조회
         *
         * user:
         * 응답 DTO에서 작성자 userId를 사용할 수 있으므로 함께 조회한다.
         *
         * store:
         * 응답 DTO에서 storeId를 사용할 수 있으므로 함께 조회한다.
         *
         * order:
         * 응답 DTO에서 orderId를 사용할 수 있으므로 함께 조회한다.
         *
         * Fetch Join을 사용하여 연관 엔티티 접근 시
         * 추가 쿼리가 반복되는 N+1 문제를 방지한다.
         */
        List<Review> content = queryFactory
                .selectFrom(review)
                .leftJoin(review.user).fetchJoin()
                .leftJoin(review.store).fetchJoin()
                .leftJoin(review.order).fetchJoin()
                .where(condition)
                .orderBy(createdAtOrder(pageable, review))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /*
         * 전체 리뷰 개수 조회 쿼리
         *
         * 검색 조건에 해당하는 전체 리뷰 수를 조회하여
         * totalElements와 totalPages를 계산할 때 사용한다.
         *
         * 개수만 조회하면 되므로 Fetch Join은 사용하지 않는다.
         */
        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(condition);

        /*
         * 조회 결과를 Page 객체로 변환
         *
         * PageableExecutionUtils는 마지막 페이지 등에서
         * 전체 개수 조회가 불필요한 경우 count 쿼리를 생략할 수 있다.
         */
        return PageableExecutionUtils.getPage(
                content,
                pageable,
                countQuery::fetchOne
        );
    }

    /*
     * 리뷰 생성일 정렬 조건 생성
     *
     * createdAt 오름차순 요청이면 ASC를 적용하고,
     * 그 외에는 기본값인 DESC를 적용한다.
     */
    private OrderSpecifier<?> createdAtOrder(
            Pageable pageable,
            QReview review
    ) {
        /*
         * Pageable에 createdAt 정렬 조건이 존재하고
         * 해당 정렬이 오름차순인지 확인한다.
         */
        boolean ascending = pageable.getSort()
                .getOrderFor("createdAt") != null
                && pageable.getSort()
                .getOrderFor("createdAt")
                .isAscending();

        /*
         * 오름차순이면 ASC,
         * 정렬 조건이 없거나 내림차순이면 DESC를 반환한다.
         */
        return new OrderSpecifier<>(
                ascending ? Order.ASC : Order.DESC,
                review.createdAt
        );
    }
}