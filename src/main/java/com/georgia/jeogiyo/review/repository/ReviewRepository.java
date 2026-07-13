package com.georgia.jeogiyo.review.repository;

import com.georgia.jeogiyo.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 삭제되지 않은 리뷰 상세 조회
    Optional<Review> findByReviewIdAndIsDeletedFalse(UUID reviewId);

    // 한 주문에 이미 리뷰가 작성되었는지 확인
    boolean existsByOrder_OrderIdAndIsDeletedFalse(UUID orderId);

    // 특정 가게의 전체 리뷰 조회
    Page<Review> findAllByStore_StoreIdAndIsDeletedFalse(
            UUID storeId,
            Pageable pageable
    );

    // 특정 가게의 평점별 리뷰 조회
    Page<Review> findAllByStore_StoreIdAndRatingAndIsDeletedFalse(
            UUID storeId,
            Integer rating,
            Pageable pageable
    );
}