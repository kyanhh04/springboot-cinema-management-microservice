package com.example.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;
    
    @Column(name = "sms_enabled")
    private Boolean smsEnabled = true;
    
    @Column(name = "push_enabled")
    private Boolean pushEnabled = true;
    
    @Column(name = "in_app_enabled")
    private Boolean inAppEnabled = true;
    
    @Column(name = "booking_notifications")
    private Boolean bookingNotifications = true;
    
    @Column(name = "promotional_notifications")
    private Boolean promotionalNotifications = true;
    
    @Column(name = "system_notifications")
    private Boolean systemNotifications = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
