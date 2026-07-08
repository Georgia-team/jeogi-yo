package com.georgia.jeogiyo.order.entity;

public enum OrderStatus {
    ORDER_REQUESTED,   // 주문요청
    ORDER_ACCEPTED,    // 주문수락
    ORDER_REJECTED,    // 주문거절
    COOKING_COMPLETED, // 조리완료
    DELIVERED,         // 배송수령
    DELIVERY_COMPLETED,// 배송완료
    ORDER_COMPLETED    // 주문완료
}