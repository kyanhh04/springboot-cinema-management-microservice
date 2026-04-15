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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public List<ShowtimeDTO> getAllShowtimes() {
        try {
            return showtimeRepository.findAll().stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all showtimes", e);
            throw e;
        }
    }

    public ShowtimeDTO getShowtimeById(Long id) {
        try {
            Showtime showtime = showtimeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Showtime not found with ID: {}", id);
                        return new ResourceNotFoundException("Showtime", "id", id);
                    });
            return CinemaMapper.toDTO(showtime);
        } catch (Exception e) {
            log.error("Error fetching showtime by ID: {}", id, e);
            throw e;
        }
    }

    public List<ShowtimeDTO> getShowtimesByMovie(Long movieId, LocalDate date) {
        try {
            if (!movieRepository.existsById(movieId)) {
                log.error("Movie not found with ID: {}", movieId);
                throw new ResourceNotFoundException("Movie", "id", movieId);
            }
            return showtimeRepository.findByMovieIdAndShowDate(movieId, date).stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching showtimes for movie ID: {} on date: {}", movieId, date, e);
            throw e;
        }
    }

    public List<ShowtimeDTO> getShowtimesByMovieAndCinema(Long movieId, Long cinemaId, LocalDate date) {
        try {
            return showtimeRepository.findByMovieAndCinemaAndDate(movieId, cinemaId, date).stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching showtimes for movie ID: {} at cinema ID: {} on date: {}", movieId, cinemaId, date, e);
            throw e;
        }
    }

    public List<ShowtimeDTO> getShowtimesByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            return showtimeRepository.findByDateRange(startDate, endDate).stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching showtimes by date range: {} to {}", startDate, endDate, e);
            throw e;
        }
    }

    @Transactional
    public ShowtimeDTO createShowtime(Showtime showtime) {
        try {
            Movie movie = movieRepository.findById(showtime.getMovie().getId())
                    .orElseThrow(() -> {
                        log.error("Movie not found with ID: {}", showtime.getMovie().getId());
                        return new ResourceNotFoundException("Movie", "id", showtime.getMovie().getId());
                    });
            
            Screen screen = screenRepository.findById(showtime.getScreen().getId())
                    .orElseThrow(() -> {
                        log.error("Screen not found with ID: {}", showtime.getScreen().getId());
                        return new ResourceNotFoundException("Screen", "id", showtime.getScreen().getId());
                    });
            
            showtime.setMovie(movie);
            showtime.setScreen(screen);
            showtime.setAvailableSeats(screen.getTotalSeats());
            
            Showtime saved = showtimeRepository.save(showtime);
            return CinemaMapper.toDTO(saved);
        } catch (Exception e) {
            log.error("Error creating showtime", e);
            throw e;
        }
    }

    @Transactional
    public ShowtimeDTO updateShowtime(Long id, Showtime showtimeDetails) {
        try {
            Showtime showtime = showtimeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Showtime not found with ID: {}", id);
                        return new ResourceNotFoundException("Showtime", "id", id);
                    });
            
            showtime.setShowDate(showtimeDetails.getShowDate());
            showtime.setShowTime(showtimeDetails.getShowTime());
            showtime.setEndTime(showtimeDetails.getEndTime());
            showtime.setBasePrice(showtimeDetails.getBasePrice());
            showtime.setStatus(showtimeDetails.getStatus());
            
            Showtime updated = showtimeRepository.save(showtime);
            return CinemaMapper.toDTO(updated);
        } catch (Exception e) {
            log.error("Error updating showtime with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteShowtime(Long id) {
        try {
            if (!showtimeRepository.existsById(id)) {
                log.error("Showtime not found with ID: {}", id);
                throw new ResourceNotFoundException("Showtime", "id", id);
            }
            showtimeRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting showtime with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public boolean reserveSeats(Long showtimeId, int numberOfSeats) {
        try {
            Showtime showtime = showtimeRepository.findById(showtimeId)
                    .orElseThrow(() -> {
                        log.error("Showtime not found with ID: {}", showtimeId);
                        return new ResourceNotFoundException("Showtime", "id", showtimeId);
                    });
            
            if (showtime.getAvailableSeats() < numberOfSeats) {
                log.error("Not enough seats available for showtime ID: {}. Requested: {}, Available: {}", 
                        showtimeId, numberOfSeats, showtime.getAvailableSeats());
                throw new BusinessException("Not enough available seats");
            }
            
            showtime.setAvailableSeats(showtime.getAvailableSeats() - numberOfSeats);
            showtimeRepository.save(showtime);
            return true;
        } catch (Exception e) {
            log.error("Error reserving {} seats for showtime ID: {}", numberOfSeats, showtimeId, e);
            throw e;
        }
    }

    @Transactional
    public void releaseSeats(Long showtimeId, int numberOfSeats) {
        try {
            Showtime showtime = showtimeRepository.findById(showtimeId)
                    .orElseThrow(() -> {
                        log.error("Showtime not found with ID: {}", showtimeId);
                        return new ResourceNotFoundException("Showtime", "id", showtimeId);
                    });
            
            showtime.setAvailableSeats(showtime.getAvailableSeats() + numberOfSeats);
            showtimeRepository.save(showtime);
        } catch (Exception e) {
            log.error("Error releasing {} seats for showtime ID: {}", numberOfSeats, showtimeId, e);
            throw e;
        }
    }
}
