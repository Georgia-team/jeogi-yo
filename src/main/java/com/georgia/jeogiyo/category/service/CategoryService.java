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
import com.georgia.jeogiyo.category.dto.response.CategoryCreateResponse;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional // Service 단계에서 트랙잭션이 이루어져야함. 비즈니스 측면에서 재고(DB)와 이력(log)와의 차이가 없어야 함.
    public CategoryCreateResponse createCategory(CategoryCreateRequest requestDto, String loginId) {
        String categoryName = requestDto.getCategoryName().trim(); // didWhr rhdqor wprj

        if (categoryRepository.existsByCategoryName(categoryName)) {
            // TODO: 이미 사용중인 닉네임입니다. 409
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다.");
        }

        Category category = new Category(categoryName, loginId);

        Category savedCategory = categoryRepository.save(category);

        return CategoryCreateResponse.of(savedCategory);
    }
}
