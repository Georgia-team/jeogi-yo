package com.georgia.jeogiyo.category.repository;

import com.georgia.jeogiyo.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
