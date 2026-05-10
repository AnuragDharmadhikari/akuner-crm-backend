package org.ved.crm.ai.provider;

// Provider abstraction — AiService depends on this interface, not on OpenAI directly
// If we ever want to switch to Gemini, Claude, or any other LLM:
// 1. Write a new implementation of this interface
// 2. Swap the @Primary bean in config
// 3. Zero changes anywhere else in the codebase
public interface AiProvider {

    // Complete a chat prompt and return AiResponse
    // AiResponse carries both the text content AND token usage
    // systemPrompt — sets the AI's role and rules
    // userPrompt — the actual question with domain data as context
    AiResponse complete(String systemPrompt, String userPrompt);
}