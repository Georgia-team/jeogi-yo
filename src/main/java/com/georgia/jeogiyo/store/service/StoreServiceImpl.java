package com.georgia.jeogiyo.store.service;

import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import com.georgia.jeogiyo.store.dto.request.StoreCreateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreStatusUpdateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreUpdateRequest;
import com.georgia.jeogiyo.store.dto.response.StoreResponse;
import com.georgia.jeogiyo.store.dto.response.StoreSearchPageResponse;
import com.georgia.jeogiyo.store.dto.response.StoreSearchResponse;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreServiceImpl implements StoreService {
    // TODO JWT 적용 후 OWNER/MASTER 권한 검증
    // TODO OWNER는 본인 가게만 처리 가능하도록 검증

    private final StoreRepository storeRepository;
    private final UserFinder userFinder;
    private final CategoryRepository categoryRepository;

    @Override
    public StoreResponse createStore(String loginId, StoreCreateRequest request) {
        User owner = userFinder.getUserByLoginId(loginId);

        if (owner.getRole() != Role.OWNER) {
            throw new IllegalArgumentException("OWNER만 가게를 등록할 수 있습니다.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        Store store = new Store(
                owner,
                category,
                request.getStoreName(),
                request.getAddress(),
                request.getPhone()
        );

        Store savedStore = storeRepository.save(store);

        return toResponse(savedStore);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStore(UUID storeId) {
        Store store = findStore(storeId);
        return toResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreSearchPageResponse searchStores(
            UUID categoryId,
            String keyword,
            int page,
            int size,
            String sort
    ) {
        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        }

        Pageable pageable = createPageable(page, size, sort);
        Page<Store> storePage = storeRepository.searchStores(categoryId, keyword, pageable);

        return StoreSearchPageResponse.builder()
                .content(storePage.getContent().stream()
                        .map(this::toSearchResponse)
                        .toList())
                .page(storePage.getNumber())
                .size(storePage.getSize())
                .totalElements(storePage.getTotalElements())
                .totalPages(storePage.getTotalPages())
                .build();
    }

    @Override
    public StoreResponse updateStore(UUID storeId, String loginId, StoreUpdateRequest request) {
        User user = userFinder.getUserByLoginId(loginId);
        Store store = findStore(storeId);

        validateOwnerOrMaster(user, store);

        Category category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."))
                : null;

        store.update(
                category,
                request.getStoreName(),
                request.getAddress(),
                request.getPhone()
        );

        return toResponse(store);
    }

    @Override
    public StoreResponse updateStoreStatus(UUID storeId, String loginId, StoreStatusUpdateRequest request) {
        User user = userFinder.getUserByLoginId(loginId);
        Store store = findStore(storeId);

        validateOwnerOrMaster(user, store);

        store.changeStatus(request.getStoreStatus());
        return toResponse(store);
    }

    @Override
    public void deleteStore(UUID storeId, String loginId) {
        User user = userFinder.getUserByLoginId(loginId);
        Store store = findStore(storeId);

        validateOwnerOrMaster(user, store);

        store.softDelete(loginId);
    }

    private Store findStore(UUID storeId) {
        return storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
    }

    private void validateOwnerOrMaster(User user, Store store) {
        boolean isMaster = user.getRole() == Role.MASTER;
        boolean isOwnerOfStore = user.getRole() == Role.OWNER
                && store.getOwner().getLoginId().equals(user.getLoginId());

        if (!isMaster && !isOwnerOfStore) {
            throw new IllegalArgumentException("본인 가게만 처리할 수 있습니다.");
        }
    }

    private Pageable createPageable(int page, int size, String sort) {
        int validatedSize = (size == 10 || size == 30 || size == 50) ? size : 10;
        Sort.Direction direction = "asc".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                Math.max(page, 0),
                validatedSize,
                Sort.by(direction, "createdAt")
        );
    }

    private StoreResponse toResponse(Store store) {
        return StoreResponse.builder()
                .storeId(store.getStoreId())
                .ownerId(store.getOwner().getUserId())
                .categoryId(store.getCategory().getCategoryId())
                .categoryName(store.getCategory().getCategoryName())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .storeStatus(store.getStoreStatus())
                .createdAt(store.getCreatedAt())
                .isDeleted(store.isDeleted())
                .build();
    }

    private StoreSearchResponse toSearchResponse(Store store) {
        return StoreSearchResponse.builder()
                .storeId(store.getStoreId())
                .categoryId(store.getCategory().getCategoryId())
                .categoryName(store.getCategory().getCategoryName())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .storeStatus(store.getStoreStatus())
                .averageRating(null)
                .build();
    }
}
