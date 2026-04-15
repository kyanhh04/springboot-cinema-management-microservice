package com.example.notification_service.listener;

import com.example.notification_service.event.BookingEvent;
import com.example.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "booking.created.queue")
    public void handleBookingCreated(BookingEvent event) {
        log.info("Received booking created event: {}", event.getBookingReference());
        
        try {
            notificationService.sendBookingCreatedNotification(event);
            log.info("Booking created notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send booking created notification", e);
            throw e; // Will trigger retry mechanism
        }
    }

    @RabbitListener(queues = "booking.confirmed.queue")
    public void handleBookingConfirmed(BookingEvent event) {
        log.info("Received booking confirmed event: {}", event.getBookingReference());
        
        try {
            notificationService.sendBookingConfirmedNotification(event);
            log.info("Booking confirmed notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send booking confirmed notification", e);
            throw e;
        }
    }

    @RabbitListener(queues = "booking.cancelled.queue")
    public void handleBookingCancelled(BookingEvent event) {
        log.info("Received booking cancelled event: {}", event.getBookingReference());
        
        try {
            notificationService.sendBookingCancelledNotification(event);
            log.info("Booking cancelled notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send booking cancelled notification", e);
            throw e;
        }
    }
}
