package com.georgia.jeogiyo.ai.repository;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiHistoryRepository extends JpaRepository<AiHistory, UUID>, AiHistoryRepositoryCustom {

}