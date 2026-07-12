package com.georgia.jeogiyo.order.dto.response;

import com.georgia.jeogiyo.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateResponse {

    private UUID orderId;
    private UUID storeId;
    private String address;
    private String orderStatus;
    private Integer totalPrice;
    private LocalDateTime createdAt;
}