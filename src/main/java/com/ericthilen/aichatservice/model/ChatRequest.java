package com.ericthilen.aichatservice.model;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    @NotBlank
    private String personality;

    @NotBlank
    private String message;

    private String sessionId;

    public String getPersonality() {
        return personality;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
