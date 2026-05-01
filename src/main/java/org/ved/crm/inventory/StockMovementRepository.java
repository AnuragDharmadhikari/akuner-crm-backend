package org.ved.crm.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    // Get all movements for a specific batch — full audit trail
    @Query("""
            SELECT sm FROM StockMovement sm
            JOIN FETCH sm.batch b
            JOIN FETCH b.product p
            WHERE sm.batch.id = :batchId
            ORDER BY sm.createdAt DESC
            """)
    List<StockMovement> findByBatchId(@Param("batchId") UUID batchId);

    // Get all movements of a specific type — e.g. all SALE movements
    @Query("""
            SELECT sm FROM StockMovement sm
            JOIN FETCH sm.batch b
            JOIN FETCH b.product p
            WHERE sm.movementType = :movementType
            ORDER BY sm.createdAt DESC
            """)
    List<StockMovement> findByMovementType(
            @Param("movementType") MovementType movementType);
}