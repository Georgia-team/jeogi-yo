package com.georgia.jeogiyo.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 가게 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
public class StoreCreateRequest {

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private UUID categoryId;

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String storeName;

    @NotBlank(message = "가게 주소는 필수입니다.")
    private String address;

    @NotBlank(message = "가게 연락처는 필수입니다.")
    private String phone;
}