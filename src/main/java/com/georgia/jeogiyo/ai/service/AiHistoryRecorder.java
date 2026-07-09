package com.georgia.jeogiyo.ai.service;

import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.ai.repository.AiHistoryRepository;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiHistoryRecorder {

    private final AiHistoryRepository aiHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFail(
            User user,
            Product product,
            String requestText,
            String modelName,
            String errorMessage
    ) {
        aiHistoryRepository.save(
                AiHistory.fail(user, product, requestText, modelName, errorMessage)
        );
    }
}