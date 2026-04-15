package com.example.cinema_service.dto;

import com.example.cinema_service.entity.Showtime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long screenId;
    private String screenName;
    private Long cinemaId;
    private String cinemaName;
    private LocalDate showDate;
    private LocalTime showTime;
    private LocalTime endTime;
    private BigDecimal basePrice;
    private Showtime.ShowtimeStatus status;
    private Integer availableSeats;
    private Integer totalSeats;
}
