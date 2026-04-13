package com.example.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_inventory_id", nullable = false)
    private CinemaInventory cinemaInventory;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "reference_type", length = 50)
    private String referenceType;
    
    @Column(name = "reference_id")
    private Long referenceId;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "performed_by")
    private Long performedBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum MovementType {
        IN, OUT, ADJUSTMENT, RETURN
    }
}
