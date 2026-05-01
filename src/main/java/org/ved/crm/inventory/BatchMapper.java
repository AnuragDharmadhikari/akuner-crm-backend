package org.ved.crm.inventory;

import org.springframework.stereotype.Component;

@Component
public class BatchMapper {

    public BatchDto toDto(Batch batch){
        return new BatchDto(
                batch.getId(),
                batch.getProduct().getId(),
                batch.getProduct().getName(),
                batch.getProduct().getHsnCode(),
                batch.getBatchNumber(),
                batch.getMfgDate(),
                batch.getExpiryDate(),
                batch.getInitialQuantity(),
                batch.getCurrentQuantity(),
                batch.isExpired(),
                batch.isNearExpiry(),
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }
}
