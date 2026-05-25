package com.example.inventory_service.controller;

import com.example.inventory_service.dto.InventoryRequest;
import com.example.inventory_service.dto.StockAdjustmentRequest;
import com.example.inventory_service.entity.CinemaInventory;
import com.example.inventory_service.entity.StockMovement;
import com.example.inventory_service.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final StockService stockService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CinemaInventory>> getAllInventory() {
        return ResponseEntity.ok(stockService.getAllInventory());
    }

    @GetMapping("/cinema/{cinemaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CinemaInventory>> getInventoryByCinema(@PathVariable Long cinemaId) {
        return ResponseEntity.ok(stockService.getInventoryByCinema(cinemaId));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CinemaInventory>> getLowStockInventory() {
        return ResponseEntity.ok(stockService.getLowStockInventory());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaInventory> createInventory(@RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.createOrUpdateInventory(request));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaInventory> updateInventory(@RequestBody InventoryRequest request) {
        return ResponseEntity.ok(stockService.createOrUpdateInventory(request));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaInventory> adjustStock(@RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockService.adjustStock(request));
    }

    @PostMapping("/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaInventory> restock(@RequestBody StockAdjustmentRequest request) {
        request.setMovementType(StockMovement.MovementType.IN);
        return ResponseEntity.ok(stockService.adjustStock(request));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockMovement>> getAllMovements() {
        return ResponseEntity.ok(stockService.getAllMovements());
    }

    @GetMapping("/{inventoryId}/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockMovement>> getMovementsByInventory(@PathVariable Long inventoryId) {
        return ResponseEntity.ok(stockService.getMovementsByInventory(inventoryId));
    }
}
