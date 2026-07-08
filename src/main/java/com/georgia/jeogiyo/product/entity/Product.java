package com.georgia.jeogiyo.product.entity;

import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.global.entity.BaseEntity;
import com.georgia.jeogiyo.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    // N(Product) : 1(Store)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // N(Product) : 1(Category)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;


    // 상품 정보 수정
    public void update(Category category, String productName, String description,
                       Integer price, Integer stock, Boolean isHidden) {
        this.category = category;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.isHidden = isHidden;
    }

    // 상품 숨김 처리
    public void hide() {
        this.isHidden = true;
    }

    // 상품 숨김 해제
    public void show() {
        this.isHidden = false;
    }

    // AI 생성 설명 적용
    public void updateDescription(String description) {
        this.description = description;
    }

    /* 아래는 OrderItem에서 상품을 주문할 때 Product를 참조하므로,
   주문 Service에서 이 메서드 사용 가능성을 고려하여 미리 설계
   TODO 주문 기능과 연결시 재점검 및 수정 예정
    */

    // 주문 가능 여부 확인
    public boolean isOrderable() {
        return !this.isHidden && this.stock > 0;
    }

    // 재고 차감
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
        }

        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        this.stock -= quantity;
    }

    // 재고 복구 TODO 필요여부 개발 완성도 보면서 다시 점검
    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("복구 수량은 1개 이상이어야 합니다.");
        }

        this.stock += quantity;
    }


}