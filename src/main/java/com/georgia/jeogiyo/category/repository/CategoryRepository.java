package com.georgia.jeogiyo.category.repository;

import com.georgia.jeogiyo.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByCategoryName(String categoryName);

    Optional<Category> findByCategoryIdAndIsDeletedFalse(UUID categoryId); // Optional = 값이 있을 수도 있고, 없을 수도 있고

    boolean existsByCategoryNameAndCategoryIdNot(String categoryName, UUID categoryId);
}
