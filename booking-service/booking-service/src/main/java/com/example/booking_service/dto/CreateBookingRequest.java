package com.example.booking_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateBookingRequest {
    private Long showtimeId;
    private Long cinemaId;
    private Long movieId;
    private List<Long> seatIds;
    private BigDecimal totalAmount;
}
