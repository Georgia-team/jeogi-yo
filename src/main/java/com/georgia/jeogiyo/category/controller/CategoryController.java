package com.georgia.jeogiyo.category.controller;

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.request.CategoryUpdateRequest;
import com.georgia.jeogiyo.category.dto.response.*;
import com.georgia.jeogiyo.category.service.CategoryService;
import com.georgia.jeogiyo.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController // Spring Annotation, @Controller + @ResponseBody
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor // final 필드와 @NonNull 필드를 매개변수로 받는 생성자 자동 생성
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @PostMapping
    public CategoryCreateResponse createCategory(
            @Valid @RequestBody CategoryCreateRequest requestDto
    ) {
        return categoryService.createCategory(requestDto);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/{categoryId}")
    public CategoryReadResponse readCategory(
            @PathVariable UUID categoryId
    ) {
        return categoryService.readResponse(categoryId);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping
    public CategorySearchResponse searchCategories(
            @RequestParam(required = false) String keyword, // 키워드 없으면 전체 카테고리 조회
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        return categoryService.searchCategories(keyword, page, size, sort);
    }

    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @PatchMapping("/{categoryId}")
    public CategoryUpdateResponse updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest requestDto
    ) {
        return categoryService.updateCategory(categoryId, requestDto);
    }

    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @DeleteMapping("/{categoryId}")
    public CategoryDeleteResponse deleteCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String loginId = userDetails.getUsername();

        return categoryService.deleteCategory(categoryId, loginId);
    }

}
