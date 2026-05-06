package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.model.ChatMessage;
import com.ericthilen.aichatservice.model.ChatRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    private final OpenRouterClient openRouterClient;

    private final Map<String, List<ChatMessage>> memory = new HashMap<>();

    public ChatService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    public String createReply(ChatRequest request) {
        String sessionId = request.getSessionId();

        List<ChatMessage> history = memory.getOrDefault(sessionId, new ArrayList<>());

        List<ChatMessage> messagesToAi = new ArrayList<>();
        messagesToAi.add(new ChatMessage("system", getSystemPrompt(request.getPersonality())));
        messagesToAi.addAll(history);
        messagesToAi.add(new ChatMessage("user", request.getMessage()));

        String reply = openRouterClient.askAi(messagesToAi);

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
        if (personality.equalsIgnoreCase("coder")) {
            return "Du är en programmeringshjälpare. Svara tydligt, enkelt och visa kodexempel när det passar.";
        }

        if (personality.equalsIgnoreCase("pirate")) {
            return "Du är en hjälpsam assistent som svarar med lite piratstil. Svara fortfarande tydligt och på svenska.";
        }

        return "Du är en hjälpsam assistent. Svara tydligt, enkelt och på svenska.";
    }
}