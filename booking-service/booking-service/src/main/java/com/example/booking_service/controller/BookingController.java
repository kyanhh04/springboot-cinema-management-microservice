//package com.example.booking_service.controller;
//
//import com.example.booking_service.entity.Booking;
//import com.example.booking_service.service.BookingService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/bookings")
//public class BookingController {
//
//    @Autowired
//    private BookingService bookingService;
//
//    @GetMapping
//    public ResponseEntity<List<Booking>> getAllBookings() {
//        return ResponseEntity.ok(bookingService.getAllBookings());
//    }
//
//    @PostMapping
//    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
//        try {
//            Booking created = bookingService.createBooking(booking);
//            return ResponseEntity.ok(created);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//}
