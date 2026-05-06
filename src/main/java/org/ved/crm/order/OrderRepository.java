package org.ved.crm.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // JOIN FETCH chemist always — it is never null
    // LEFT JOIN FETCH stockist — it can be null for DIRECT orders
    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.rep r
            JOIN FETCH o.chemist c
            LEFT JOIN FETCH o.stockist s
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.rep r
            JOIN FETCH o.chemist c
            LEFT JOIN FETCH o.stockist s
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            """)
    List<Order> findAllWithDetails();

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.rep r
            JOIN FETCH o.chemist c
            LEFT JOIN FETCH o.stockist s
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            WHERE o.stockist.id = :stockistId
            """)
    List<Order> findByStockistIdWithDetails(@Param("stockistId") UUID stockistId);

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.rep r
            JOIN FETCH o.chemist c
            LEFT JOIN FETCH o.stockist s
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            WHERE o.chemist.id = :chemistId
            """)
    List<Order> findByChemistIdWithDetails(@Param("chemistId") UUID chemistId);

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.rep r
            JOIN FETCH o.chemist c
            LEFT JOIN FETCH o.stockist s
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            WHERE o.rep.id = :repId
            """)
    List<Order> findByRepIdWithDetails(@Param("repId") UUID repId);
}