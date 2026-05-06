package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.exception.AiServiceException;
import com.ericthilen.aichatservice.model.ChatMessage;
import com.ericthilen.aichatservice.model.OpenRouterRequest;
import com.ericthilen.aichatservice.model.OpenRouterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class OpenRouterClient {

    private final RestClient restClient;

    @Value("${openrouter.api.key}")
    String apiKey;

    @Value("${openrouter.api.url}")
    String apiUrl;

    @Value("${openrouter.model}")
    String model;

    public OpenRouterClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retryable(
            retryFor = RestClientException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String askAi(List<ChatMessage> messagesToAi) {
        try {
            OpenRouterRequest aiRequest = new OpenRouterRequest(model, messagesToAi);

            OpenRouterResponse aiResponse = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(aiRequest)
                    .retrieve()
                    .body(OpenRouterResponse.class);

            if (aiResponse == null || aiResponse.getChoices() == null || aiResponse.getChoices().isEmpty()) {
                throw new AiServiceException("AI-tjänsten svarade inte korrekt.");
            }

            return aiResponse.getChoices().get(0).getMessage().getContent();

        } catch (RestClientException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException("AI-tjänsten kunde inte hantera svaret.");
        }
    }
}