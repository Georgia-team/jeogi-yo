package com.georgia.jeogiyo.category.service;

/*
 * 기능의 실제 동작과 규칙
 *
 * Entity 생성
 * DB 저장
 * Response DTO로 변환
 * 외부에 Entity를 직접 노출하지 않음
 */

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.request.CategoryUpdateRequest;
import com.georgia.jeogiyo.category.dto.response.CategoryCreateResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryDeleteResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryReadResponse;
import com.georgia.jeogiyo.category.dto.response.CategorySearchItemResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryUpdateResponse;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.util.PageUtil;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import static com.georgia.jeogiyo.global.exception.GlobalErrorCode.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    // 카테고리 생성
    @Transactional
    public CategoryCreateResponse createCategory(
            CategoryCreateRequest requestDto
    ) {
        String categoryName = requestDto
                .getCategoryName()
                .trim();

        // 삭제된 카테고리 이름을 포함하여 중복 검사
        if (categoryRepository.existsByCategoryName(categoryName)) {
            throw new BusinessException(DUPLICATE_CATEGORY_NAME);
        }

        Category category = new Category(categoryName);

        Category savedCategory =
                categoryRepository.save(category);

        return CategoryCreateResponse.of(savedCategory);
    }

    // 카테고리 상세 조회
    @Transactional(readOnly = true)
    public CategoryReadResponse readResponse(UUID categoryId) {
        Category category = categoryRepository
                .findByCategoryIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() ->
                        new BusinessException(NOT_FOUND_CATEGORY)
                );

        return new CategoryReadResponse(
                category.getCategoryId(),
                category.getCategoryName()
        );
    }

    // 카테고리 목록 및 검색
    @Transactional(readOnly = true)
    public PageResponse<CategorySearchItemResponse> searchCategories(
            String keyword,
            int page,
            int size,
            String sort
    ) {
        // 1. 공통 페이징 정책 적용
        Pageable pageable = PageUtil.toPageable(
                page,
                size,
                sort
        );

        // 2. 검색어 앞뒤 공백 제거
        String trimmedKeyword =
                keyword == null
                        ? null
                        : keyword.trim();

        Page<Category> categoryPage;

        // 3. 검색어가 없으면 전체 조회
        if (trimmedKeyword == null || trimmedKeyword.isBlank()) {
            categoryPage =
                    categoryRepository.findAllByIsDeletedFalse(
                            pageable
                    );
        } else {
            // 4. 검색어가 있으면 카테고리 이름 검색
            categoryPage =
                    categoryRepository
                            .findAllByIsDeletedFalseAndCategoryNameContainingIgnoreCase(
                                    trimmedKeyword,
                                    pageable
                            );
        }

        // 5. Page<Category>를 공통 응답 DTO로 변환
        return PageResponse.from(
                categoryPage,
                CategorySearchItemResponse::of
        );
    }

    // 카테고리 수정
    @Transactional
    public CategoryUpdateResponse updateCategory(
            UUID categoryId,
            CategoryUpdateRequest requestDto
    ) {
        String categoryName = requestDto
                .getCategoryName()
                .trim();

        // 1. 삭제되지 않은 카테고리 조회
        Category category = categoryRepository
                .findByCategoryIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() ->
                        new BusinessException(NOT_FOUND_CATEGORY)
                );

        // 2. 자기 자신을 제외한 이름 중복 검사
        // 삭제된 카테고리 이름도 중복으로 검사됨
        if (categoryRepository
                .existsByCategoryNameAndCategoryIdNot(
                        categoryName,
                        categoryId
                )) {
            throw new BusinessException(DUPLICATE_CATEGORY_NAME);
        }

        // 3. 엔티티 수정
        category.update(categoryName);

        // 4. 응답 DTO 변환
        return CategoryUpdateResponse.of(category);
    }

    // 카테고리 삭제
    @Transactional
    public CategoryDeleteResponse deleteCategory(
            UUID categoryId,
            String loginId
    ) {
        // 1. 삭제된 카테고리도 확인하기 위해 findById 사용
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new BusinessException(NOT_FOUND_CATEGORY)
                );

        // 2. 이미 삭제된 카테고리인지 확인
        if (category.isDeleted()) {
            throw new BusinessException(ALREADY_DELETED_CATEGORY);
        }

        // 3. 가게에서 사용 중인지 확인
        boolean usedByStore = storeRepository
                .existsByCategory_CategoryIdAndIsDeletedFalse(
                        categoryId
                );

        // 4. 상품에서 사용 중인지 확인
        boolean usedByProduct = productRepository
                .existsByCategory_CategoryIdAndIsDeletedFalse(
                        categoryId
                );

        // 5. 가게 또는 상품에서 사용 중이면 삭제 불가
        if (usedByStore || usedByProduct) {
            throw new BusinessException(CATEGORY_IN_USE);
        }

        // 6. Soft Delete
        category.softDelete(loginId);

        // 7. 삭제 결과 반환
        return CategoryDeleteResponse.of(category);
    }
}