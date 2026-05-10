package org.ved.crm.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.ved.crm.common.audit.BaseAuditEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ai_interactions")
public class AiInteraction extends BaseAuditEntity {

    @Column(name = "feature",nullable = false)
    private String feature;

    @Column(name = "entity_id",nullable = false)
    private UUID entityId;

    @Column(name = "prompt_tokens", nullable = false)
    private int promptTokens;

    @Column(name = "completion_tokens", nullable = false)
    private int completionTokens;

    @Column(name = "total_tokens",nullable = false)
    private int totalTokens;

    @Column(name = "cost_usd", nullable = false, precision = 10, scale = 8)
    private BigDecimal costUsd;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Builder.Default
    @Column(name = "cached_response", nullable = false)
    private boolean cachedResponse = false;

    @Column(name = "model_used", nullable = false)
    private String modelUsed;

}
