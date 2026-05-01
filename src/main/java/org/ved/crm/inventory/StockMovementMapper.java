package org.ved.crm.inventory;

import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementDto toDto(StockMovement movement){
        return new StockMovementDto(
                movement.getId(),
                movement.getBatch().getId(),
                movement.getBatch().getBatchNumber(),
                movement.getBatch().getProduct().getId(),
                movement.getBatch().getProduct().getName(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReferenceId(),
                movement.getReferenceType(),
                movement.getNotes(),
                movement.getCreatedAt()
        );
    }
}
