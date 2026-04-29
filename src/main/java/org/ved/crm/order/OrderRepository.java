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

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.rep
        JOIN FETCH o.stockist
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product
        WHERE o.id = :id
    """)
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.rep
        JOIN FETCH o.stockist
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product
        ORDER BY o.orderDate DESC
    """)
    List<Order> findAllWithDetails();

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.rep
        JOIN FETCH o.stockist
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product
        WHERE o.stockist.id = :stockistId
        ORDER BY o.orderDate DESC
    """)
    List<Order> findByStockistIdWithDetails(
            @Param("stockistId") UUID stockistId);

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.rep
        JOIN FETCH o.stockist
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product
        WHERE o.rep.id = :repId
        ORDER BY o.orderDate DESC
    """)
    List<Order> findByRepIdWithDetails(@Param("repId") UUID repId);
}