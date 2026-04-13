package com.example.cinema_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(length = 100)
    private String genre;
    
    @Column(length = 50)
    private String language;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    @Column(length = 100)
    private String director;
    
    @Column(columnDefinition = "TEXT")
    private String cast;
    
    @Column(length = 10)
    private String rating;
    
    @Column(name = "poster_url", length = 500)
    private String posterUrl;
    
    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status = MovieStatus.COMING_SOON;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
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
    
    public enum MovieStatus {
        COMING_SOON, NOW_SHOWING, ENDED
    }
}
