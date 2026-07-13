package com.georgia.jeogiyo.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewCreateRequest {

    // 평점: 1~5
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private Integer rating;

    // 리뷰 내용: 10자 이상 300자 이하
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 10, max = 300, message = "리뷰 내용은 10자 이상 300자 이하로 작성해야 합니다.")
    private String content;

    public ReviewCreateRequest(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}