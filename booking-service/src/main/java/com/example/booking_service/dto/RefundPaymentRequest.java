package com.example.booking_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundPaymentRequest {
    private BigDecimal refundAmount;
}
