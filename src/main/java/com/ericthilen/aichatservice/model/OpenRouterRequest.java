package com.ericthilen.aichatservice.model;

import java.util.List;

public class OpenRouterRequest {

    private String model;
    private List<ChatMessage> messages;

    public OpenRouterRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}