package com.example.cinema_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"screen_id", "seat_row", "seat_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;
    
    @Column(name = "seat_row", nullable = false, length = 5)
    private String seatRow;
    
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType = SeatType.REGULAR;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum SeatType {
        REGULAR, VIP, COUPLE
    }
    
    public enum SeatStatus {
        AVAILABLE, BLOCKED, MAINTENANCE
    }
}
