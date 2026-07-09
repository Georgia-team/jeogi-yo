package com.georgia.jeogiyo.store.dto.request;

import com.georgia.jeogiyo.store.entity.StoreStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가게 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class StoreStatusUpdateRequest {

    @NotNull(message = "가게 상태는 필수입니다.")
    private StoreStatus storeStatus;
}