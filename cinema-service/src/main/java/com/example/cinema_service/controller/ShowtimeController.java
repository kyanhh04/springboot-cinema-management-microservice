package com.example.cinema_service.controller;

import com.example.cinema_service.dto.ShowtimeDTO;
import com.example.cinema_service.entity.Showtime;
import com.example.cinema_service.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes() {
        return ResponseEntity.ok(showtimeService.getAllShowtimes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(movieId, date));
    }

    @GetMapping("/movie/{movieId}/cinema/{cinemaId}")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovieAndCinema(
            @PathVariable Long movieId,
            @PathVariable Long cinemaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovieAndCinema(movieId, cinemaId, date));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(showtimeService.getShowtimesByDateRange(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<ShowtimeDTO> createShowtime(@Valid @RequestBody Showtime showtime) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(showtimeService.createShowtime(showtime));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> updateShowtime(
            @PathVariable Long id,
            @Valid @RequestBody Showtime showtime) {
        return ResponseEntity.ok(showtimeService.updateShowtime(id, showtime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<Void> reserveSeats(
            @PathVariable Long id,
            @RequestParam int seats) {
        showtimeService.reserveSeats(id, seats);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseSeats(
            @PathVariable Long id,
            @RequestParam int seats) {
        showtimeService.releaseSeats(id, seats);
        return ResponseEntity.ok().build();
    }
}
