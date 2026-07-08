package com.georgia.jeogiyo.category.entity;

import com.georgia.jeogiyo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseEntity {
    // PK, 카테고리 고유 번호
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;
    // 카테고리 이름
    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    public Category(String categoryName, String createdBy) {
        this.categoryName = categoryName;
        // this.createdBy = createdBy;
        // this.createdAt = LocalDateTime.now();
    }
}
