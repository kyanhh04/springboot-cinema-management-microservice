package com.example.cinema_service.mapper;

import com.example.cinema_service.dto.*;
import com.example.cinema_service.entity.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class CinemaMapper {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static CinemaDTO toDTO(Cinema cinema) {
        return CinemaDTO.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .address(cinema.getAddress())
                .city(cinema.getCity())
                .state(cinema.getState())
                .phoneNumber(cinema.getPhoneNumber())
                .email(cinema.getEmail())
                .description(cinema.getDescription())
                .status(cinema.getStatus())
                .totalScreens(cinema.getScreens() != null ? cinema.getScreens().size() : 0)
                .build();
    }
    
    public static MovieDTO toDTO(Movie movie) {
        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .genre(movie.getGenre())
                .subtitleType(movie.getSubtitleType())
                .releaseDate(movie.getReleaseDate())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .rating(movie.getRating())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .status(movie.getStatus())
                .build();
    }
    
    public static ScreenDTO toDTO(Screen screen) {
        return ScreenDTO.builder()
                .id(screen.getId())
                .cinemaId(screen.getCinema().getId())
                .screenNumber(screen.getScreenNumber())
                .totalSeats(screen.getTotalSeats())
                .status(screen.getStatus())
                .build();
    }
    
    public static ShowtimeDTO toDTO(Showtime showtime) {
        return ShowtimeDTO.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .screenId(showtime.getScreen().getId())
                .screenName(showtime.getScreen().getScreenNumber())
                .cinemaId(showtime.getScreen().getCinema().getId())
                .cinemaName(showtime.getScreen().getCinema().getName())
                .showDate(showtime.getShowDate())
                .showTime(showtime.getShowTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .status(showtime.getStatus())
                .availableSeats(showtime.getAvailableSeats())
                .totalSeats(showtime.getScreen().getTotalSeats())
                .build();
    }
    
    public static SeatDTO toDTO(Seat seat) {
        return SeatDTO.builder()
                .id(seat.getId())
                .screenId(seat.getScreen().getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .seatLabel(seat.getSeatRow() + seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .status(seat.getStatus())
                .build();
    }
}
