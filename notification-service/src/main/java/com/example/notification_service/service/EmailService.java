package com.example.notification_service.service;

import com.example.notification_service.entity.Notification;
import com.example.notification_service.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:no-reply@cinema.local}")
    private String emailFrom;

    public void sendNotificationEmail(Notification notification) {
        sendEmail(
                notification.getRecipient(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getHtmlMessage());
    }

    public void sendEmail(String recipient, String subject, String textBody, String htmlBody) {
        if (!emailEnabled) {
            log.info("Email disabled. Simulated email to={}, subject={}", recipient, subject);
            log.debug("Simulated email body: {}", textBody);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new RuntimeException("JavaMailSender is not available. Check spring-boot-starter-mail dependency.");
        }

        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(textBody != null ? textBody : "", htmlBody != null ? htmlBody : textBody);
            mailSender.send(message);
            log.info("Email sent to={}, subject={}", recipient, subject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + recipient, e);
        }
    }

    public void sendBookingCreatedEmail(BookingEvent event) {
        log.info("Sending booking created email to: {}", event.getUserEmail());
        log.info("Subject: Booking Created - {}", event.getBookingReference());
        log.info("Booking details: Movie={}, Cinema={}, Seats={}, Amount={}",
                event.getMovieTitle(), event.getCinemaName(), event.getTotalSeats(), event.getTotalAmount());
    }

    public void sendBookingConfirmedEmail(BookingEvent event) {
        log.info("Sending booking confirmed email to: {}", event.getUserEmail());
        log.info("Subject: Booking Confirmed - {}", event.getBookingReference());
    }

    public void sendBookingCancelledEmail(BookingEvent event) {
        log.info("Sending booking cancelled email to: {}", event.getUserEmail());
        log.info("Subject: Booking Cancelled - {}", event.getBookingReference());
    }
}
