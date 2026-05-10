package org.ved.crm.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

// Configuration for the OpenAI HTTP client
// We use Java's built-in HttpClient — no extra dependencies needed
// Spring Boot 4 + Java 25 has a production-grade HttpClient in stdlib
@Configuration
public class AiConfig {

    // Timeout injected from application.yml — openai.timeout-seconds: 30
    // AI calls can be slow — 30 seconds is generous but safe
    // Without timeout, a slow OpenAI response hangs your thread forever
    @Value("${openai.timeout-seconds}")
    private int timeoutSeconds;

    // HttpClient bean — shared across all AI calls
    // Singleton scope by default — one client, reused for every request
    // HttpClient is thread-safe — safe to share across concurrent requests
    // connectTimeout — how long to wait to establish TCP connection to OpenAI
    // followRedirects — automatically follow HTTP 3xx redirects
    @Bean
    public HttpClient openAiHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}