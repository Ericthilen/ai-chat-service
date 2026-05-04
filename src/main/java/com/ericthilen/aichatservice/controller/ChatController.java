package com.ericthilen.aichatservice.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.ericthilen.aichatservice.model.ChatRequest;
import com.ericthilen.aichatservice.model.ChatResponse;
import com.ericthilen.aichatservice.service.ChatService;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String reply = chatService.createReply(request);
        String sessionId = chatService.getSessionId(request);

        return new ChatResponse(reply, sessionId);
    }
}