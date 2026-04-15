package com.example.cinema_service.controller;

import com.example.cinema_service.dto.ScreenDTO;
import com.example.cinema_service.dto.SeatDTO;
import com.example.cinema_service.entity.Screen;
import com.example.cinema_service.service.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @GetMapping("/{id}")
    public ResponseEntity<ScreenDTO> getScreenById(@PathVariable Long id) {
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    @PostMapping
    public ResponseEntity<ScreenDTO> createScreen(@Valid @RequestBody Screen screen) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(screenService.createScreen(screen));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScreenDTO> updateScreen(
            @PathVariable Long id,
            @Valid @RequestBody Screen screen) {
        return ResponseEntity.ok(screenService.updateScreen(id, screen));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDTO>> getSeatsByScreen(@PathVariable Long id) {
        return ResponseEntity.ok(screenService.getSeatsByScreen(id));
    }

    @PostMapping("/{id}/generate-seats")
    public ResponseEntity<List<SeatDTO>> generateSeats(
            @PathVariable Long id,
            @RequestParam int rows,
            @RequestParam int seatsPerRow) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(screenService.generateSeatsForScreen(id, rows, seatsPerRow));
    }
}
