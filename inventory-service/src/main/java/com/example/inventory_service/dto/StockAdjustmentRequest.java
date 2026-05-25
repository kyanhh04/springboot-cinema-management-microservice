package com.example.inventory_service.dto;

import com.example.inventory_service.entity.StockMovement;
import lombok.Data;

@Data
public class StockAdjustmentRequest {
    private Long cinemaId;
    private Long productId;
    private Integer quantity;
    private StockMovement.MovementType movementType;
    private String notes;
    private Long performedBy;
}
