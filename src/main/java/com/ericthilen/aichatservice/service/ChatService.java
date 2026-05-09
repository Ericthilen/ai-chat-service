package com.ericthilen.aichatservice.service;

import com.ericthilen.aichatservice.model.ChatMessage;
import com.ericthilen.aichatservice.model.ChatRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    private final OpenRouterClient openRouterClient;

    private final Map<String, List<ChatMessage>> memory = new HashMap<>();
    private final Map<String, String> sessionPersonality = new HashMap<>();

    public ChatService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    public String createReply(ChatRequest request) {
        String sessionId = request.getSessionId();
        String currentPersonality = request.getPersonality();

        List<ChatMessage> history = memory.getOrDefault(sessionId, new ArrayList<>());
        String lastPersonality = sessionPersonality.get(sessionId);

        // Om personligheten har ändrats, lägg till ett förtydligande meddelande i historiken
        if (lastPersonality != null && !lastPersonality.equalsIgnoreCase(currentPersonality)) {
            history.add(new ChatMessage("system", "OBS: Användaren har bytt personlighet till: " + currentPersonality + ". Justera din stil därefter."));
        }
        sessionPersonality.put(sessionId, currentPersonality);

        List<ChatMessage> messagesToAi = new ArrayList<>();
        messagesToAi.add(new ChatMessage("system", getSystemPrompt(currentPersonality)));
        messagesToAi.addAll(history);
        messagesToAi.add(new ChatMessage("user", request.getMessage()));

        String reply = openRouterClient.askAi(messagesToAi);

        history.add(new ChatMessage("user", request.getMessage()));
        history.add(new ChatMessage("assistant", reply));
        memory.put(sessionId, history);

        return reply;
    }

    public String getSessionId(ChatRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            return UUID.randomUUID().toString();
        }

        return request.getSessionId();
    }

    private String getSystemPrompt(String personality) {
        if (personality.equalsIgnoreCase("coder")) {
            return "Du är Eric Coder, en duktig programmeringshjälpare. Svara tydligt, enkelt och visa kodexempel när det passar. Håll dig till din roll som Eric Coder.";
        }

        if (personality.equalsIgnoreCase("pirate")) {
            return "Du är Eric Pirate. Du är en hjälpsam assistent som svarar med pirat-tema (använd pirat-ord som 'Ahoy', 'Landkrabba', etc). Svara på svenska och håll dig i karaktär som en pirat hela tiden.";
        }

        return "Du är Eric Helper, en hjälpsam och vänlig assistent. Svara tydligt, enkelt och på svenska.";
    }
}