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
public class OrderSearchResponse {

    private List<OrderListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderListItem {
        private UUID orderId;
        private UUID storeId;
        private String storeName;
        private String orderStatus;
        private Integer totalPrice;
        private LocalDateTime createdAt;
    }
}