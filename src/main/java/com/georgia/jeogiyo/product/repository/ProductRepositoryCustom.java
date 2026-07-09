package com.georgia.jeogiyo.product.repository;

import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductRepositoryCustom {

    Page<Product> searchProducts(
            UUID storeId,
            UUID categoryId,
            String keyword,
            Role role,
            String loginId,
            Pageable pageable
    );
}
