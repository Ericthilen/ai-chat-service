package com.ericthilen.aichatservice.service;

import org.springframework.stereotype.Service;
import com.ericthilen.aichatservice.model.ChatRequest;

import java.util.UUID;

@Service
public class ChatService {

    public String createReply(ChatRequest request) {
        String message = request.getMessage();

        return "Svar: " + message;
    }

    public String getSessionId(ChatRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            return UUID.randomUUID().toString();
        }

        return request.getSessionId();
    }
}