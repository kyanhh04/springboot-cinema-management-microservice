package com.example.cinema_service.controller;

import com.example.cinema_service.dto.CinemaDTO;
import com.example.cinema_service.dto.ScreenDTO;
import com.example.cinema_service.entity.Cinema;
import com.example.cinema_service.service.CinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public ResponseEntity<List<CinemaDTO>> getAllCinemas() {
        return ResponseEntity.ok(cinemaService.getAllCinemas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaDTO> getCinemaById(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }

    @PostMapping
    public ResponseEntity<CinemaDTO> createCinema(@Valid @RequestBody Cinema cinema) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cinemaService.createCinema(cinema));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CinemaDTO> updateCinema(
            @PathVariable Long id,
            @Valid @RequestBody Cinema cinema) {
        return ResponseEntity.ok(cinemaService.updateCinema(id, cinema));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCinema(@PathVariable Long id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/screens")
    public ResponseEntity<List<ScreenDTO>> getScreensByCinema(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.getScreensByCinema(id));
    }
}
