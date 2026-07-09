package com.georgia.jeogiyo.ai.repository;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiHistoryRepository extends JpaRepository<AiHistory, UUID>, AiHistoryRepositoryCustom {

    // TODO QueryDSL Custom Repository 추가 예정
    // AiHistoryRepositoryCustom 상속 예정
}