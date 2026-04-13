//package com.example.cinema_service.service;
//
//import com.example.cinema_service.repository.CinemaRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class CinemaService {
//
//    @Autowired
//    private CinemaRepository cinemaRepository;
//
//    public List<Cinema> getAllCinemas() {
//        return cinemaRepository.findAll();
//    }
//
//    public Optional<Cinema> getCinemaById(Long id) {
//        return cinemaRepository.findById(id);
//    }
//
//    public Cinema createCinema(Cinema cinema) {
//        return cinemaRepository.save(cinema);
//    }
//
//    public boolean reserveSeats(Long cinemaId, int numberOfSeats) {
//        Optional<Cinema> cinemaOpt = cinemaRepository.findById(cinemaId);
//        if (cinemaOpt.isPresent()) {
//            Cinema cinema = cinemaOpt.get();
//            if (cinema.getAvailableSeats() >= numberOfSeats) {
//                cinema.setAvailableSeats(cinema.getAvailableSeats() - numberOfSeats);
//                cinemaRepository.save(cinema);
//                return true;
//            }
//        }
//        return false;
//    }
//}
