package com.georgia.jeogiyo.product.controller;

import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.product.dto.request.ProductCreateRequest;
import com.georgia.jeogiyo.product.dto.request.ProductUpdateRequest;
import com.georgia.jeogiyo.product.dto.response.ProductResponse;
import com.georgia.jeogiyo.product.dto.response.ProductSearchResponse;
import com.georgia.jeogiyo.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Product", description = "상품 API")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록", description = "OWNER가 본인 가게에 상품을 등록합니다. AI 설명 사용도 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PostMapping("/stores/{storeId}/products")
    public ResponseEntity<CommonResponse<ProductResponse>> createProduct(
            @Parameter(description = "가게 ID", example = "33333333-3333-3333-3333-333333333331")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductCreateRequest request
    ) {
        String loginId = userDetails.getUsername();
        ProductResponse response = productService.createProduct(storeId, loginId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("상품 등록 성공", response));
    }

    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다. 숨김 상품은 권한에 따라 조회가 제한됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 조회 성공"),
            @ApiResponse(responseCode = "403", description = "숨김 상품 조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> getProduct(
            @Parameter(description = "상품 ID", example = "44444444-4444-4444-4444-444444444441")
            @PathVariable UUID productId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginId = userDetails.getUsername();
        ProductResponse response = productService.getProduct(productId, loginId);
        return ResponseEntity.ok(CommonResponse.success("상품 조회 성공", response));
    }

    @Operation(summary = "상품 목록 검색", description = "가게별 상품 목록을 카테고리, 키워드, 페이지 조건으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 목록 검색 성공")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/stores/{storeId}/products")
    public ResponseEntity<CommonResponse<PageResponse<ProductSearchResponse>>> searchProducts(
            @Parameter(description = "가게 ID", example = "33333333-3333-3333-3333-333333333331")
            @PathVariable UUID storeId,
            @Parameter(description = "카테고리 ID", example = "22222222-2222-2222-2222-222222222221")
            @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "검색 키워드", example = "치킨")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 허용값 10, 30, 50", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 방향", example = "desc")
            @RequestParam(defaultValue = "desc") String sort,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginId = userDetails.getUsername();
        PageResponse<ProductSearchResponse> response = productService.searchProducts(
                storeId,
                categoryId,
                keyword,
                page,
                size,
                sort,
                loginId
        );
        return ResponseEntity.ok(CommonResponse.success("상품 목록 조회 성공", response));
    }

    @Operation(summary = "상품 수정", description = "OWNER 또는 MASTER가 상품 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "상품 또는 카테고리 없음")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MASTER')")
    @PatchMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> updateProduct(
            @Parameter(description = "상품 ID", example = "44444444-4444-4444-4444-444444444441")
            @PathVariable UUID productId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        String loginId = userDetails.getUsername();
        ProductResponse response = productService.updateProduct(productId, loginId, request);
        return ResponseEntity.ok(CommonResponse.success("상품 수정 성공", response));
    }

    @Operation(summary = "상품 삭제", description = "OWNER 또는 MASTER가 상품을 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MASTER')")
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(
            @Parameter(description = "상품 ID", example = "44444444-4444-4444-4444-444444444441")
            @PathVariable UUID productId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginId = userDetails.getUsername();
        productService.deleteProduct(productId, loginId);
        return ResponseEntity.ok(CommonResponse.<Void>success("상품 삭제 성공", null));
    }
}
