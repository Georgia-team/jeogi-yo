package com.georgia.jeogiyo.order.dto.request;

import com.georgia.jeogiyo.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull
    private OrderStatus orderStatus;
}