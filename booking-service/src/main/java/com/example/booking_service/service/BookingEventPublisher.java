package com.example.booking_service.service;

import com.example.booking_service.config.RabbitMQConfig;
import com.example.booking_service.entity.Booking;
import com.example.booking_service.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBookingCreated(Booking booking) {
        BookingEvent event = buildBookingEvent(booking, "CREATED");
        
        log.info("Publishing booking created event: {}", event.getBookingReference());
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_EXCHANGE,
                RabbitMQConfig.BOOKING_CREATED_ROUTING_KEY,
                event
        );
        
        log.info("Booking created event published successfully");
    }

    public void publishBookingConfirmed(Booking booking) {
        BookingEvent event = buildBookingEvent(booking, "CONFIRMED");
        
        log.info("Publishing booking confirmed event: {}", event.getBookingReference());
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_EXCHANGE,
                RabbitMQConfig.BOOKING_CONFIRMED_ROUTING_KEY,
                event
        );
        
        log.info("Booking confirmed event published successfully");
    }

    public void publishBookingCancelled(Booking booking) {
        BookingEvent event = buildBookingEvent(booking, "CANCELLED");
        
        log.info("Publishing booking cancelled event: {}", event.getBookingReference());
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_EXCHANGE,
                RabbitMQConfig.BOOKING_CANCELLED_ROUTING_KEY,
                event
        );
        
        log.info("Booking cancelled event published successfully");
    }

    private BookingEvent buildBookingEvent(Booking booking, String eventType) {
        return BookingEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .userEmail("user@example.com") // TODO: Fetch from User Service
                .showtimeId(booking.getShowtimeId())
                .cinemaId(booking.getCinemaId())
                .movieId(booking.getMovieId())
                .movieTitle("Movie Title") // TODO: Fetch from Cinema Service
                .cinemaName("Cinema Name") // TODO: Fetch from Cinema Service
                .totalSeats(booking.getTotalSeats())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus().name())
                .paymentStatus(booking.getPaymentStatus().name())
                .bookingDate(booking.getBookingDate())
                .expiryTime(booking.getExpiryTime())
                .confirmedAt(booking.getConfirmedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .eventType(eventType)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
