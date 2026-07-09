package com.georgia.jeogiyo.ai.repository;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.entity.AiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AiHistoryRepositoryCustom {

    Optional<AiHistory> findActiveById(UUID aiHistoryId);

    Page<AiHistory> searchAiHistories(
            AiStatus aiStatus,
            UUID productId,
            Pageable pageable
    );
}