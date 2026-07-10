package com.georgia.jeogiyo.category.dto.response;

import com.georgia.jeogiyo.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategoryDeleteResponse {

    private final UUID categoryId;

    public static CategoryDeleteResponse of(Category category) {
        return new CategoryDeleteResponse(
                category.getCategoryId()
        );
    }
}
