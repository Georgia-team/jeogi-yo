package com.georgia.jeogiyo.order.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailResponse {

    private UUID orderId;
    private UUID storeId;
    private UUID addressId;
    private String orderStatus;
    private Integer totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItemResponse {
        private UUID productId;
        private String productName;
        private Integer quantity;
        private Integer unitPrice;
        private Integer itemTotalPrice;
    }
}