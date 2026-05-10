package org.ved.crm.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface AiInteractionRepository extends JpaRepository<AiInteraction, UUID> {

    // Total cost spent on AI calls by a specific user
    // Owner can see how much each rep is consuming in AI credits
    @Query(value = """
            SELECT COALESCE(SUM(cost_usd), 0)
            FROM ai_interactions
            WHERE performed_by = :performedBy
            """, nativeQuery = true)
    BigDecimal getTotalCostByUser(UUID performedBy);

    // Total cost across entire application
    // Owner uses this to monitor overall AI spend
    @Query(value = """
            SELECT COALESCE(SUM(cost_usd), 0)
            FROM ai_interactions
            """, nativeQuery = true)
    BigDecimal getTotalCostAllTime();

    // Count of AI calls per feature — usage analytics
    // Tells Owner which features are being used most
    @Query(value = """
            SELECT COUNT(*)
            FROM ai_interactions
            WHERE feature = :feature
            """, nativeQuery = true)
    long getCallCountByFeature(String feature);
}