package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.model.ChatMessage;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class ChatServiceWireMockTest {

    private WireMockServer wireMockServer;
    private OpenRouterClient openRouterClient;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

        configureFor("localhost", 8089);

        RestClient restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();

        openRouterClient = new OpenRouterClient(restClient);

        openRouterClient.apiKey = "test";
        openRouterClient.apiUrl = "http://localhost:8089/chat";
        openRouterClient.model = "test";
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

        List<ChatMessage> messages = List.of(
                new ChatMessage("system", "Du är en hjälpsam assistent."),
                new ChatMessage("user", "Hej")
        );

        String reply = openRouterClient.askAi(messages);

        assertEquals("Mockat AI-svar", reply);
    }
}