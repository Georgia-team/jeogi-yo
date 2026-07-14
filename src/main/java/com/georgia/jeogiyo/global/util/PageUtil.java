package com.georgia.jeogiyo.global.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 진혜림
 * 작성일: 2026-07-13
 * 설명: 페이징 정책 통일
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */
public class PageUtil {

    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private PageUtil() {
        // 정적 메서드만 사용하는 유틸 클래스이므로 인스턴스 생성을 막습니다.
    }

    public static Pageable toPageable(int page, int size, String sort) {
        return toPageable(page, size, sort, DEFAULT_SORT_FIELD);
    }

    public static Pageable toPageable(int page, int size, String sort, String sortField) {
        int validatedPage = Math.max(page, 0); // todo 어떻게 하면 좋은지? 음수 등 잘못들어오면 0으로 처리 -> 예외 처리 할 수 도 있고
        int validatedSize = validateSize(size);
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(validatedPage, validatedSize, Sort.by(direction, sortField));
    }

    private static int validateSize(int size) {
        return (size == 10 || size == 30 || size == 50) ? size : DEFAULT_SIZE;
    }
}
