package com.example.notification_service.dto;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private Boolean emailNotificationsEnabled;

    private Boolean bookingCreatedEmailEnabled;

    private Boolean bookingConfirmedEmailEnabled;

    private Boolean bookingCancelledEmailEnabled;

    private Boolean promotionEmailEnabled;
}
