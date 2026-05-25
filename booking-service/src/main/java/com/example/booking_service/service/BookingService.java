package com.example.booking_service.service;

import com.example.booking_service.client.CinemaClient;
import com.example.booking_service.entity.Booking;
import com.example.booking_service.entity.BookingSeat;
import com.example.booking_service.entity.Payment;
import com.example.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CinemaClient cinemaClient;
    private final BookingEventPublisher eventPublisher;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        return createBooking(booking, List.of());
    }

    @Transactional
    public Booking createBooking(Booking booking, List<Long> seatIds) {
        log.info("Creating booking for user: {}", booking.getUserId());
        if (seatIds != null && !seatIds.isEmpty()) {
            cinemaClient.reserveSeats(booking.getShowtimeId(), seatIds.size());
            booking.setBookingSeats(buildBookingSeats(booking, seatIds));
            booking.setTotalSeats(seatIds.size());
        }
        
        // Generate unique booking reference
        booking.setBookingReference(generateBookingReference());
        
        // Set initial status
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Payment.PaymentStatus.PENDING);

        booking.setBookingDate(LocalDateTime.now());
        booking.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        if (booking.getBookingProducts() != null) {
            booking.getBookingProducts().forEach(product -> product.setBooking(booking));
        }
        
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with reference: {}", savedBooking.getBookingReference());
        
        // Publish event to RabbitMQ
        try {
            eventPublisher.publishBookingCreated(savedBooking);
        } catch (Exception e) {
            log.error("Failed to publish booking created event", e);
        }
        
        return savedBooking;
    }

    @Transactional
    public Booking confirmBooking(Long bookingId) {
        log.info("Confirming booking: {}", bookingId);
        
        Booking booking = getBookingById(bookingId);
        
        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking cannot be confirmed. Current status: " + booking.getBookingStatus());
        }
        
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Payment.PaymentStatus.PAID);
        booking.setConfirmedAt(LocalDateTime.now());
        
        Booking confirmedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed: {}", confirmedBooking.getBookingReference());
        
        // Publish event to RabbitMQ
        try {
            eventPublisher.publishBookingConfirmed(confirmedBooking);
        } catch (Exception e) {
            log.error("Failed to publish booking confirmed event", e);
        }
        
        return confirmedBooking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String reason) {
        log.info("Cancelling booking: {}", bookingId);
        
        Booking booking = getBookingById(bookingId);
        
        if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        if (booking.getTotalSeats() != null && booking.getTotalSeats() > 0) {
            cinemaClient.releaseSeats(booking.getShowtimeId(), booking.getTotalSeats());
        }

        
        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking cancelled: {}", cancelledBooking.getBookingReference());

        try {
            eventPublisher.publishBookingCancelled(cancelledBooking);
        } catch (Exception e) {
            log.error("Failed to publish booking cancelled event", e);
        }
        
        return cancelledBooking;
    }

    private String generateBookingReference() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private List<BookingSeat> buildBookingSeats(Booking booking, List<Long> seatIds) {
        return seatIds.stream()
                .map(seatId -> {
                    CinemaClient.SeatInfo seatInfo = cinemaClient.getSeatById(seatId);
                    BookingSeat bookingSeat = new BookingSeat();
                    bookingSeat.setBooking(booking);
                    bookingSeat.setSeatId(seatId);
                    bookingSeat.setSeatRow(seatInfo.getSeatRow());
                    bookingSeat.setSeatNumber(seatInfo.getSeatNumber());
                    bookingSeat.setSeatType(BookingSeat.SeatType.valueOf(seatInfo.getSeatType()));
                    bookingSeat.setPrice(booking.getTotalAmount()
                            .divide(java.math.BigDecimal.valueOf(seatIds.size()), 2, java.math.RoundingMode.HALF_UP));
                    return bookingSeat;
                })
                .collect(Collectors.toList());
    }
}
