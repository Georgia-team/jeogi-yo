package com.georgia.jeogiyo.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGeminiServiceImpl implements AiGeminiService {

    private static final String MODEL_NAME = "gemini-2.5-flash-lite";

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent";

    private final RestClient.Builder restClientBuilder;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public String generateDescription(String requestText) {
        log.info("Gemini request started. model={}, requestLength={}",
                MODEL_NAME, requestText == null ? 0 : requestText.length());

        try {
            RestClient restClient = restClientBuilder.build();

            GeminiRequest request = new GeminiRequest(
                    List.of(new Content(
                            List.of(new Part(requestText))
                    ))
            );

            GeminiResponse response = restClient.post()
                    .uri(GEMINI_API_URL + "?key={key}", apiKey)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            String text = extractText(response);

            if (text == null || text.isBlank()) {
                throw new BusinessException(GlobalErrorCode.EMPTY_AI_RESPONSE);
            }

            log.info("Gemini request succeeded. model={}, responseLength={}", MODEL_NAME, text.length());

            return text;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini request failed. model={}", MODEL_NAME, e);
            throw new BusinessException(GlobalErrorCode.AI_GENERATION_FAILED);
        }

    }

    private String extractText(GeminiResponse response) {
        if (response == null
                || response.candidates() == null
                || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            return null;
        }

        return response.candidates().get(0).content().parts().get(0).text();
    }

    private record GeminiRequest(
            List<Content> contents
    ) {
    }

    private record Content(
            List<Part> parts
    ) {
    }

    private record Part(
            String text
    ) {
    }

    private record GeminiResponse(
            List<Candidate> candidates
    ) {
    }

    private record Candidate(
            Content content
    ) {
    }
}