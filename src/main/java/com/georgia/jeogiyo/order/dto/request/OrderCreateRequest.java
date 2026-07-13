package com.georgia.jeogiyo.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateRequest {
    @NotNull
    private UUID storeId;
    @NotNull
    private UUID addressId;
    @NotNull
    private List<OrderItemRequest> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItemRequest {
        @NotNull
        private UUID productId;
        @NotNull
        private Integer quantity;

    }


}

