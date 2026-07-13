package com.georgia.jeogiyo.review.service;

import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.repository.OrderRepository;
import com.georgia.jeogiyo.review.dto.request.ReviewCreateRequest;
import com.georgia.jeogiyo.review.dto.request.ReviewUpdateRequest;
import com.georgia.jeogiyo.review.dto.response.ReviewCreateResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewDeleteResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewReadResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewSearchItemResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewSearchResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewUpdateResponse;
import com.georgia.jeogiyo.review.entity.Review;
import com.georgia.jeogiyo.review.repository.ReviewRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    // 리뷰 생성 Service
    @Transactional
    public ReviewCreateResponse createReview(
            UUID orderId,
            ReviewCreateRequest requestDto,
            String loginId
    ) {
        // 1. 로그인 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 사용자입니다."
                ));

        // 2. CUSTOMER 권한 확인
        if (user.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "고객만 리뷰를 작성할 수 있습니다."
            );
        }

        // 3. 주문 조회
        Order order = orderRepository
                .findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 주문입니다."
                ));

        // 4. 본인의 주문인지 확인
        if (!order.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException(
                    "본인의 주문에만 리뷰를 작성할 수 있습니다."
            );
        }

        // 5. 주문 완료 여부 확인
        if (order.getOrderStatus() != OrderStatus.ORDER_COMPLETED) {
            throw new IllegalArgumentException(
                    "주문이 완료된 이후에만 리뷰를 작성할 수 있습니다."
            );
        }

        // 6. 주문당 리뷰 한 개인지 확인
        if (reviewRepository
                .existsByOrder_OrderIdAndIsDeletedFalse(orderId)) {
            throw new IllegalArgumentException(
                    "이미 리뷰가 작성된 주문입니다."
            );
        }

        // 7. 주문의 가게 조회
        Store store = storeRepository
                .findByStoreIdAndIsDeletedFalse(order.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 가게입니다."
                ));

        // 8. Review 엔티티 생성
        Review review = new Review(
                user,
                store,
                order,
                requestDto.getRating(),
                requestDto.getContent()
        );

        // 9. DB 저장
        Review savedReview = reviewRepository.save(review);

        // 10. 응답 DTO로 변환
        return ReviewCreateResponse.of(savedReview);
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

    // 가게별 리뷰 검색 Service
    @Transactional(readOnly = true)
    public ReviewSearchResponse searchReviews(
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

        // 2. 평점이 전달되었다면 범위 확인
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException(
                    "평점은 1점부터 5점 사이여야 합니다."
            );
        }

        // 3. 정렬 방향 설정
        Sort.Direction direction;

        if ("asc".equalsIgnoreCase(sort)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }

        // 4. 페이지 객체 생성
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, "createdAt")
        );

        Page<Review> reviewPage;

        // 5. rating이 없으면 가게의 전체 리뷰 조회
        if (rating == null) {
            reviewPage = reviewRepository
                    .findAllByStore_StoreIdAndIsDeletedFalse(
                            storeId,
                            pageable
                    );
        } else {
            // 6. rating이 있으면 해당 평점의 리뷰 조회
            reviewPage = reviewRepository
                    .findAllByStore_StoreIdAndRatingAndIsDeletedFalse(
                            storeId,
                            rating,
                            pageable
                    );
        }

        // 7. Review Entity 목록을 ItemResponse 목록으로 변환
        List<ReviewSearchItemResponse> reviews = reviewPage
                .getContent()
                .stream()
                .map(ReviewSearchItemResponse::of)
                .toList();

        // 8. 목록과 페이지 정보를 응답
        return new ReviewSearchResponse(
                reviews,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isFirst(),
                reviewPage.isLast()
        );
    }

    // 리뷰 수정 Service
    @Transactional
    public ReviewUpdateResponse updateReview(
            UUID reviewId,
            ReviewUpdateRequest requestDto,
            String loginId
    ) {
        // 1. 로그인 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 사용자입니다."
                ));

        // 2. 리뷰 조회
        Review review = reviewRepository
                .findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 리뷰입니다."
                ));

        // 3. 작성자 본인인지 확인
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException(
                    "본인이 작성한 리뷰만 수정할 수 있습니다."
            );
        }

        // 4. 수정할 값이 하나라도 있는지 확인
        if (requestDto.getRating() == null
                && requestDto.getContent() == null) {
            throw new IllegalArgumentException(
                    "수정할 평점 또는 내용을 입력해 주세요."
            );
        }

        // 5. Review 엔티티 수정
        review.update(
                requestDto.getRating(),
                requestDto.getContent()
        );

        // 6. 수정 결과 반환
        return ReviewUpdateResponse.of(review);
    }

    // 리뷰 삭제 Service
    @Transactional
    public ReviewDeleteResponse deleteReview(
            UUID reviewId,
            String loginId
    ) {
        // 1. 로그인 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 사용자입니다."
                ));

        // 삭제된 리뷰도 조회하기 위해 findById 사용
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 리뷰입니다."
                ));

        // 2. 이미 삭제된 리뷰인지 확인
        if (review.isDeleted()) {
            throw new IllegalArgumentException(
                    "이미 삭제된 리뷰입니다."
            );
        }

        // 3. 리뷰 작성자 또는 MASTER인지 확인
        boolean isWriter =
                review.getUser().getUserId().equals(user.getUserId());

        boolean isMaster =
                user.getRole() == Role.MASTER;

        if (!isWriter && !isMaster) {
            throw new IllegalArgumentException(
                    "리뷰 작성자 또는 관리자만 삭제할 수 있습니다."
            );
        }

        // 4. Soft Delete
        review.softDelete(loginId);

        // 5. 삭제 결과 반환
        return ReviewDeleteResponse.of(review);
    }
}