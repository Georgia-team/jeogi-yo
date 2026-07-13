package com.georgia.jeogiyo.category.controller;

/*
 * 요청과 응답 처리 *
 *
 *
 */

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.request.CategoryUpdateRequest;
import com.georgia.jeogiyo.category.dto.response.*;
import com.georgia.jeogiyo.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController // Spring Annotation, @Controller + @ResponseBody
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor // final 필드와 @NonNull 필드를 매개변수로 받는 생성자 자동 생성
public class CategoryController {

    private final CategoryService categoryService;

    // TODO: 권한 관련 처리
    @PostMapping
    public CategoryCreateResponse createCategory(
            @Valid @RequestBody CategoryCreateRequest requestDto,
            @RequestHeader("loginId") String loginId
    ) {
        return categoryService.createCategory(requestDto, loginId);
    }

    @GetMapping("/{categoryId}")
    public CategoryReadResponse readCategory(
            @PathVariable UUID categoryId
    ) {
        return categoryService.readResponse(categoryId);
    }

    @GetMapping
    public CategorySearchResponse searchCategories(
            @RequestParam(required = false) String keyword, // 키워드 없으면 전체 카테고리 조회
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        return categoryService.searchCategories(keyword, page, size, sort);
    }

    // TODO: 권한 관련 처리
    @PatchMapping("/{categoryId}")
    public CategoryUpdateResponse updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest requestDto,
            @RequestHeader("loginId") String loginId
    ) {
        return categoryService.updateCategory(categoryId, requestDto, loginId);
    }

    @DeleteMapping("/{categoryId}")
    public CategoryDeleteResponse deleteCategory(
            @PathVariable UUID categoryId,
            @RequestHeader("loginId") String loginId
    ) {
        return categoryService.deleteCategory(categoryId, loginId);
    }

}
