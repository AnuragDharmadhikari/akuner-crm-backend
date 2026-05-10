package org.ved.crm.ai.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// OpenAI implementation of AiProvider
// Makes HTTP POST to https://api.openai.com/v1/chat/completions
// Parses JSON response and returns AiResponse with text + token usage
@Component
public class OpenAiProvider implements AiProvider{

    private static final String OPENAI_API = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private int maxTokens;

    @Value("${openai.timeout-seconds}")
    private int timeoutSeconds;

    private final HttpClient httpClient;

    // ObjectMapper is thread-safe and expensive to create
    // Create once here — reuse for every call
    private final ObjectMapper objectMapper;

    public OpenAiProvider(HttpClient openAiHttpClient) {
        this.httpClient = openAiHttpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AiResponse complete(String systemPrompt, String userPrompt) {

        try{
            // ── Build Request Body ─────────────────────────────────────────────
            // OpenAI expects this JSON structure:
            // {
            //   "model": "gpt-4o-mini",
            //   "max_tokens": 1000,
            //   "messages": [
            //     {"role": "system", "content": "You are a pharma analyst..."},
            //     {"role": "user",   "content": "Analyse this doctor..."}
            //   ]
            // }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model",model);
            requestBody.put("max_tokens",maxTokens);

            // Build messages array
            ArrayNode messages = objectMapper.createArrayNode();

            // System message — sets AI persona and rules
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role","system");
            systemMessage.put("content",systemPrompt);
            messages.add(systemMessage);

            // User message — actual prompt with domain data
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role","user");
            userMessage.put("content",userPrompt);
            messages.add(userMessage);

            requestBody.set("messages",messages);

            // Serialize to JSON string
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // ── Build HTTP Request ─────────────────────────────────────────────
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API))
                    .header("Authorization", "Bearer "+apiKey)
                    .header("Content-Type","application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // ── Send Request ───────────────────────────────────────────────────
            // Synchronous — blocks until OpenAI responds
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // ── Handle Non-200 Response ────────────────────────────────────────
            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "OpenAI API error. Status: " + response.statusCode()
                                + " Body: " + response.body()
                );
            }

            // ── Parse Response ─────────────────────────────────────────────────
            // OpenAI response structure:
            // {
            //   "choices": [{"message": {"content": "AI text here"}}],
            //   "usage": {
            //     "prompt_tokens": 150,
            //     "completion_tokens": 300,
            //     "total_tokens": 450
            //   }
            // }
            JsonNode responseJson = objectMapper.readTree(response.body());

            // Extract AI generated text
            // choices[0].message.content = the response text
            String content = responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content").asString();

            // Extract token usage for cost tracking
            JsonNode usage = responseJson.path("usage");
            int promptTokens     = usage.path("prompt_tokens").asInt();
            int completionTokens = usage.path("completion_tokens").asInt();
            int totalTokens      = usage.path("total_tokens").asInt();

            // Return both content and token usage in one object
            return new AiResponse(content, promptTokens, completionTokens, totalTokens);

        } catch (RuntimeException e) {
            // Re-throw runtime exceptions as-is
            throw e;
        } catch (Exception e) {
            // Wrap checked exceptions into unchecked
            throw new RuntimeException(
                    "Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }
}
