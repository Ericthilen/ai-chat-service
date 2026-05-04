package com.ericthilen.aichatservice.service;

import org.springframework.stereotype.Service;
import com.ericthilen.aichatservice.model.ChatRequest;

import java.util.*;

@Service
public class ChatService {

    private final Map<String, List<String>> memory = new HashMap<>();

    public String createReply(ChatRequest request) {
        String message = request.getMessage();
        String sessionId = getSessionId(request);

        List<String> history = memory.getOrDefault(sessionId, new ArrayList<>());

        history.add(message);

        memory.put(sessionId, history);

        return "Svar: " + message + " (antal meddelanden: " + history.size() + ")";
    }

    public String getSessionId(ChatRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            return UUID.randomUUID().toString();
        }

        return request.getSessionId();
    }
}