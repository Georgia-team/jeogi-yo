package com.georgia.jeogiyo.review.service;

import com.georgia.jeogiyo.global.exception.BusinessException;
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

import static com.georgia.jeogiyo.global.exception.GlobalErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    // 리뷰 작성
    @Transactional
    public ReviewCreateResponse createReview(
            UUID orderId,
            ReviewCreateRequest requestDto,
            UserDetailsImpl userDetails
    ) {
        User loginUser = userDetails.getUser();

        // 1. 주문 조회
        Order order = orderRepository
                .findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_ORDER));

        // 2. 해당 주문의 주문자인지 확인
        if (!order.getUser().getUserId().equals(loginUser.getUserId())) {
            throw new BusinessException(FORBIDDEN_ORDER);
        }

        // 3. 주문 완료 여부 확인
        if (order.getOrderStatus() != OrderStatus.ORDER_COMPLETED) {
            throw new BusinessException(REVIEW_NOT_ALLOWED);
        }

        // 4. 주문당 리뷰 한 개만 작성 가능
        if (reviewRepository.existsByOrder_OrderIdAndIsDeletedFalse(orderId)) {
            throw new BusinessException(DUPLICATE_REVIEW);
        }

        // 5. 주문에 연결된 가게 조회
        Store store = storeRepository
                .findByStoreIdAndIsDeletedFalse(order.getStoreId())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_STORE));

        // 6. 리뷰 생성
        Review review = new Review(
                loginUser,
                store,
                order,
                requestDto.getRating(),
                requestDto.getContent()
        );

        Review savedReview = reviewRepository.save(review);

        return ReviewCreateResponse.of(savedReview);
    }

    // 리뷰 상세 조회
    @Transactional(readOnly = true)
    public ReviewReadResponse readReview(UUID reviewId) {
        Review review = reviewRepository
                .findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_REVIEW));

        return ReviewReadResponse.of(review);
    }

    // 가게별 리뷰 목록 검색
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
                .orElseThrow(() -> new BusinessException(NOT_FOUND_STORE));

        // 2. 평점 범위 확인
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BusinessException(INVALID_REVIEW_RATING);
        }

        // 3. 공통 페이징 정책 적용
        Pageable pageable = PageUtil.toPageable(
                page,
                size,
                sort
        );

        // 4. 리뷰 검색
        Page<Review> reviewPage = reviewRepository.searchReviews(
                storeId,
                rating,
                pageable
        );

        // 5. 공통 페이지 응답으로 변환
        return PageResponse.from(
                reviewPage,
                ReviewSearchItemResponse::of
        );
    }

    // 리뷰 수정
    @Transactional
    public ReviewUpdateResponse updateReview(
            UUID reviewId,
            ReviewUpdateRequest requestDto,
            UserDetailsImpl userDetails
    ) {
        User loginUser = userDetails.getUser();

        // 1. 삭제되지 않은 리뷰 조회
        Review review = reviewRepository
                .findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_REVIEW));

        // 2. 리뷰 작성자 본인인지 확인
        if (!review.getUser()
                .getUserId()
                .equals(loginUser.getUserId())) {
            throw new BusinessException(FORBIDDEN_REVIEW);
        }

        // 3. 수정할 값 존재 여부 확인
        if (requestDto.getRating() == null
                && requestDto.getContent() == null) {
            throw new BusinessException(EMPTY_REVIEW_UPDATE);
        }

        // 4. 리뷰 수정
        review.update(
                requestDto.getRating(),
                requestDto.getContent()
        );

        return ReviewUpdateResponse.of(review);
    }

    // 리뷰 삭제
    @Transactional
    public ReviewDeleteResponse deleteReview(
            UUID reviewId,
            UserDetailsImpl userDetails
    ) {
        User loginUser = userDetails.getUser();

        // 1. 삭제된 리뷰도 확인하기 위해 findById 사용
        Review review = reviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_REVIEW));

        // 2. 이미 삭제된 리뷰인지 확인
        if (review.isDeleted()) {
            throw new BusinessException(ALREADY_DELETED_REVIEW);
        }

        // 3. 리뷰 작성자 또는 MASTER인지 확인
        boolean isWriter = review.getUser()
                .getUserId()
                .equals(loginUser.getUserId());

        boolean isMaster = loginUser.getRole()
                .name()
                .equals("MASTER");

        if (!isWriter && !isMaster) {
            throw new BusinessException(FORBIDDEN_REVIEW);
        }

        // TODO: Review를 참조하는 다른 Entity가 있는지 확인

        // 4. Soft Delete
        review.softDelete(loginUser.getLoginId());

        // 5. 삭제 결과 반환
        return ReviewDeleteResponse.of(review);
    }
}