package com.example.notification_service.dto;

import lombok.Data;

@Data
public class TestEmailRequest {
    private Long userId;
    private String recipient;
    private String subject;
    private String message;
    private String htmlMessage;
}
