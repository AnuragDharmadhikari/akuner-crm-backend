package org.ved.crm.target;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallTargetRepository extends JpaRepository<CallTarget, UUID> {

    // JOIN FETCH both rep and assignedBy to avoid LazyInitializationException
    @Query("""
            SELECT ct FROM CallTarget ct
            JOIN FETCH ct.rep r
            JOIN FETCH ct.assignedBy ab
            WHERE ct.id = :id
            """)
    Optional<CallTarget> findByIdWithDetails(@Param("id") UUID id);

    // Get all targets for a specific rep
    @Query("""
            SELECT ct FROM CallTarget ct
            JOIN FETCH ct.rep r
            JOIN FETCH ct.assignedBy ab
            WHERE ct.rep.id = :repId
            ORDER BY ct.year DESC, ct.month DESC
            """)
    List<CallTarget> findByRepIdWithDetails(@Param("repId") UUID repId);

    // Get target for a specific rep in a specific month and year
    @Query("""
            SELECT ct FROM CallTarget ct
            JOIN FETCH ct.rep r
            JOIN FETCH ct.assignedBy ab
            WHERE ct.rep.id = :repId
            AND ct.month = :month
            AND ct.year = :year
            """)
    Optional<CallTarget> findByRepIdAndMonthAndYear(
            @Param("repId") UUID repId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    // Check for duplicate before hitting DB unique constraint
    boolean existsByRepIdAndMonthAndYear(UUID repId, Integer month, Integer year);
}