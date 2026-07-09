package com.georgia.jeogiyo.product.controller;

import com.georgia.jeogiyo.product.dto.request.ProductCreateRequest;
import com.georgia.jeogiyo.product.dto.request.ProductUpdateRequest;
import com.georgia.jeogiyo.product.dto.response.ProductResponse;
import com.georgia.jeogiyo.product.dto.response.ProductSearchPageResponse;
import com.georgia.jeogiyo.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController { // TODO JWT 적용 후 OWNER/MASTER 권한 검증

    private final ProductService productService;

    @PostMapping("/stores/{storeId}/products")
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable UUID storeId,
            @RequestParam String loginId, // TODO JWT
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.createProduct(storeId, loginId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable UUID productId,
            @RequestParam String loginId // TODO JWT
    ) {
        ProductResponse response = productService.getProduct(productId, loginId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stores/{storeId}/products")
    public ResponseEntity<ProductSearchPageResponse> searchProducts(
            @PathVariable UUID storeId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam String loginId // TODO JWT
    ) {
        ProductSearchPageResponse response = productService.searchProducts(
                storeId,
                categoryId,
                keyword,
                page,
                size,
                sort,
                loginId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @RequestParam String loginId, // TODO JWT
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productService.updateProduct(productId, loginId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID productId,
            @RequestParam String loginId // TODO JWT
    ) {
        productService.deleteProduct(productId, loginId); // TODO JWT 적용 후 로그인 사용자의 loginId 전달

        return ResponseEntity.noContent().build();
    }
}
