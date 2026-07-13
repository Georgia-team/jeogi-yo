package com.georgia.jeogiyo.review.entity;

import com.georgia.jeogiyo.global.entity.BaseEntity;
import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_review")
public class Review extends BaseEntity {

    // 리뷰 고유번호
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id", nullable = false, updatable = false)
    private UUID reviewId;

    // 리뷰 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_P_REVIEW_USER"))
    private User user;

    // 리뷰 대상 가게
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_P_REVIEW_STORE"))
    private Store store;

    // 리뷰 대상 주문
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false, unique = true, foreignKey = @ForeignKey(name = "FK_P_REVIEW_ORDER"))
    private Order order;

    // 평점: 1~5
    @Column(name = "rating", nullable = false)
    private Integer rating;

    // 리뷰 내용: 10자 이상 300자 이하
    @Column(name = "content", nullable = false, length = 300)
    private String content;

    public Review(User user, Store store, Order order, Integer rating, String content) {
        validateRating(rating);
        validateContent(content);

        this.user = user;
        this.store = store;
        this.order = order;
        this.rating = rating;
        this.content = content.trim();
    }

    // 리뷰 수정
    public void update(Integer rating, String content) {
        if (rating != null) {
            validateRating(rating);
            this.rating = rating;
        }

        if (content != null) {
            validateContent(content);
            this.content = content.trim();
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "평점은 1점부터 5점 사이여야 합니다."
            );
        }
    }

    private void validateContent(String content) {
        if (content == null) {
            throw new IllegalArgumentException(
                    "리뷰 내용은 필수입니다."
            );
        }

        String trimmedContent = content.trim();

        if (trimmedContent.length() < 10
                || trimmedContent.length() > 300) {
            throw new IllegalArgumentException(
                    "리뷰 내용은 10자 이상 300자 이하로 작성해야 합니다."
            );
        }
    }
}