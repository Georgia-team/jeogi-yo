package com.georgia.jeogiyo.review.dto.response;

import com.georgia.jeogiyo.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewUpdateResponse {

    private final UUID reviewId;
    private final Integer rating;
    private final String content;

    public static ReviewUpdateResponse of(Review review) {
        return new ReviewUpdateResponse(
                review.getReviewId(),
                review.getRating(),
                review.getContent()
        );
    }
}