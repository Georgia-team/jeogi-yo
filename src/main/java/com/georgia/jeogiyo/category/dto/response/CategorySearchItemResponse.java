package com.georgia.jeogiyo.category.dto.response;

import com.georgia.jeogiyo.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategorySearchItemResponse {

    private final UUID categoryId;

    private final String categoryName;

    public static CategorySearchItemResponse of(Category category) {
        return new CategorySearchItemResponse(
                category.getCategoryId(),
                category.getCategoryName()
        );
    }
}