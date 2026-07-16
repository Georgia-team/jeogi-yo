package com.georgia.jeogiyo.review.controller;

import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.security.UserDetailsImpl;
import com.georgia.jeogiyo.review.dto.request.ReviewCreateRequest;
import com.georgia.jeogiyo.review.dto.request.ReviewUpdateRequest;
import com.georgia.jeogiyo.review.dto.response.ReviewCreateResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewDeleteResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewReadResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewSearchItemResponse;
import com.georgia.jeogiyo.review.dto.response.ReviewUpdateResponse;
import com.georgia.jeogiyo.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Review", description = "리뷰 API")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @Operation(
            summary = "리뷰 작성",
            description = "CUSTOMER 권한 사용자가 배송 완료된 주문에 대한 리뷰를 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인의 주문이 아니거나 권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문 또는 가게 없음"),
            @ApiResponse(responseCode = "409", description = "배송이 완료되지 않았거나 이미 리뷰가 작성된 주문")
    })
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<CommonResponse<ReviewCreateResponse>> createReview(
            @Parameter(
                    description = "주문 ID",
                    example = "44444444-4444-4444-4444-444444444441"
            )
            @PathVariable UUID orderId,

            @Valid @RequestBody ReviewCreateRequest requestDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewCreateResponse response =
                reviewService.createReview(orderId, requestDto, userDetails);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("리뷰 작성 성공", response));
    }

    // 리뷰 상세 조회
    @Operation(
            summary = "리뷰 상세 조회",
            description = "reviewId로 삭제되지 않은 리뷰의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewReadResponse>> readReview(
            @Parameter(
                    description = "리뷰 ID",
                    example = "55555555-5555-5555-5555-555555555551"
            )
            @PathVariable UUID reviewId
    ) {
        ReviewReadResponse response = reviewService.readReview(reviewId);

        return ResponseEntity.ok(
                CommonResponse.success("리뷰 조회 성공", response)
        );
    }

    // 가게별 리뷰 검색
    @Operation(
            summary = "가게별 리뷰 검색",
            description = "가게 ID와 별점, 페이지, 정렬 조건으로 삭제되지 않은 리뷰 목록을 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 별점, 페이지 크기 또는 정렬 조건"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게 없음")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<CommonResponse<PageResponse<ReviewSearchItemResponse>>> searchReviews(
            @Parameter(
                    description = "가게 ID",
                    example = "33333333-3333-3333-3333-333333333331"
            )
            @PathVariable UUID storeId,

            @Parameter(
                    description = "별점 필터. 입력하지 않으면 전체 별점을 조회합니다.",
                    example = "5"
            )
            @RequestParam(required = false) Integer rating,

            @Parameter(
                    description = "페이지 번호. 0부터 시작합니다.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "페이지 크기. 허용값 10, 30, 50",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int size,

            @Parameter(
                    description = "작성일 기준 정렬 방향",
                    example = "desc"
            )
            @RequestParam(defaultValue = "desc") String sort
    ) {
        PageResponse<ReviewSearchItemResponse> response =
                reviewService.searchReviews(
                        storeId,
                        rating,
                        page,
                        size,
                        sort
                );

        return ResponseEntity.ok(
                CommonResponse.success("리뷰 목록 조회 성공", response)
        );
    }

    // 리뷰 수정
    @Operation(
            summary = "리뷰 수정",
            description = "CUSTOMER 권한의 리뷰 작성자가 자신의 리뷰를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 또는 수정할 값 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "리뷰 작성자 본인이 아님"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewUpdateResponse>> updateReview(
            @Parameter(
                    description = "리뷰 ID",
                    example = "55555555-5555-5555-5555-555555555551"
            )
            @PathVariable UUID reviewId,

            @Valid @RequestBody ReviewUpdateRequest requestDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewUpdateResponse response =
                reviewService.updateReview(reviewId, requestDto, userDetails);

        return ResponseEntity.ok(
                CommonResponse.success("리뷰 수정 성공", response)
        );
    }

    // 리뷰 삭제
    @Operation(
            summary = "리뷰 삭제",
            description = "리뷰 작성자 또는 MASTER가 리뷰를 soft delete 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "리뷰 작성자 또는 MASTER가 아님"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음"),
            @ApiResponse(responseCode = "409", description = "이미 삭제된 리뷰")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_MASTER')")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewDeleteResponse>> deleteReview(
            @Parameter(
                    description = "리뷰 ID",
                    example = "55555555-5555-5555-5555-555555555551"
            )
            @PathVariable UUID reviewId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReviewDeleteResponse response =
                reviewService.deleteReview(reviewId, userDetails);

        return ResponseEntity.ok(
                CommonResponse.success("리뷰 삭제 성공", response)
        );
    }
}