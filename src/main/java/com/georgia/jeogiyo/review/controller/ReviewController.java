package com.georgia.jeogiyo.review.controller;

import com.georgia.jeogiyo.review.dto.request.ReviewCreateRequest;
import com.georgia.jeogiyo.review.dto.request.ReviewUpdateRequest;
import com.georgia.jeogiyo.review.dto.response.ReviewCreateResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewDeleteResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewReadResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewSearchResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewUpdateResponse;
import com.georgia.jeogiyo.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/orders/{orderId}/reviews")
    public ReviewCreateResponse createReview(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReviewCreateRequest requestDto,
            @RequestHeader("loginId") String loginId
    ) {
        return reviewService.createReview(
                orderId,
                requestDto,
                loginId
        );
    }

    // 리뷰 상세 조회
    @GetMapping("/reviews/{reviewId}")
    public ReviewReadResponse readReview(
            @PathVariable UUID reviewId
    ) {
        return reviewService.readReview(reviewId);
    }

    // 가게별 리뷰 검색
    @GetMapping("/stores/{storeId}/reviews")
    public ReviewSearchResponse searchReviews(
            @PathVariable UUID storeId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        return reviewService.searchReviews(
                storeId,
                rating,
                page,
                size,
                sort
        );
    }

    // 리뷰 수정
    @PatchMapping("/reviews/{reviewId}")
    public ReviewUpdateResponse updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest requestDto,
            @RequestHeader("loginId") String loginId
    ) {
        return reviewService.updateReview(
                reviewId,
                requestDto,
                loginId
        );
    }

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ReviewDeleteResponse deleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader("loginId") String loginId
    ) {
        return reviewService.deleteReview(
                reviewId,
                loginId
        );
    }
}