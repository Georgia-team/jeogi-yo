package com.georgia.jeogiyo.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "카테고리 ID", example = "22222222-2222-2222-2222-222222222221")
    private UUID categoryId;

    @Schema(description = "가게명", example = "수정된 테스트 치킨")
    private String storeName;

    @Schema(description = "가게 주소", example = "서울시 테스트구 수정로 20")
    private String address;

    @Schema(description = "가게 전화번호", example = "02-2222-2222")
    private String phone;

}