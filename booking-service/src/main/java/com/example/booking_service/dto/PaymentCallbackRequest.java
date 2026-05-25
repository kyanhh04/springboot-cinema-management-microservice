package com.example.booking_service.dto;

import com.example.booking_service.entity.Payment;
import lombok.Data;

@Data
public class PaymentCallbackRequest {
    private String paymentReference;
    private Payment.PaymentStatus paymentStatus;
}
