package com.example.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cinema_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cinema_id", "product_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaInventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cinema_id", nullable = false)
    private Long cinemaId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;
    
    @Column(name = "max_stock_level")
    private Integer maxStockLevel = 1000;
    
    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "cinemaInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockMovement> stockMovements;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
