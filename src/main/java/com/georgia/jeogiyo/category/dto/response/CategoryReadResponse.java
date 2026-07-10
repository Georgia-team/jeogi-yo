package com.georgia.jeogiyo.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategoryReadResponse {

    private final UUID categoryId;

    private final String categoryName;
}
