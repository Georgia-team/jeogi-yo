package com.georgia.jeogiyo.store.dto.response;

import com.georgia.jeogiyo.store.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 가게 목록 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class StoreSearchResponse {

    private UUID storeId;

    private UUID categoryId;

    private String categoryName;

    private String storeName;

    private String address;

    private StoreStatus storeStatus;

    private Double averageRating;
}