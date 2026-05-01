package org.ved.crm.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}