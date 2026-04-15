package com.example.booking_service.controller;

import com.example.booking_service.dto.BookingResponse;
import com.example.booking_service.dto.CreateBookingRequest;
import com.example.booking_service.entity.Booking;
import com.example.booking_service.service.BookingService;
import com.example.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings().stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        try {
            // Get userId from JWT token
            Long userId = SecurityUtils.getCurrentUserId();
            
            // Create booking entity
            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setShowtimeId(request.getShowtimeId());
            booking.setCinemaId(request.getCinemaId());
            booking.setMovieId(request.getMovieId());
            booking.setTotalSeats(request.getSeatIds() != null ? request.getSeatIds().size() : 0);
            booking.setTotalAmount(request.getTotalAmount());
            
            Booking created = bookingService.createBooking(booking);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BookingResponse.fromEntity(created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Booking booking = bookingService.getBookingById(id);
            
            // Check if user owns this booking or is admin
            if (!booking.getUserId().equals(userId) && !SecurityUtils.hasRole("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(BookingResponse.fromEntity(booking));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Booking booking = bookingService.getBookingById(id);
            
            // Check ownership
            if (!booking.getUserId().equals(userId) && !SecurityUtils.hasRole("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Booking confirmed = bookingService.confirmBooking(id);
            return ResponseEntity.ok(BookingResponse.fromEntity(confirmed));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Booking booking = bookingService.getBookingById(id);
            
            // Check ownership
            if (!booking.getUserId().equals(userId) && !SecurityUtils.hasRole("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Booking cancelled = bookingService.cancelBooking(id, reason);
            return ResponseEntity.ok(BookingResponse.fromEntity(cancelled));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
