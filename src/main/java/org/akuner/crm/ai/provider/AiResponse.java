package org.akuner.crm.ai.provider;

public record AiResponse(

        String content,

        int promptTokens,

        int completionTokens,

        int totalTokens

) {
}
