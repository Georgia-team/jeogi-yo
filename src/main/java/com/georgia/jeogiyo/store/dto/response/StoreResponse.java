package com.georgia.jeogiyo.store.dto.response;

import com.georgia.jeogiyo.store.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 가게 상세 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class StoreResponse {

    private UUID storeId;

    private UUID ownerId;

    private UUID categoryId;

    private String categoryName;

    private String storeName;

    private String address;

    private String phone;

    private StoreStatus storeStatus;

    private Integer reviewCount;

    private Double averageRating;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String updatedBy;

    private Boolean isDeleted;
}
