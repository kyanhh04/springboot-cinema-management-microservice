package com.example.booking_service.service;

import com.example.booking_service.client.CinemaClient;
import com.example.booking_service.entity.Booking;
import com.example.booking_service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CinemaClient cinemaClient;

//    public List<Booking> getAllBookings() {
//        return bookingRepository.findAll();
//    }

//    public Booking createBooking(Booking booking) {
//        // Kiểm tra số ghế còn trống từ cinema-service
//        Integer availableSeats = cinemaClient.getAvailableSeats(booking.getCinemaId());
//
//        if (availableSeats == null || availableSeats < booking.getNumberOfSeats()) {
//            throw new RuntimeException("Not enough seats available");
//        }
//
//        booking.setStatus("CONFIRMED");
//        return bookingRepository.save(booking);
//    }
}
