package com.georgia.jeogiyo.review.controller;

import com.georgia.jeogiyo.global.security.UserDetailsImpl;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/orders/{orderId}/reviews")
    public ReviewCreateResponse createReview(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReviewCreateRequest requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return reviewService.createReview(
                orderId,
                requestDto,
                userDetails
        );
    }

    // 리뷰 상세 조회
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/reviews/{reviewId}")
    public ReviewReadResponse readReview(
            @PathVariable UUID reviewId
    ) {
        return reviewService.readReview(reviewId);
    }

    // 가게별 리뷰 검색
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/stores/{storeId}/reviews")
    public ReviewSearchResponse searchReviews(
            @PathVariable UUID storeId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        return reviewService.searchReviews(storeId, rating, page, size, sort);
    }

    // 리뷰 수정
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PatchMapping("/reviews/{reviewId}")
    public ReviewUpdateResponse updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return reviewService.updateReview(reviewId, requestDto, userDetails);
    }

    // 리뷰 삭제
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_MASTER')")
    @DeleteMapping("/reviews/{reviewId}")
    public ReviewDeleteResponse deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return reviewService.deleteReview(reviewId, userDetails);
    }
}