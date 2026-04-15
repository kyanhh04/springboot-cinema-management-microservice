package com.example.booking_service.dto;

import com.example.booking_service.entity.Booking;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
    private LocalDateTime createdAt;

    public static BookingResponse fromEntity(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setUserId(booking.getUserId());
        response.setShowtimeId(booking.getShowtimeId());
        response.setCinemaId(booking.getCinemaId());
        response.setMovieId(booking.getMovieId());
        response.setTotalSeats(booking.getTotalSeats());
        response.setTotalAmount(booking.getTotalAmount());
        response.setBookingStatus(booking.getBookingStatus().name());
        response.setPaymentStatus(booking.getPaymentStatus().name());
        response.setBookingDate(booking.getBookingDate());
        response.setExpiryTime(booking.getExpiryTime());
        response.setConfirmedAt(booking.getConfirmedAt());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
}
