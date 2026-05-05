package com.ericthilen.aichatservice.controller;

import com.ericthilen.aichatservice.model.ChatRequest;
import com.ericthilen.aichatservice.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void shouldReturnChatResponse() throws Exception {

        when(chatService.createReply(any(ChatRequest.class))).thenReturn("Test-svar");
        when(chatService.getSessionId(any(ChatRequest.class))).thenReturn("123");

        mockMvc.perform(post("/api/v1/chat")
                        .contentType("application/json")
                        .content("""
                                {
                                  "personality": "helper",
                                  "message": "Hej",
                                  "sessionId": "123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Test-svar"))
                .andExpect(jsonPath("$.sessionId").value("123"));
    }

    @Test
    void shouldReturnBadRequestWhenMessageIsEmpty() throws Exception {

        mockMvc.perform(post("/api/v1/chat")
                        .contentType("application/json")
                        .content("""
                                {
                                  "personality": "helper",
                                  "message": "",
                                  "sessionId": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPersonalityIsEmpty() throws Exception {

        mockMvc.perform(post("/api/v1/chat")
                        .contentType("application/json")
                        .content("""
                                {
                                  "personality": "",
                                  "message": "Hej",
                                  "sessionId": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}