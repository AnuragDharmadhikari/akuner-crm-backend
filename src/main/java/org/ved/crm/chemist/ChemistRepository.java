package org.ved.crm.chemist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChemistRepository extends JpaRepository<Chemist, UUID> {

    // JOIN FETCH assignedRep so mapper can access rep name without LazyInitializationException
    @Query("""
            SELECT c FROM Chemist c
            JOIN FETCH c.assignedRep r
            WHERE c.id = :id
            """)
    Optional<Chemist> findByIdWithDetails(@Param("id") UUID id);

    // Get all active chemists with rep details loaded
    @Query("""
            SELECT c FROM Chemist c
            JOIN FETCH c.assignedRep r
            WHERE c.isActive = true
            """)
    List<Chemist> findAllActiveWithDetails();

    // Get all chemists assigned to a specific rep
    @Query("""
            SELECT c FROM Chemist c
            JOIN FETCH c.assignedRep r
            WHERE c.assignedRep.id = :repId
            AND c.isActive = true
            """)
    List<Chemist> findByAssignedRepId(@Param("repId") UUID repId);

    // Duplicate checks before hitting DB unique constraint
    boolean existsByDrugLicenseNumber(String drugLicenseNumber);
    boolean existsByGstin(String gstin);
}