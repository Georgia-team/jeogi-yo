package com.georgia.jeogiyo.review.dto.response;

import com.georgia.jeogiyo.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewCreateResponse {

    private final UUID reviewId;
    private final UUID orderId;
    private final UUID storeId;
    private final UUID userId;
    private final Integer rating;
    private final String content;

    public static ReviewCreateResponse of(Review review) {
        return new ReviewCreateResponse(
                review.getReviewId(),
                review.getOrder().getOrderId(),
                review.getStore().getStoreId(),
                review.getUser().getUserId(),
                review.getRating(),
                review.getContent()
        );
    }
}