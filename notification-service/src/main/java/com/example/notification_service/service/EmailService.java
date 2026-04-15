package com.example.notification_service.service;

import com.example.notification_service.event.BookingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
    
    public void sendBookingCreatedEmail(BookingEvent event) {
        log.info("Sending booking created email to: {}", event.getUserEmail());
        log.info("Subject: Booking Created - {}", event.getBookingReference());
        log.info("Booking details: Movie={}, Cinema={}, Seats={}, Amount={}",
                event.getMovieTitle(), event.getCinemaName(), event.getTotalSeats(), event.getTotalAmount());
        
        // Simulate email sending
        simulateEmailSending();
    }

    public void sendBookingConfirmedEmail(BookingEvent event) {
        log.info("Sending booking confirmed email to: {}", event.getUserEmail());
        log.info("Subject: Booking Confirmed - {}", event.getBookingReference());
        
        simulateEmailSending();
    }

    public void sendBookingCancelledEmail(BookingEvent event) {
        log.info("Sending booking cancelled email to: {}", event.getUserEmail());
        log.info("Subject: Booking Cancelled - {}", event.getBookingReference());
        
        simulateEmailSending();
    }

    private void simulateEmailSending() {
        try {
            Thread.sleep(100); // Simulate email sending delay
            log.info("Email sent successfully (simulated)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email sending interrupted", e);
        }
    }
}
