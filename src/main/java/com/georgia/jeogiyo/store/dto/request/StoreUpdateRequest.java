package com.georgia.jeogiyo.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 가게 정보 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class StoreUpdateRequest {

    private UUID categoryId;

    private String storeName;

    private String address;

    private String phone;

}