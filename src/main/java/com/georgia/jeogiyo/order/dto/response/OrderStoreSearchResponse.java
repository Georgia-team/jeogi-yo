package com.georgia.jeogiyo.order.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderStoreSearchResponse {
    private UUID orderId;
    private String customerName;
    private String orderStatus;
    private Integer totalPrice;
    private LocalDateTime createdAt;
}