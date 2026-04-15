package com.example.cinema_service.dto;

import com.example.cinema_service.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String genre;
    private Movie.SubtitleType subtitleType;
    private LocalDate releaseDate;
    private String director;
    private String cast;
    private String rating;
    private String posterUrl;
    private String trailerUrl;
    private Movie.MovieStatus status;
}
