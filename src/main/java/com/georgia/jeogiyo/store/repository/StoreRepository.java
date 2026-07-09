package com.georgia.jeogiyo.store.repository;

import com.georgia.jeogiyo.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
* QueryDSL 적용: categoryId, keyword, isDeleted=false 조건 검색 구현 */

public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {
    Optional<Store> findByStoreIdAndIsDeletedFalse(UUID storeId);
}
