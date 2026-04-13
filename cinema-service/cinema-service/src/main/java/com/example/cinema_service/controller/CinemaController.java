//package com.example.cinema_service.controller;
//
//import com.example.cinema_service.service.CinemaService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/cinemas")
//public class CinemaController {
//
//    @Autowired
//    private CinemaService cinemaService;
//
//    @GetMapping
//    public ResponseEntity<List<Cinema>> getAllCinemas() {
//        return ResponseEntity.ok(cinemaService.getAllCinemas());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Cinema> getCinemaById(@PathVariable Long id) {
//        return cinemaService.getCinemaById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<Cinema> createCinema(@RequestBody Cinema cinema) {
//        return ResponseEntity.ok(cinemaService.createCinema(cinema));
//    }
//
//    @GetMapping("/{id}/available-seats")
//    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long id) {
//        return cinemaService.getCinemaById(id)
//                .map(cinema -> ResponseEntity.ok(cinema.getAvailableSeats()))
//                .orElse(ResponseEntity.notFound().build());
//    }
//}
