package com.georgia.jeogiyo.order.entity;

import com.georgia.jeogiyo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(name = "road_address", length = 100, nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", length = 100)
    private String detailAddress;

    @Column(name = "zipcode", length = 10, nullable = false)
    private String zipcode;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    public Order(UUID userId, UUID storeId, UUID addressId, String roadAddress,
                 String detailAddress, String zipcode, Integer totalPrice, OrderStatus orderStatus) {
        this.userId = userId;
        this.storeId = storeId;
        this.addressId = addressId;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.zipcode = zipcode;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
}
}