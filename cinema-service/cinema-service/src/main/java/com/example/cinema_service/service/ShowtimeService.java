package com.example.cinema_service.service;

import com.example.cinema_service.dto.ShowtimeDTO;
import com.example.cinema_service.entity.Movie;
import com.example.cinema_service.entity.Screen;
import com.example.cinema_service.entity.Showtime;
import com.example.cinema_service.mapper.CinemaMapper;
import com.example.cinema_service.repository.MovieRepository;
import com.example.cinema_service.repository.ScreenRepository;
import com.example.cinema_service.repository.ShowtimeRepository;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public List<ShowtimeDTO> getAllShowtimes() {
        return showtimeRepository.findAll().stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ShowtimeDTO getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", id));
        return CinemaMapper.toDTO(showtime);
    }

    public List<ShowtimeDTO> getShowtimesByMovie(Long movieId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie", "id", movieId);
        }
        return showtimeRepository.findByMovieIdAndShowDate(movieId, date).stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ShowtimeDTO> getShowtimesByMovieAndCinema(Long movieId, Long cinemaId, LocalDate date) {
        return showtimeRepository.findByMovieAndCinemaAndDate(movieId, cinemaId, date).stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ShowtimeDTO> getShowtimesByDateRange(LocalDate startDate, LocalDate endDate) {
        return showtimeRepository.findByDateRange(startDate, endDate).stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShowtimeDTO createShowtime(Showtime showtime) {
        Movie movie = movieRepository.findById(showtime.getMovie().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", showtime.getMovie().getId()));
        
        Screen screen = screenRepository.findById(showtime.getScreen().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", showtime.getScreen().getId()));
        
        showtime.setMovie(movie);
        showtime.setScreen(screen);
        showtime.setAvailableSeats(screen.getTotalSeats());
        
        Showtime saved = showtimeRepository.save(showtime);
        return CinemaMapper.toDTO(saved);
    }

    @Transactional
    public ShowtimeDTO updateShowtime(Long id, Showtime showtimeDetails) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", id));
        
        showtime.setShowDate(showtimeDetails.getShowDate());
        showtime.setShowTime(showtimeDetails.getShowTime());
        showtime.setEndTime(showtimeDetails.getEndTime());
        showtime.setBasePrice(showtimeDetails.getBasePrice());
        showtime.setStatus(showtimeDetails.getStatus());
        
        Showtime updated = showtimeRepository.save(showtime);
        return CinemaMapper.toDTO(updated);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Showtime", "id", id);
        }
        showtimeRepository.deleteById(id);
    }

    @Transactional
    public boolean reserveSeats(Long showtimeId, int numberOfSeats) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", showtimeId));
        
        if (showtime.getAvailableSeats() < numberOfSeats) {
            throw new BusinessException("Not enough available seats");
        }
        
        showtime.setAvailableSeats(showtime.getAvailableSeats() - numberOfSeats);
        showtimeRepository.save(showtime);
        return true;
    }

    @Transactional
    public void releaseSeats(Long showtimeId, int numberOfSeats) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", showtimeId));
        
        showtime.setAvailableSeats(showtime.getAvailableSeats() + numberOfSeats);
        showtimeRepository.save(showtime);
    }
}
