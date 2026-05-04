package com.ericthilen.aichatservice.model;

import java.util.List;

public class OpenRouterResponse {

    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public static class Choice {
        private ChatMessage message;

        public ChatMessage getMessage() {
            return message;
        }
    }
}