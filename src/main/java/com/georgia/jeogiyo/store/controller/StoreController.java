package com.georgia.jeogiyo.store.controller;

import com.georgia.jeogiyo.store.dto.request.StoreCreateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreStatusUpdateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreUpdateRequest;
import com.georgia.jeogiyo.store.dto.response.StoreResponse;
import com.georgia.jeogiyo.store.dto.response.StoreSearchPageResponse;
import com.georgia.jeogiyo.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @RequestParam String loginId, // TODO JWT
            @Valid @RequestBody StoreCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.createStore(loginId, request));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(storeService.getStore(storeId));
    }

    @GetMapping
    public ResponseEntity<StoreSearchPageResponse> searchStores(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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

    @PatchMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable UUID storeId,
            @RequestParam String loginId, // TODO JWT
            @Valid @RequestBody StoreUpdateRequest request
    ) {
        return ResponseEntity.ok(storeService.updateStore(storeId, loginId, request));
    }

    @PatchMapping({"/{storeId}/status", "/{storeId}/storestatus"})
    public ResponseEntity<StoreResponse> updateStoreStatus(
            @PathVariable UUID storeId,
            @RequestParam String loginId, // TODO JWT
            @Valid @RequestBody StoreStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(storeService.updateStoreStatus(storeId, loginId, request));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(
            @PathVariable UUID storeId,
            @RequestParam String loginId // TODO JWT
    ) {
        storeService.deleteStore(storeId, loginId);
        return ResponseEntity.noContent().build();
    }
}
