package com.georgia.jeogiyo.order.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderSearchResponse {
    private UUID orderId;
    private UUID storeId;
    private String storeName;
    private String orderStatus;
    private Integer totalPrice;
    private LocalDateTime createdAt;
}