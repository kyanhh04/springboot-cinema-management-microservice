package com.example.cinema_service.dto;

import com.example.cinema_service.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Long id;
    private Long screenId;
    private String seatRow;
    private Integer seatNumber;
    private String seatLabel;
    private Seat.SeatType seatType;
    private Seat.SeatStatus status;
}
