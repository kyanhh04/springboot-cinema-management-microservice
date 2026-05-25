package com.example.booking_service.dto;

import com.example.booking_service.entity.Payment;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private Long bookingId;
    private Payment.PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String currency;
}
