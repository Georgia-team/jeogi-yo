package com.georgia.jeogiyo.category.service;

/*
 * 기능의 실제 동작과 규칙 *
 *
 * Entity 생성
 * DB 저장
 * CategoryCreate Response로 변환 (외부에 Entity 노출 X, 필요한 값만 골라서)
 * Controller에 반환
 */

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.request.CategoryUpdateRequest;
import com.georgia.jeogiyo.category.dto.response.CategoryCreateResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryDeleteResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryReadResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryUpdateResponse;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // 카테고리 생성 Service
    @Transactional // Service 단계에서 트랙잭션이 이루어져야함. 비즈니스 측면에서 재고(DB)와 이력(log)와의 차이가 없어야 함.
    public CategoryCreateResponse createCategory(CategoryCreateRequest requestDto, String loginId) {
        String categoryName = requestDto.getCategoryName().trim(); // didWhr rhdqor wprj

        if (categoryRepository.existsByCategoryName(categoryName)) {
            // TODO: 이미 사용중인 닉네임입니다. 409
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다.");
        }

        Category category = new Category(categoryName);

        Category savedCategory = categoryRepository.save(category);

        return CategoryCreateResponse.of(savedCategory);
    }

    // 카테고리 상세 조회 API
    @Transactional(readOnly = true)
    public CategoryReadResponse readResponse(UUID categoryId) {
        Category category = categoryRepository
                .findByCategoryIdAndIsDeletedFalse(categoryId)
                .orElseThrow( () ->
                    new IllegalArgumentException("존재하지 않는 카테고리입니다.")
                );
        return new CategoryReadResponse(
                    category.getCategoryId(),
                    category.getCategoryName()
                );
    }

    // 카테고리 수정 API
    @Transactional
    public CategoryUpdateResponse updateCategory(UUID categoryId, CategoryUpdateRequest requestDto, String loginId) {
        String categoryName = requestDto.getCategoryName().trim();

        // 1. 수정할 카테고리 조회
        Category category = categoryRepository
                .findByCategoryIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 카테고리입니다."
                        )
                );

        // 2. 자기 자신을 제외한 이름 중복 검사
        if (categoryRepository
                .existsByCategoryNameAndCategoryIdNot(
                        categoryName,
                        categoryId
                )) {
            // TODO: 409 Conflict 예외로 변경
            throw new IllegalArgumentException(
                    "이미 존재하는 카테고리 이름입니다."
            );
        }

        // 3. Category 엔티티의 update() 메서드 호출
        category.update(categoryName, loginId);

        // 4. 수정된 엔티티를 응답 DTO로 변환
        return CategoryUpdateResponse.of(category);
    }

    // 카테고리 삭제 API
    @Transactional
    public CategoryDeleteResponse deleteCategory(UUID categoryId, String loginId) {
        // 삭제된 카테고리도 조회하기 위해 findById 사용
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 카테고리입니다."
                        )
                );

        // 이미 삭제된 카테고리인지 확인
        if (category.isDeleted()) {
            // TODO: 409 Conflict
            throw new IllegalArgumentException(
                    "이미 삭제된 카테고리입니다."
            );
        }

        category.softDelete(loginId);

        return CategoryDeleteResponse.of(category);
    }
}
