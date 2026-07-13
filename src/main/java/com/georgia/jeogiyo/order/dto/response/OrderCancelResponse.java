package com.georgia.jeogiyo.order.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderCancelResponse {
    private UUID orderId;
    private String orderStatus;
    private LocalDateTime canceledAt;
    private String cancelReason;
}