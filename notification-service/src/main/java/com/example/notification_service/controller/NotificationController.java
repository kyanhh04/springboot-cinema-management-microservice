package com.example.notification_service.controller;

import com.example.common.exception.BusinessException;
import com.example.common.security.SecurityUtils;
import com.example.notification_service.dto.TestEmailRequest;
import com.example.notification_service.entity.EmailTemplate;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationPreference;
import com.example.notification_service.dto.NotificationPreferenceRequest;
import com.example.notification_service.service.EmailTemplateService;
import com.example.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        verifyCanAccessUser(notification.getUserId());
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getFailedNotifications() {
        return ResponseEntity.ok(notificationService.getFailedNotifications());
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> resendNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.resendNotification(id));
    }

    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> sendTestEmail(@Valid @RequestBody TestEmailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.sendTestEmail(request));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable Long userId) {
        verifyCanAccessUser(userId);
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @GetMapping("/preferences/users/{userId}")
    public ResponseEntity<NotificationPreference> getPreference(@PathVariable Long userId) {
        verifyCanAccessUser(userId);
        return ResponseEntity.ok(notificationService.getPreferenceByUserId(userId));
    }

    @PutMapping("/preferences/users/{userId}")
    public ResponseEntity<NotificationPreference> updatePreference(
            @PathVariable Long userId,
            @Valid @RequestBody NotificationPreferenceRequest preferenceRequest) {
        verifyCanAccessUser(userId);
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setEmailNotificationsEnabled(preferenceRequest.getEmailNotificationsEnabled());
        pref.setBookingCreatedEmailEnabled(preferenceRequest.getBookingCreatedEmailEnabled());
        pref.setBookingConfirmedEmailEnabled(preferenceRequest.getBookingConfirmedEmailEnabled());
        pref.setBookingCancelledEmailEnabled(preferenceRequest.getBookingCancelledEmailEnabled());
        pref.setPromotionEmailEnabled(preferenceRequest.getPromotionEmailEnabled());
        return ResponseEntity.ok(notificationService.updatePreference(userId, pref));
    }

    @GetMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        return ResponseEntity.ok(emailTemplateService.getAllTemplates());
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailTemplate> createTemplate(@Valid @RequestBody EmailTemplate template) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailTemplateService.createTemplate(template));
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailTemplate> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody EmailTemplate template) {
        return ResponseEntity.ok(emailTemplateService.updateTemplate(id, template));
    }

    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        emailTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    private void verifyCanAccessUser(Long userId) {
        if (SecurityUtils.hasRole("ADMIN") || Objects.equals(userId, SecurityUtils.getCurrentUserId())) {
            return;
        }

        throw new BusinessException("Access denied", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}
