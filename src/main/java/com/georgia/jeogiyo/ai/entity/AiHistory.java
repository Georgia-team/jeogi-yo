package com.georgia.jeogiyo.ai.entity;

import com.georgia.jeogiyo.global.entity.BaseEntity;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_ai_history")
public class AiHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_history_id", nullable = false, updatable = false)
    private UUID aiHistoryId;

    // N(AiHistory) : 1(User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // TODO (확장 적용)일반 AI 질의는 product_id 없이도 저장가능하도록 설계(nullable = true)
    // 상품 등록 중 AI가 실패하면 아직 Product가 저장되기 전일 수 있으니 product가 null일 수 있어야 합니다.
    // N(AiHistory) : 1(Product)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "request_text", nullable = false, columnDefinition = "TEXT")
    private String requestText;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status", nullable = false)
    private AiStatus aiStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;


    private AiHistory(User user, Product product, String requestText,
                      String responseText, String modelName,
                      AiStatus aiStatus, String errorMessage) {
        this.user = user;
        this.product = product;
        this.requestText = requestText;
        this.responseText = responseText;
        this.modelName = modelName;
        this.aiStatus = aiStatus;
        this.errorMessage = errorMessage;
    }

    // AI 요청 성공 이력 생성
    public static AiHistory success(User user, Product product, String requestText,
                                    String responseText, String modelName) {
        return new AiHistory(
                user, product, requestText, responseText, modelName, AiStatus.SUCCESS, null
        );
    }

    // AI 요청 실패 이력 생성
    public static AiHistory fail(User user, Product product, String requestText,
                                 String modelName, String errorMessage) {
        return new AiHistory(
                user, product, requestText, null, modelName, AiStatus.FAIL, errorMessage
        );
    }

}