package com.example.cinema_service.dto;

import com.example.cinema_service.entity.Screen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenDTO {
    private Long id;
    private Long cinemaId;
    private String screenNumber;
    private Integer totalSeats;
    private Screen.ScreenStatus status;
}
