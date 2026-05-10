package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.model.ChatMessage;
import com.ericthilen.aichatservice.model.OpenRouterResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
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
        wireMockServer = new WireMockServer(0); // Dynamisk port
        wireMockServer.start();

        int port = wireMockServer.port();
        configureFor("localhost", port);

        RestClient restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();

        openRouterClient = new OpenRouterClient(restClient);

        openRouterClient.apiKey = "test";
        openRouterClient.apiUrl = "http://localhost:" + port + "/chat";
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

    @Test
    void shouldRetryOnFailureAndSucceed() {
        // Vi simulerar ett scenario: "Retry Scenario"
        // 1. Första anropet ger 503 Service Unavailable
        stubFor(post(urlEqualTo("/chat"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("Failed Once"));

        // 2. Andra anropet ger 200 OK
        stubFor(post(urlEqualTo("/chat"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Failed Once")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "choices": [
                                    {
                                      "message": {
                                        "content": "Svar efter retry"
                                      }
                                    }
                                  ]
                                }
                                """)));

        List<ChatMessage> messages = List.of(new ChatMessage("user", "Hej"));

        // Vi använder en manuell RetryTemplate eftersom vi inte har full Spring Context
        org.springframework.retry.support.RetryTemplate retryTemplate = new org.springframework.retry.support.RetryTemplate();
        org.springframework.retry.policy.SimpleRetryPolicy retryPolicy = new org.springframework.retry.policy.SimpleRetryPolicy(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        String reply = retryTemplate.execute(context -> openRouterClient.askAi(messages));

        assertEquals("Svar efter retry", reply);

        // Verifiera att WireMock tog emot 2 anrop
        verify(2, postRequestedFor(urlEqualTo("/chat")));
    }
}