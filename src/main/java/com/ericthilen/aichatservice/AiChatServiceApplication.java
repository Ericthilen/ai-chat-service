package com.ericthilen.aichatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class AiChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatServiceApplication.class, args);
    }
}