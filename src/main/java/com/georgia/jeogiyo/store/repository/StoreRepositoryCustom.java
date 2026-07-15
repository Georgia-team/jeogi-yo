package com.georgia.jeogiyo.store.repository;

import com.georgia.jeogiyo.store.dto.response.StoreSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreRepositoryCustom {

    Page<StoreSearchResponse> searchStores(
            UUID categoryId,
            String keyword,
            Pageable pageable
    );
}
