package com.example.inventory_service.dto;

import lombok.Data;

@Data
public class InventoryRequest {
    private Long cinemaId;
    private Long productId;
    private Integer quantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;
}
