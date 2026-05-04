package com.ericthilen.aichatservice.model;

public class ChatResponse {

    private String reply;
    private String sessionId;

    public ChatResponse(String reply, String sessionId) {
        this.reply = reply;
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public String getSessionId() {
        return sessionId;
    }
}
