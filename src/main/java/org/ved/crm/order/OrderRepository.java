package org.ved.crm.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ved.crm.analytics.OrderProjections;

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

    // ── ANALYTICS: Rep Performance
    @Query(value = """
        SELECT
            u.id                                                AS rep_id,
            u.full_name                                         AS rep_name,
            COUNT(DISTINCT v.id)                                AS total_visits,
            COUNT(DISTINCT v.id) FILTER (
                WHERE v.status = 'COMPLETED'
            )                                                   AS completed_visits,
            COUNT(DISTINCT o.id)                                AS total_orders,
            COALESCE(SUM(DISTINCT i.grand_total), 0)            AS total_revenue,
            ct.target_visits                                    AS target_visits,
            ct.actual_visits                                    AS actual_visits
        FROM users u
        LEFT JOIN orders o      ON o.rep_id = u.id
        LEFT JOIN invoices i    ON i.order_id = o.id
            AND i.status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID')
        LEFT JOIN visits v      ON v.rep_id = u.id
        LEFT JOIN call_targets ct ON ct.rep_id = u.id
            AND ct.month = EXTRACT(MONTH FROM NOW())
            AND ct.year  = EXTRACT(YEAR  FROM NOW())
        WHERE u.role = 'REP'
          AND u.is_active = true
        GROUP BY u.id, u.full_name, ct.target_visits, ct.actual_visits
        ORDER BY total_revenue DESC
        """, nativeQuery = true)
    List<OrderProjections.RepPerformanceProjection> findRepPerformance();
}