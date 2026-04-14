package com.example.booking_service.controller;

import com.example.booking_service.entity.Booking;
import com.example.booking_service.service.BookingService;
import com.example.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        try {
            // Automatically set userId from JWT token
            Long userId = SecurityUtils.getCurrentUserId();
            booking.setUserId(userId);
            
            Booking created = bookingService.createBooking(booking);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingService.getBookingById(id);
        
        // Check if user owns this booking
        if (!booking.getUserId().equals(userId) && !SecurityUtils.hasRole("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(booking);
    }
}
