package com.example.booking_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent implements Serializable {
    
    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private String userEmail; // Will be fetched from User Service
    private Long showtimeId;
    private Long cinemaId;
    private Long movieId;
    private String movieTitle; // Will be fetched from Cinema Service
    private String cinemaName; // Will be fetched from Cinema Service
    private Integer totalSeats;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private String paymentStatus;
    private LocalDateTime bookingDate;
    private LocalDateTime expiryTime;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String eventType; // CREATED, CONFIRMED, CANCELLED
    private LocalDateTime eventTimestamp;
}
