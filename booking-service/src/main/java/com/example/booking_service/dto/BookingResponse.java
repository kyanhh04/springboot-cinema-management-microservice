package com.example.booking_service.dto;

import com.example.booking_service.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    
    private Long id;
    private String bookingReference;
    private Long userId;
    private Long showtimeId;
    private Long cinemaId;
    private Long movieId;
    private Integer totalSeats;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private String paymentStatus;
    private LocalDateTime bookingDate;
    private LocalDateTime expiryTime;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BookingResponse fromEntity(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                .cinemaId(booking.getCinemaId())
                .movieId(booking.getMovieId())
                .totalSeats(booking.getTotalSeats())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus().name())
                .paymentStatus(booking.getPaymentStatus().name())
                .bookingDate(booking.getBookingDate())
                .expiryTime(booking.getExpiryTime())
                .confirmedAt(booking.getConfirmedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
