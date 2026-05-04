package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.model.ChatMessage;
import com.ericthilen.aichatservice.model.ChatRequest;
import com.ericthilen.aichatservice.model.OpenRouterRequest;
import com.ericthilen.aichatservice.model.OpenRouterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class ChatService {

    private final RestClient restClient;

    private final Map<String, List<ChatMessage>> memory = new HashMap<>();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String model;

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

        OpenRouterRequest aiRequest = new OpenRouterRequest(model, messagesToAi);

        OpenRouterResponse aiResponse = restClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(aiRequest)
                .retrieve()
                .body(OpenRouterResponse.class);

        String reply = aiResponse
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();

        history.add(new ChatMessage("user", request.getMessage()));
        history.add(new ChatMessage("assistant", reply));
        memory.put(sessionId, history);

        return reply;
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