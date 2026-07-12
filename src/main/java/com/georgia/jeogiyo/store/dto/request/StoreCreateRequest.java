package com.georgia.jeogiyo.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "카테고리 ID", example = "22222222-2222-2222-2222-222222222221")
    @NotNull(message = "카테고리 ID는 필수입니다.")
    private UUID categoryId;

    @Schema(description = "가게명", example = "테스트 치킨")
    @NotBlank(message = "가게 이름은 필수입니다.")
    private String storeName;

    @Schema(description = "가게 주소", example = "서울시 테스트구 테스트로 10")
    @NotBlank(message = "가게 주소는 필수입니다.")
    private String address;

    @Schema(description = "가게 전화번호", example = "02-1234-5678")
    @NotBlank(message = "가게 연락처는 필수입니다.")
    private String phone;
}