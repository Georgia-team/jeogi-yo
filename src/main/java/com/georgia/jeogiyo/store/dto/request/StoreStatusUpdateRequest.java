package com.georgia.jeogiyo.store.dto.request;

import com.georgia.jeogiyo.store.entity.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가게 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class StoreStatusUpdateRequest {

    @Schema(description = "가게 상태", example = "OPEN")
    @NotNull(message = "가게 상태는 필수입니다.")
    private StoreStatus storeStatus;
}