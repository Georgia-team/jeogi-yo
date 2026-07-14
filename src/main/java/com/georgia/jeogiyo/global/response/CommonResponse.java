package com.georgia.jeogiyo.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 진혜림
 * 작성일: 2026-07-14
 * 설명: success, data, message 응답 포맷 통일
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // JSON 변환 시 null인 필드 제거
public class CommonResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private CommonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, message, data);
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(true, "요청이 성공적으로 처리되었습니다.", null);
    }

    public static <T> CommonResponse<T> fail(String message) {
        return new CommonResponse<>(false, message, null);
    }
}
