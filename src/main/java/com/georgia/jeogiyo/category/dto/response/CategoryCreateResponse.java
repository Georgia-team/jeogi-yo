package com.georgia.jeogiyo.category.dto.response;

import com.georgia.jeogiyo.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategoryCreateResponse {
    private final UUID categoryId;

    private final String categoryName;

    private final LocalDateTime createdAt; // 생성 시간

    private final Boolean isDeleted; // 삭제 여부(생성 시 default: false)

    public static CategoryCreateResponse of(Category category) {
        return new CategoryCreateResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCreatedAt(),
                category.isDeleted()
        );
    }
}
