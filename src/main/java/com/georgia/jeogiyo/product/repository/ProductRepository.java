package com.georgia.jeogiyo.product.repository;

import com.georgia.jeogiyo.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
 QueryDSL 적용: storeId, categoryId, keyword, isDeleted=false 조건 검색 구현
*/
public interface ProductRepository extends JpaRepository<Product, UUID>, ProductRepositoryCustom {
    Optional<Product> findByProductIdAndIsDeletedFalse(UUID productId);
}


