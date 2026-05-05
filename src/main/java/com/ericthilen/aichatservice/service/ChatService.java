package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.exception.AiServiceException;
import com.ericthilen.aichatservice.model.ChatMessage;
import com.ericthilen.aichatservice.model.ChatRequest;
import com.ericthilen.aichatservice.model.OpenRouterRequest;
import com.ericthilen.aichatservice.model.OpenRouterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
public class ChatService {

    private final RestClient restClient;

    private final Map<String, List<ChatMessage>> memory = new HashMap<>();

    @Value("${openrouter.api.key}")
    String apiKey;

    @Value("${openrouter.api.url}")
    String apiUrl;

    @Value("${openrouter.model}")
    String model;

    public ChatService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String createReply(ChatRequest request) {
        String sessionId = getSessionId(request);

        List<ChatMessage> history = memory.getOrDefault(sessionId, new ArrayList<>());

        List<ChatMessage> messagesToAi = new ArrayList<>();
        messagesToAi.add(new ChatMessage("system", getSystemPrompt(request.getPersonality())));
        messagesToAi.addAll(history);
        messagesToAi.add(new ChatMessage("user", request.getMessage()));

        String reply = askAi(messagesToAi);

        history.add(new ChatMessage("user", request.getMessage()));
        history.add(new ChatMessage("assistant", reply));
        memory.put(sessionId, history);

        return reply;
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

            return aiResponse
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

        } catch (RestClientException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException("AI-tjänsten kunde inte hantera svaret.");
        }
    }

    public String getSessionId(ChatRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            return UUID.randomUUID().toString();
        }

        return request.getSessionId();
    }

    private String getSystemPrompt(String personality) {
        return "Du är en hjälpsam assistent. Svara tydligt, enkelt och på svenska.";
    }
}