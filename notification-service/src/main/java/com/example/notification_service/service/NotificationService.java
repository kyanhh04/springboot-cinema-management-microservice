package com.example.notification_service.service;

import com.example.notification_service.dto.TestEmailRequest;
import com.example.notification_service.dto.NotificationPreferenceRequest;
import com.example.notification_service.entity.EmailTemplate;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationPreference;
import com.example.notification_service.event.BookingEvent;
import com.example.notification_service.repository.NotificationPreferenceRepository;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    public static final String BOOKING_CREATED = "BOOKING_CREATED";
    public static final String BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
    public static final String BOOKING_CANCELLED = "BOOKING_CANCELLED";
    public static final String TEST_EMAIL = "TEST_EMAIL";

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final EmailTemplateService emailTemplateService;
    private final TemplateRenderer templateRenderer;
    private final EmailService emailService;

    @Value("${notification.email.max-retry-count:3}")
    private int maxRetryCount;

    @Value("${notification.email.retry-delay-minutes:5}")
    private long retryDelayMinutes;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getFailedNotifications() {
        return notificationRepository.findByStatusOrderByCreatedAtDesc(Notification.NotificationStatus.FAILED);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    public NotificationPreference getPreferenceByUserId(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference preference = new NotificationPreference();
                    preference.setUserId(userId);
                    return notificationPreferenceRepository.save(preference);
                });
    }

    @Transactional
    public NotificationPreference updatePreference(Long userId, NotificationPreference request) {
        NotificationPreference preference = getPreferenceByUserId(userId);
        if (request.getEmailNotificationsEnabled() != null) {
            preference.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getBookingCreatedEmailEnabled() != null) {
            preference.setBookingCreatedEmailEnabled(request.getBookingCreatedEmailEnabled());
        }
        if (request.getBookingConfirmedEmailEnabled() != null) {
            preference.setBookingConfirmedEmailEnabled(request.getBookingConfirmedEmailEnabled());
        }
        if (request.getBookingCancelledEmailEnabled() != null) {
            preference.setBookingCancelledEmailEnabled(request.getBookingCancelledEmailEnabled());
        }
        if (request.getPromotionEmailEnabled() != null) {
            preference.setPromotionEmailEnabled(request.getPromotionEmailEnabled());
        }
        return notificationPreferenceRepository.save(preference);
    }

    @Transactional
    public NotificationPreference updatePreferenceFromDto(Long userId, NotificationPreferenceRequest request) {
        NotificationPreference prefReq = new NotificationPreference();
        prefReq.setUserId(userId);
        prefReq.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        prefReq.setBookingCreatedEmailEnabled(request.getBookingCreatedEmailEnabled());
        prefReq.setBookingConfirmedEmailEnabled(request.getBookingConfirmedEmailEnabled());
        prefReq.setBookingCancelledEmailEnabled(request.getBookingCancelledEmailEnabled());
        prefReq.setPromotionEmailEnabled(request.getPromotionEmailEnabled());
        return updatePreference(userId, prefReq);
    }

    @Transactional
    public void sendBookingCreatedNotification(BookingEvent event) {
        sendBookingNotification(event, BOOKING_CREATED, "Booking Created", buildBookingCreatedMessage(event));
    }

    @Transactional
    public void sendBookingConfirmedNotification(BookingEvent event) {
        sendBookingNotification(event, BOOKING_CONFIRMED, "Booking Confirmed", buildBookingConfirmedMessage(event));
    }

    @Transactional
    public void sendBookingCancelledNotification(BookingEvent event) {
        sendBookingNotification(event, BOOKING_CANCELLED, "Booking Cancelled", buildBookingCancelledMessage(event));
    }

    @Transactional
    public Notification sendTestEmail(TestEmailRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId() != null ? request.getUserId() : 0L);
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setTitle("Test Email");
        notification.setSubject(request.getSubject() != null ? request.getSubject() : "Cinema test email");
        notification.setMessage(request.getMessage() != null ? request.getMessage() : "This is a test email.");
        notification.setHtmlMessage(request.getHtmlMessage());
        notification.setRecipient(request.getRecipient());
        notification.setEventType(TEST_EMAIL);
        notification.setTemplateName(TEST_EMAIL);
        notification.setStatus(Notification.NotificationStatus.PENDING);

        Notification saved = notificationRepository.save(notification);
        return sendSavedNotification(saved);
    }

    @Transactional
    public Notification resendNotification(Long id) {
        Notification notification = getNotificationById(id);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setErrorMessage(null);
        notification.setNextRetryAt(null);
        return sendSavedNotification(notificationRepository.save(notification));
    }

    @Scheduled(fixedDelayString = "${notification.email.retry-scan-delay-ms:60000}")
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> retryableNotifications =
                notificationRepository.findTop50ByStatusAndNextRetryAtLessThanEqualAndRetryCountLessThanOrderByCreatedAtAsc(
                        Notification.NotificationStatus.FAILED,
                        LocalDateTime.now(),
                        maxRetryCount);

        for (Notification notification : retryableNotifications) {
            log.info("Retrying notification id={}, recipient={}, retryCount={}",
                    notification.getId(), notification.getRecipient(), notification.getRetryCount());
            sendSavedNotification(notification);
        }
    }

    private void sendBookingNotification(BookingEvent event, String eventType, String fallbackTitle, String fallbackBody) {
        if (!isEmailEnabledForEvent(event.getUserId(), eventType)) {
            log.info("Skipping {} email for user {} because preference is disabled", eventType, event.getUserId());
            return;
        }

        RenderedEmail renderedEmail = renderBookingEmail(eventType, fallbackTitle, fallbackBody, event);
        Notification notification = createNotification(
                event.getUserId(),
                eventType,
                fallbackTitle,
                renderedEmail.subject(),
                renderedEmail.textBody(),
                renderedEmail.htmlBody(),
                event.getUserEmail());

        sendSavedNotification(notificationRepository.save(notification));
    }

    private Notification sendSavedNotification(Notification notification) {
        try {
            emailService.sendNotificationEmail(notification);
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);
            notification.setNextRetryAt(null);
        } catch (Exception e) {
            log.error("Failed to send notification id={}", notification.getId(), e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setRetryCount((notification.getRetryCount() != null ? notification.getRetryCount() : 0) + 1);
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
            notification.setErrorMessage(e.getMessage());
        }
        return notificationRepository.save(notification);
    }

    private boolean isEmailEnabledForEvent(Long userId, String eventType) {
        NotificationPreference preference = getPreferenceByUserId(userId);
        if (!Boolean.TRUE.equals(preference.getEmailNotificationsEnabled())) {
            return false;
        }
        return switch (eventType) {
            case BOOKING_CREATED -> Boolean.TRUE.equals(preference.getBookingCreatedEmailEnabled());
            case BOOKING_CONFIRMED -> Boolean.TRUE.equals(preference.getBookingConfirmedEmailEnabled());
            case BOOKING_CANCELLED -> Boolean.TRUE.equals(preference.getBookingCancelledEmailEnabled());
            default -> true;
        };
    }

    private RenderedEmail renderBookingEmail(String templateName, String fallbackTitle, String fallbackBody, BookingEvent event) {
        Map<String, Object> variables = bookingVariables(event);
        try {
            EmailTemplate template = emailTemplateService.getActiveTemplateByName(templateName);
            return new RenderedEmail(
                    templateRenderer.render(template.getSubject(), variables),
                    templateRenderer.render(template.getBodyText(), variables),
                    templateRenderer.render(template.getBodyHtml(), variables));
        } catch (RuntimeException e) {
            return new RenderedEmail(
                    fallbackTitle + " - " + event.getBookingReference(),
                    fallbackBody,
                    fallbackBody.replace("\n", "<br/>"));
        }
    }

    private Notification createNotification(
            Long userId,
            String eventType,
            String title,
            String subject,
            String message,
            String htmlMessage,
            String recipient) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setTitle(title);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setHtmlMessage(htmlMessage);
        notification.setRecipient(recipient);
        notification.setEventType(eventType);
        notification.setTemplateName(eventType);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        return notification;
    }

    private Map<String, Object> bookingVariables(BookingEvent event) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("bookingId", event.getBookingId());
        variables.put("bookingReference", event.getBookingReference());
        variables.put("userId", event.getUserId());
        variables.put("userEmail", event.getUserEmail());
        variables.put("movieTitle", event.getMovieTitle());
        variables.put("cinemaName", event.getCinemaName());
        variables.put("totalSeats", event.getTotalSeats());
        variables.put("totalAmount", event.getTotalAmount());
        variables.put("bookingStatus", event.getBookingStatus());
        variables.put("paymentStatus", event.getPaymentStatus());
        variables.put("bookingDate", event.getBookingDate());
        variables.put("expiryTime", event.getExpiryTime());
        variables.put("confirmedAt", event.getConfirmedAt());
        variables.put("cancelledAt", event.getCancelledAt());
        variables.put("cancellationReason", event.getCancellationReason());
        return variables;
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

    private record RenderedEmail(String subject, String textBody, String htmlBody) {
    }
}
