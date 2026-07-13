package com.georgia.jeogiyo.category.repository;

import com.georgia.jeogiyo.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByCategoryName(String categoryName);

    Optional<Category> findByCategoryIdAndIsDeletedFalse(UUID categoryId); // Optional = 값이 있을 수도 있고, 없을 수도 있고

    boolean existsByCategoryNameAndCategoryIdNot(String categoryName, UUID categoryId);

    // 검색어 없이 삭제되지 않은 전체 카테고리 조회
    Page<Category> findAllByIsDeletedFalse(Pageable pageable);

    // 삭제되지 않은 카테고리 중 이름에 keyword가 포함된 카테고리 조회
    Page<Category> findAllByIsDeletedFalseAndCategoryNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );
}
