package com.example.cinema_service.service;

import com.example.cinema_service.dto.ScreenDTO;
import com.example.cinema_service.dto.SeatDTO;
import com.example.cinema_service.entity.Cinema;
import com.example.cinema_service.entity.Screen;
import com.example.cinema_service.entity.Seat;
import com.example.cinema_service.mapper.CinemaMapper;
import com.example.cinema_service.repository.CinemaRepository;
import com.example.cinema_service.repository.ScreenRepository;
import com.example.cinema_service.repository.SeatRepository;
import com.example.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final CinemaRepository cinemaRepository;
    private final SeatRepository seatRepository;

    public ScreenDTO getScreenById(Long id) {
        try {
            Screen screen = screenRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Screen not found with ID: {}", id);
                        return new ResourceNotFoundException("Screen", "id", id);
                    });
            return CinemaMapper.toDTO(screen);
        } catch (Exception e) {
            log.error("Error fetching screen by ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public ScreenDTO createScreen(Screen screen) {
        try {
            Cinema cinema = cinemaRepository.findById(screen.getCinema().getId())
                    .orElseThrow(() -> {
                        log.error("Cinema not found with ID: {}", screen.getCinema().getId());
                        return new ResourceNotFoundException("Cinema", "id", screen.getCinema().getId());
                    });
            
            screen.setCinema(cinema);
            Screen saved = screenRepository.save(screen);
            return CinemaMapper.toDTO(saved);
        } catch (Exception e) {
            log.error("Error creating screen", e);
            throw e;
        }
    }

    @Transactional
    public ScreenDTO updateScreen(Long id, Screen screenDetails) {
        try {
            Screen screen = screenRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Screen not found with ID: {}", id);
                        return new ResourceNotFoundException("Screen", "id", id);
                    });
            
            screen.setScreenNumber(screenDetails.getScreenNumber());
            screen.setTotalSeats(screenDetails.getTotalSeats());
            screen.setStatus(screenDetails.getStatus());
            
            Screen updated = screenRepository.save(screen);
            return CinemaMapper.toDTO(updated);
        } catch (Exception e) {
            log.error("Error updating screen with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteScreen(Long id) {
        try {
            if (!screenRepository.existsById(id)) {
                log.error("Screen not found with ID: {}", id);
                throw new ResourceNotFoundException("Screen", "id", id);
            }
            screenRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting screen with ID: {}", id, e);
            throw e;
        }
    }

    public List<SeatDTO> getSeatsByScreen(Long screenId) {
        try {
            if (!screenRepository.existsById(screenId)) {
                log.error("Screen not found with ID: {}", screenId);
                throw new ResourceNotFoundException("Screen", "id", screenId);
            }
            return seatRepository.findByScreenId(screenId).stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching seats for screen ID: {}", screenId, e);
            throw e;
        }
    }

    @Transactional
    public List<SeatDTO> generateSeatsForScreen(Long screenId, int rows, int seatsPerRow) {
        try {
            Screen screen = screenRepository.findById(screenId)
                    .orElseThrow(() -> {
                        log.error("Screen not found with ID: {}", screenId);
                        return new ResourceNotFoundException("Screen", "id", screenId);
                    });
            
            List<Seat> seats = new ArrayList<>();
            String[] rowLabels = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
            
            for (int i = 0; i < rows && i < rowLabels.length; i++) {
                for (int j = 1; j <= seatsPerRow; j++) {
                    Seat seat = new Seat();
                    seat.setScreen(screen);
                    seat.setSeatRow(rowLabels[i]);
                    seat.setSeatNumber(j);
                    seat.setSeatType(Seat.SeatType.REGULAR);
                    seat.setStatus(Seat.SeatStatus.AVAILABLE);
                    seats.add(seat);
                }
            }
            
            List<Seat> savedSeats = seatRepository.saveAll(seats);
            screen.setTotalSeats(savedSeats.size());
            screenRepository.save(screen);
            
            return savedSeats.stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error generating seats for screen ID: {}", screenId, e);
            throw e;
        }
    }
}
