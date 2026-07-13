package com.georgia.jeogiyo.review.dto.response;

import com.georgia.jeogiyo.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewDeleteResponse {

    private final UUID reviewId;
    private final boolean isDeleted;
    private final LocalDateTime deletedAt;
    private final String deletedBy;

    public static ReviewDeleteResponse of(Review review) {
        return new ReviewDeleteResponse(
                review.getReviewId(),
                review.isDeleted(),
                review.getDeletedAt(),
                review.getDeletedBy()
        );
    }
}