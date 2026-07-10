package com.georgia.jeogiyo.category.dto.response;

import com.georgia.jeogiyo.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategoryUpdateResponse {

    private final UUID categoryId;

    private final String categoryName;

    public static CategoryUpdateResponse of(Category category) {
        return new CategoryUpdateResponse(
                category.getCategoryId(),
                category.getCategoryName()
        );
    }
}
