package com.georgia.jeogiyo.review.dto.response;

import com.georgia.jeogiyo.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewSearchItemResponse {

    private final UUID reviewId;
    private final UUID userId;
    private final String nickname;
    private final Integer rating;
    private final String content;
    private final LocalDateTime createdAt;

    public static ReviewSearchItemResponse of(Review review) {
        return new ReviewSearchItemResponse(
                review.getReviewId(),
                review.getUser().getUserId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}