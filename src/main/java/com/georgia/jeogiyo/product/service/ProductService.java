package com.georgia.jeogiyo.product.service;

import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.product.dto.request.ProductCreateRequest;
import com.georgia.jeogiyo.product.dto.request.ProductUpdateRequest;
import com.georgia.jeogiyo.product.dto.response.ProductResponse;
import com.georgia.jeogiyo.product.dto.response.ProductSearchResponse;

import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(UUID storeId, String loginId, ProductCreateRequest request);

    ProductResponse getProduct(UUID productId, String loginId);

    PageResponse<ProductSearchResponse> searchProducts(
            UUID storeId,
            UUID categoryId,
            String keyword,
            int page,
            int size,
            String sort,
            String loginId
    );

    ProductResponse updateProduct(UUID productId, String loginId, ProductUpdateRequest request);

    void deleteProduct(UUID productId, String loginId);

}
