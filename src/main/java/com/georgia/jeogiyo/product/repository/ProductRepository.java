package com.georgia.jeogiyo.product.repository;

import com.georgia.jeogiyo.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}