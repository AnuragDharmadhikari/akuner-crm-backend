package org.ved.crm.ai.provider;

public record AiResponse(

        String content,

        int promptTokens,

        int completionTokens,

        int totalTokens

) {
}
