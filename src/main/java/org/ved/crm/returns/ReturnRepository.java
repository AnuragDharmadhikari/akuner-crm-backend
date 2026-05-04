package org.ved.crm.returns;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReturnRepository extends JpaRepository<Return, UUID> {

    // Load return with all relationships in one query
    // LEFT JOIN FETCH on chemist and stockist — only one will be present
    // JOIN FETCH on returnItems to avoid LazyInitializationException in mapper
    @Query("""
            SELECT r FROM Return r
            LEFT JOIN FETCH r.chemist c
            LEFT JOIN FETCH r.stockist s
            JOIN FETCH r.returnItems ri
            JOIN FETCH ri.batch b
            JOIN FETCH ri.product p
            WHERE r.id = :id
            """)
    Optional<Return> findByIdWithDetails(@Param("id") UUID id);

    // Get all returns — ordered newest first
    @Query("""
            SELECT DISTINCT r FROM Return r
            LEFT JOIN FETCH r.chemist c
            LEFT JOIN FETCH r.stockist s
            JOIN FETCH r.returnItems ri
            JOIN FETCH ri.batch b
            JOIN FETCH ri.product p
            ORDER BY r.returnDate DESC
            """)
    List<Return> findAllWithDetails();

    // Get all returns by a specific chemist
    @Query("""
            SELECT DISTINCT r FROM Return r
            LEFT JOIN FETCH r.chemist c
            LEFT JOIN FETCH r.stockist s
            JOIN FETCH r.returnItems ri
            JOIN FETCH ri.batch b
            JOIN FETCH ri.product p
            WHERE r.chemist.id = :chemistId
            ORDER BY r.returnDate DESC
            """)
    List<Return> findByChemistId(@Param("chemistId") UUID chemistId);

    // Get all returns by a specific stockist
    @Query("""
            SELECT DISTINCT r FROM Return r
            LEFT JOIN FETCH r.chemist c
            LEFT JOIN FETCH r.stockist s
            JOIN FETCH r.returnItems ri
            JOIN FETCH ri.batch b
            JOIN FETCH ri.product p
            WHERE r.stockist.id = :stockistId
            ORDER BY r.returnDate DESC
            """)
    List<Return> findByStockistId(@Param("stockistId") UUID stockistId);

    // Sequential return number from PostgreSQL SEQUENCE
    @Query(value = "SELECT nextval('return_number_seq')", nativeQuery = true)
    Long getNextSequenceValue();
}