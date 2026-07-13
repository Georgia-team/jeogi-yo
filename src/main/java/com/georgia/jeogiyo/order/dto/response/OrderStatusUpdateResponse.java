package com.georgia.jeogiyo.order.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusUpdateResponse {
    private UUID orderId;
    private String orderStatus;
    private LocalDateTime updatedAt;
}