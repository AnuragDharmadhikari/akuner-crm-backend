package org.ved.crm.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ved.crm.analytics.BatchProjections;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    // JOIN FETCH product for mapper access
    @Query("""
            SELECT b FROM Batch b
            JOIN FETCH b.product p
            WHERE b.id = :id
            """)
    Optional<Batch> findByIdWithDetails(@Param("id") UUID id);

    // Get all batches for a product — ordered by expiry date ASC
    // FIFO by expiry — oldest expiring batch comes first
    @Query("""
            SELECT b FROM Batch b
            JOIN FETCH b.product p
            WHERE b.product.id = :productId
            AND b.currentQuantity > 0
            ORDER BY b.expiryDate ASC
            """)
    List<Batch> findAvailableBatchesByProduct(@Param("productId") UUID productId);

    // Get all batches for a product including empty ones — for reporting
    @Query("""
            SELECT b FROM Batch b
            JOIN FETCH b.product p
            WHERE b.product.id = :productId
            ORDER BY b.expiryDate ASC
            """)
    List<Batch> findAllBatchesByProduct(@Param("productId") UUID productId);

    // Get near expiry batches — expiring within next 90 days
    @Query("""
            SELECT b FROM Batch b
            JOIN FETCH b.product p
            WHERE b.expiryDate <= :warningDate
            AND b.expiryDate >= :today
            AND b.currentQuantity > 0
            ORDER BY b.expiryDate ASC
            """)
    List<Batch> findNearExpiryBatches(
            @Param("today") LocalDate today,
            @Param("warningDate") LocalDate warningDate);

    // Get expired batches that still have stock — need writeoff
    @Query("""
            SELECT b FROM Batch b
            JOIN FETCH b.product p
            WHERE b.expiryDate < :today
            AND b.currentQuantity > 0
            """)
    List<Batch> findExpiredBatchesWithStock(@Param("today") LocalDate today);

    // Check duplicate batch number per product
    boolean existsByProductIdAndBatchNumber(UUID productId, String batchNumber);

    // ── ANALYTICS: Inventory Value
    @Query(value = """
        SELECT
            p.id                                            AS product_id,
            p.name                                          AS product_name,
            p.hsn_code                                      AS hsn_code,
            p.dealer_price                                  AS dealer_price,
            SUM(b.current_quantity)                         AS total_current_units,
            ROUND(SUM(b.current_quantity * p.dealer_price), 2) AS total_inventory_value
        FROM batches b
        JOIN products p ON p.id = b.product_id
        WHERE b.expiry_date > CURRENT_DATE
          AND b.current_quantity > 0
          AND p.is_active = true
        GROUP BY p.id, p.name, p.hsn_code, p.dealer_price
        ORDER BY total_inventory_value DESC
        """, nativeQuery = true)
    List<BatchProjections.InventoryValueProjection> findInventoryValue();

    // ── ANALYTICS: Near Expiry Value
    @Query(value = """
        SELECT
            b.id                                                    AS batch_id,
            b.batch_number                                          AS batch_number,
            p.id                                                    AS product_id,
            p.name                                                  AS product_name,
            b.expiry_date                                           AS expiry_date,
            (b.expiry_date - CURRENT_DATE)::BIGINT                  AS days_until_expiry,
            b.current_quantity                                      AS current_quantity,
            p.dealer_price                                          AS dealer_price,
            ROUND(b.current_quantity * p.dealer_price, 2)          AS value_at_risk
        FROM batches b
        JOIN products p ON p.id = b.product_id
        WHERE b.expiry_date > CURRENT_DATE
          AND b.expiry_date <= CURRENT_DATE + INTERVAL '90 days'
          AND b.current_quantity > 0
          AND p.is_active = true
        ORDER BY b.expiry_date ASC
        """, nativeQuery = true)
    List<BatchProjections.NearExpiryValueProjection> findNearExpiryBatches();
}