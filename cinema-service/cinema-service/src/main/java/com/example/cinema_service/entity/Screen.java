package com.example.cinema_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "screens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cinema_id", "screen_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Screen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;
    
    @Column(name = "screen_number", nullable = false, length = 20)
    private String screenNumber;
    
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScreenStatus status = ScreenStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats;
    
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Showtime> showtimes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ScreenStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }
}
