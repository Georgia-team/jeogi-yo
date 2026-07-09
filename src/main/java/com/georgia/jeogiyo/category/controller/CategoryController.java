package com.georgia.jeogiyo.category.controller;

/*
 * 요청과 응답 처리 *
 *
 *
 */

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.response.CategoryCreateResponse;
import com.georgia.jeogiyo.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // Spring Annotation, @Controller + @ResponseBody
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor // final 필드와 @NonNull 필드를 매개변수로 받는 생성자 자동 생성
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public CategoryCreateResponse createCategory(@RequestBody CategoryCreateRequest requestDto, @RequestHeader("loginId") String loginId) {
        return categoryService.createCategory(requestDto, loginId);
    }

}
