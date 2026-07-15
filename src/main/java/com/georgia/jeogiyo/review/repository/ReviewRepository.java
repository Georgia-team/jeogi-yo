package com.georgia.jeogiyo.review.repository;

import com.georgia.jeogiyo.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/*
 * 1. JpaRepository가 제공하는 기본 CRUD 기능 사용
 * 2. 간단한 조건 조회는 Spring Data JPA 메서드 이름 규칙으로 처리
 * 3. 복잡한 리뷰 검색은 ReviewRepositoryCustom과 연결하여 Querydsl로 처리
 */
public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

    // 삭제되지 않은 리뷰 조회 (reviewId가 일치하고 isDeleted가 false인 리뷰만 조회)
    Optional<Review> findByReviewIdAndIsDeletedFalse(UUID reviewId);

    // 주문에 이미 리뷰가 존재하는지 확인 (삭제되지 않은 리뷰만 존재 여부를 확인)
    boolean existsByOrder_OrderIdAndIsDeletedFalse(UUID orderId);

    long countByStore_StoreIdAndIsDeletedFalse(UUID storeId);

    @Query("""
        select coalesce(avg(r.rating), 0.0)
        from Review r
        where r.store.storeId = :storeId
          and r.isDeleted = false
        """)
    Double findAverageRatingByStoreId(@Param("storeId") UUID storeId);
}