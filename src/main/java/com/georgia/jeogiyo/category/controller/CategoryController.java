package com.georgia.jeogiyo.category.controller;

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.request.CategoryUpdateRequest;
import com.georgia.jeogiyo.category.dto.response.CategoryCreateResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryDeleteResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryReadResponse;
import com.georgia.jeogiyo.category.dto.response.CategorySearchItemResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryUpdateResponse;
import com.georgia.jeogiyo.category.service.CategoryService;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 API")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 등록
    @Operation(
            summary = "카테고리 등록",
            description = "MASTER 권한 사용자가 새로운 카테고리를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리 이름")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @PostMapping
    public CategoryCreateResponse createCategory(
            @Valid @RequestBody CategoryCreateRequest requestDto
    ) {
        return categoryService.createCategory(requestDto);
    }

    // 카테고리 상세 조회
    @Operation(
            summary = "카테고리 상세 조회",
            description = "categoryId로 삭제되지 않은 카테고리의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리 없음")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping("/{categoryId}")
    public CategoryReadResponse readCategory(
            @Parameter(
                    description = "카테고리 ID",
                    example = "22222222-2222-2222-2222-222222222221"
            )
            @PathVariable UUID categoryId
    ) {
        return categoryService.readResponse(categoryId);
    }

    // 카테고리 목록 검색
    @Operation(
            summary = "카테고리 목록 검색",
            description = "검색어, 페이지, 정렬 조건으로 삭제되지 않은 카테고리 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 크기 또는 정렬 조건"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MASTER')")
    @GetMapping
    public PageResponse<CategorySearchItemResponse> searchCategories(
            @Parameter(
                    description = "카테고리 이름 검색어. 입력하지 않으면 전체 카테고리를 조회합니다.",
                    example = "한식"
            )
            @RequestParam(required = false) String keyword,

            @Parameter(
                    description = "페이지 번호. 0부터 시작합니다.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "페이지 크기. 허용값 10, 30, 50",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int size,

            @Parameter(
                    description = "생성일 기준 정렬 방향",
                    example = "desc"
            )
            @RequestParam(defaultValue = "desc") String sort
    ) {
        return categoryService.searchCategories(
                keyword,
                page,
                size,
                sort
        );
    }

    // 카테고리 수정
    @Operation(
            summary = "카테고리 수정",
            description = "MASTER 권한 사용자가 카테고리 이름을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리 없음"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리 이름")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @PatchMapping("/{categoryId}")
    public CategoryUpdateResponse updateCategory(
            @Parameter(
                    description = "카테고리 ID",
                    example = "22222222-2222-2222-2222-222222222221"
            )
            @PathVariable UUID categoryId,

            @Valid @RequestBody CategoryUpdateRequest requestDto
    ) {
        return categoryService.updateCategory(
                categoryId,
                requestDto
        );
    }

    // 카테고리 삭제
    @Operation(
            summary = "카테고리 삭제",
            description = "MASTER 권한 사용자가 카테고리를 soft delete 처리합니다. 가게 또는 상품에서 사용 중인 카테고리는 삭제할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리 없음"),
            @ApiResponse(responseCode = "409", description = "이미 삭제되었거나 가게 또는 상품에서 사용 중인 카테고리")
    })
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    @DeleteMapping("/{categoryId}")
    public CategoryDeleteResponse deleteCategory(
            @Parameter(
                    description = "카테고리 ID",
                    example = "22222222-2222-2222-2222-222222222221"
            )
            @PathVariable UUID categoryId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String loginId = userDetails.getUsername();

        return categoryService.deleteCategory(
                categoryId,
                loginId
        );
    }
}