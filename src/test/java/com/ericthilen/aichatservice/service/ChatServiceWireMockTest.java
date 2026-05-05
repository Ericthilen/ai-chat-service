package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.model.ChatRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class ChatServiceWireMockTest {

    private WireMockServer wireMockServer;
    private ChatService chatService;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

        configureFor("localhost", 8089);

        RestClient restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();

        chatService = new ChatService(restClient);

        chatService.apiKey = "test";
        chatService.apiUrl = "http://localhost:8089/chat";
        chatService.model = "test";
    }

    @AfterEach
    void teardown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldReturnMockedAiResponse() {
        stubFor(post(urlEqualTo("/chat"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "choices": [
                                    {
                                      "message": {
                                        "content": "Mockat AI-svar"
                                      }
                                    }
                                  ]
                                }
                                """)));

        ChatRequest request = new ChatRequest();
        request.setMessage("Hej");
        request.setSessionId("123");
        request.setPersonality("helper");

        String reply = chatService.createReply(request);

        assertEquals("Mockat AI-svar", reply);
    }
}