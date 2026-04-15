package com.example.notification_service.service;

import com.example.notification_service.entity.Notification;
import com.example.notification_service.event.BookingEvent;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void sendBookingCreatedNotification(BookingEvent event) {
        log.info("Sending booking created notification for: {}", event.getBookingReference());
        
        // Save notification to database
        Notification notification = createNotification(
                event.getUserId(),
                "BOOKING_CREATED",
                "Booking Created",
                buildBookingCreatedMessage(event),
                event.getUserEmail()
        );
        
        notificationRepository.save(notification);
        
        // Send email (async)
        try {
            emailService.sendBookingCreatedEmail(event);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void sendBookingConfirmedNotification(BookingEvent event) {
        log.info("Sending booking confirmed notification for: {}", event.getBookingReference());
        
        Notification notification = createNotification(
                event.getUserId(),
                "BOOKING_CONFIRMED",
                "Booking Confirmed",
                buildBookingConfirmedMessage(event),
                event.getUserEmail()
        );
        
        notificationRepository.save(notification);
        
        try {
            emailService.sendBookingConfirmedEmail(event);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void sendBookingCancelledNotification(BookingEvent event) {
        log.info("Sending booking cancelled notification for: {}", event.getBookingReference());
        
        Notification notification = createNotification(
                event.getUserId(),
                "BOOKING_CANCELLED",
                "Booking Cancelled",
                buildBookingCancelledMessage(event),
                event.getUserEmail()
        );
        
        notificationRepository.save(notification);
        
        try {
            emailService.sendBookingCancelledEmail(event);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    private Notification createNotification(Long userId, String type, String title, String message, String recipient) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipient(recipient);
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private String buildBookingCreatedMessage(BookingEvent event) {
        return String.format(
                "Your booking has been created successfully!\n\n" +
                "Booking Reference: %s\n" +
                "Movie: %s\n" +
                "Cinema: %s\n" +
                "Seats: %d\n" +
                "Total Amount: %s\n" +
                "Booking Date: %s\n" +
                "Expiry Time: %s\n\n" +
                "Please complete your payment before the expiry time.",
                event.getBookingReference(),
                event.getMovieTitle(),
                event.getCinemaName(),
                event.getTotalSeats(),
                event.getTotalAmount(),
                event.getBookingDate(),
                event.getExpiryTime()
        );
    }

    private String buildBookingConfirmedMessage(BookingEvent event) {
        return String.format(
                "Your booking has been confirmed!\n\n" +
                "Booking Reference: %s\n" +
                "Movie: %s\n" +
                "Cinema: %s\n" +
                "Seats: %d\n" +
                "Total Amount: %s\n" +
                "Confirmed At: %s\n\n" +
                "Thank you for your booking!",
                event.getBookingReference(),
                event.getMovieTitle(),
                event.getCinemaName(),
                event.getTotalSeats(),
                event.getTotalAmount(),
                event.getConfirmedAt()
        );
    }

    private String buildBookingCancelledMessage(BookingEvent event) {
        return String.format(
                "Your booking has been cancelled.\n\n" +
                "Booking Reference: %s\n" +
                "Movie: %s\n" +
                "Cinema: %s\n" +
                "Cancelled At: %s\n" +
                "Reason: %s\n\n" +
                "If you have any questions, please contact our support team.",
                event.getBookingReference(),
                event.getMovieTitle(),
                event.getCinemaName(),
                event.getCancelledAt(),
                event.getCancellationReason() != null ? event.getCancellationReason() : "Not specified"
        );
    }
}
