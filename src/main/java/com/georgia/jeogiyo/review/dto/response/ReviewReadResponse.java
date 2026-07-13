package com.georgia.jeogiyo.review.dto.response;

import com.georgia.jeogiyo.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewReadResponse {

    private final UUID reviewId;
    private final UUID orderId;
    private final UUID storeId;
    private final UUID userId;
    private final String nickname;
    private final Integer rating;
    private final String content;

    public static ReviewReadResponse of(Review review) {
        return new ReviewReadResponse(
                review.getReviewId(),
                review.getOrder().getOrderId(),
                review.getStore().getStoreId(),
                review.getUser().getUserId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent()
        );
    }
}