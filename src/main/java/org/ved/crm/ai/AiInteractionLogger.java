package org.ved.crm.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.ai.provider.AiResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

// Separate Spring bean for logging AI interactions
// Extracted from AiService because @Transactional self-invocation
// does not work — Spring proxy is bypassed when calling methods
// on the same bean. Moving to a separate bean fixes this.
// REQUIRES_NEW — always creates a fresh transaction for the log write
// even if called from within a readOnly transaction in AiService
@Service
@RequiredArgsConstructor
public class AiInteractionLogger {

    private final AiInteractionRepository aiInteractionRepository;

    private static final BigDecimal INPUT_COST_PER_TOKEN =
            new BigDecimal("0.00000015");
    private static final BigDecimal OUTPUT_COST_PER_TOKEN =
            new BigDecimal("0.00000060");

    // REQUIRES_NEW — suspends any existing transaction and creates a new one
    // This guarantees the log write succeeds even if the caller is readOnly
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String feature, UUID entityId,
                    AiResponse aiResponse, long durationMs,
                    UUID performedBy) {

        BigDecimal promptCost = INPUT_COST_PER_TOKEN
                .multiply(BigDecimal.valueOf(aiResponse.promptTokens()));
        BigDecimal completionCost = OUTPUT_COST_PER_TOKEN
                .multiply(BigDecimal.valueOf(aiResponse.completionTokens()));
        BigDecimal totalCost = promptCost.add(completionCost)
                .setScale(8, RoundingMode.HALF_UP);

        AiInteraction interaction = AiInteraction.builder()
                .feature(feature)
                .entityId(entityId)
                .promptTokens(aiResponse.promptTokens())
                .completionTokens(aiResponse.completionTokens())
                .totalTokens(aiResponse.totalTokens())
                .costUsd(totalCost)
                .durationMs(durationMs)
                .performedBy(performedBy)
                .cachedResponse(false)
                .modelUsed("gpt-4o-mini")
                .build();

        aiInteractionRepository.save(interaction);
    }
}