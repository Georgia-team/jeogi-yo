package com.georgia.jeogiyo.review.service;

import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.security.UserDetailsImpl;
import com.georgia.jeogiyo.global.util.PageUtil;
import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.repository.OrderRepository;
import com.georgia.jeogiyo.review.dto.request.ReviewCreateRequest;
import com.georgia.jeogiyo.review.dto.request.ReviewUpdateRequest;
import com.georgia.jeogiyo.review.dto.response.ReviewCreateResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewDeleteResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewReadResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewSearchItemResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewUpdateResponse;
import com.georgia.jeogiyo.review.entity.Review;
import com.georgia.jeogiyo.review.repository.ReviewRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewCreateResponse createReview(UUID orderId, ReviewCreateRequest requestDto, UserDetailsImpl userDetails) {
        User loginUser = userDetails.getUser(); // 현재 로그인된 사용자 정보

        // 1) 선택한 주문이 존재하는지
        Order order = orderRepository
                .findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 주문입니다."
                ));

        // 2) 로그인 사용자가 해당 주문의 주문자인지 확인
        if (!order.getUserId().equals(loginUser.getUserId())) {
            throw new IllegalArgumentException(
                    "본인이 주문한 주문에만 리뷰를 작성할 수 있습니다."
            );
        }

        // 3) 주문 완료 여부 확인
        if (order.getOrderStatus() != OrderStatus.ORDER_COMPLETED) {
            throw new IllegalArgumentException(
                    "주문 및 배송이 완료된 이후에만 리뷰를 작성할 수 있습니다."
            );
        }

        // 4) 주문당 리뷰 한 개만 작성 가능한지 확인
        if (reviewRepository
                .existsByOrder_OrderIdAndIsDeletedFalse(orderId)) {
            throw new IllegalArgumentException(
                    "이미 리뷰가 작성된 주문입니다."
            );
        }

        // 5) 주문 정보에 저장된 storeId로 가게 조회
        Store store = storeRepository
                .findByStoreIdAndIsDeletedFalse(order.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 가게입니다."
                ));

        Review review = new Review(
                loginUser,
                store,
                order,
                requestDto.getRating(),
                requestDto.getContent()
        );

        Review savedReview = reviewRepository.save(review); // 리뷰 저장

        return ReviewCreateResponse.of(savedReview); // 응답 DTO 변환
    }

    // 리뷰 상세 조회 Service
    @Transactional(readOnly = true)
    public ReviewReadResponse readReview(UUID reviewId) {
        Review review = reviewRepository
                .findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 리뷰입니다."
                ));

        return ReviewReadResponse.of(review);
    }

    // 가게별 리뷰 목록 검색 API
    @Transactional(readOnly = true)
    public PageResponse<ReviewSearchItemResponse> searchReviews(
            UUID storeId,
            Integer rating,
            int page,
            int size,
            String sort
    ) {
        // 1. 가게 존재 여부 확인
        storeRepository
                .findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 가게입니다."
                ));

        // 2. 평점이 전달되었다면 1~5 범위인지 확인
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException(
                    "평점은 1점부터 5점 사이여야 합니다."
            );
        }

        // 3. 공통 페이징 정책 적용
        // page, size, sort 값을 검증하고 createdAt 기준 Pageable을 생성한다.
        Pageable pageable = PageUtil.toPageable(
                page,
                size,
                sort
        );

        // 4. Querydsl을 사용하여 가게별 리뷰 검색
        // rating이 null이면 전체 평점 리뷰를 조회하고,
        // rating이 존재하면 해당 평점의 리뷰만 조회한다.
        Page<Review> reviewPage =
                reviewRepository.searchReviews(
                        storeId,
                        rating,
                        pageable
                );

        // 5. Page<Review>를 공통 페이지 응답 DTO로 변환
        return PageResponse.from(
                reviewPage,
                ReviewSearchItemResponse::of
        );
    }

    // 리뷰 수정 Service
    @Transactional
    public ReviewUpdateResponse updateReview(UUID reviewId, ReviewUpdateRequest requestDto, UserDetailsImpl userDetails) {
        User loginUser = userDetails.getUser(); // 현재 로그인 사용자

        // 1) 리뷰 조회
        Review review = reviewRepository
                .findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 리뷰입니다."
                ));

        // 2) 작성자 본인인지 확인
        if (!review.getUser().getUserId()
                .equals(loginUser.getUserId())) {
            throw new IllegalArgumentException(
                    "본인이 작성한 리뷰만 수정할 수 있습니다."
            );
        }

        // 3) 수정할 값이 하나라도 있는지 확인
        if (requestDto.getRating() == null
                && requestDto.getContent() == null) {
            throw new IllegalArgumentException(
                    "수정할 평점 또는 내용을 입력해 주세요."
            );
        }

        review.update(requestDto.getRating(), requestDto.getContent());

        return ReviewUpdateResponse.of(review);
    }

    // 리뷰 삭제 Service
    @Transactional
    public ReviewDeleteResponse deleteReview(UUID reviewId, UserDetailsImpl userDetails) {
        User loginUser = userDetails.getUser(); // 현재 로그인 사용자

        // 1) 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 리뷰입니다."
                ));

        // 2) 이미 삭제된 리뷰인지 확인
        if (review.isDeleted()) {
            throw new IllegalArgumentException(
                    "이미 삭제된 리뷰입니다."
            );
        }

        // TODO: 추가로 Review가 다른 entity에서의 연관성 있는지 확인

        // 4. Soft Delete
        review.softDelete(loginUser.getLoginId());

        // 5. 삭제 결과 반환
        return ReviewDeleteResponse.of(review);
    }
}