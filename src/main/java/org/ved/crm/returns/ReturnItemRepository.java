package org.ved.crm.returns;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, UUID> {
}
