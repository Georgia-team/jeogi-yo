package com.georgia.jeogiyo.store.controller;

import com.georgia.jeogiyo.store.dto.request.StoreCreateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreStatusUpdateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreUpdateRequest;
import com.georgia.jeogiyo.store.dto.response.StoreResponse;
import com.georgia.jeogiyo.store.dto.response.StoreSearchPageResponse;
import com.georgia.jeogiyo.store.service.StoreService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
@Tag(name = "Store", description = "가게 API")
@SecurityRequirement(name = "bearerAuth")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 등록", description = "OWNER 권한 사용자가 가게를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가게 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody StoreCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.createStore(authentication.getName(), request));
    }

    @Operation(summary = "가게 상세 조회", description = "storeId로 가게 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 조회 성공"),
            @ApiResponse(responseCode = "404", description = "가게 없음")
    })
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStore(
            @Parameter(description = "가게 ID", example = "33333333-3333-3333-3333-333333333331")
            @PathVariable UUID storeId) {
        return ResponseEntity.ok(storeService.getStore(storeId));
    }

    @Operation(summary = "가게 목록 검색", description = "카테고리, 키워드, 페이지 조건으로 가게 목록을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 목록 검색 성공")
    })
    @GetMapping
    public ResponseEntity<StoreSearchPageResponse> searchStores(
            @Parameter(description = "카테고리 ID", example = "22222222-2222-2222-2222-222222222221")
            @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "검색 키워드", example = "치킨")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 허용값 10, 30, 50", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 방향", example = "desc")
            @RequestParam(defaultValue = "desc") String sort
    ) {
        StoreSearchPageResponse response = storeService.searchStores(
                categoryId,
                keyword,
                page,
                size,
                sort
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가게 정보 수정", description = "OWNER 또는 MASTER가 가게 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게 또는 카테고리 없음")
    })
    @PatchMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
            @Parameter(description = "가게 ID", example = "33333333-3333-3333-3333-333333333331")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody StoreUpdateRequest request
    ) {
        return ResponseEntity.ok(storeService.updateStore(storeId, authentication.getName(), request));
    }

    @Operation(summary = "가게 상태 변경", description = "OWNER 또는 MASTER가 가게 영업 상태를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가게 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "폐업 상태 변경 불가 또는 요청값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게 없음")
    })
    @PatchMapping({"/{storeId}/status", "/{storeId}/storestatus"})
    public ResponseEntity<StoreResponse> updateStoreStatus(
            @Parameter(description = "가게 ID", example = "33333333-3333-3333-3333-333333333331")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody StoreStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(storeService.updateStoreStatus(storeId, authentication.getName(), request));
    }

    @Operation(summary = "가게 삭제", description = "OWNER 또는 MASTER가 가게를 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "가게 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게 없음")
    })
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(
            @Parameter(description = "가게 ID", example = "33333333-3333-3333-3333-333333333331")
            @PathVariable UUID storeId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        storeService.deleteStore(storeId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
