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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final CinemaRepository cinemaRepository;
    private final SeatRepository seatRepository;

    public ScreenDTO getScreenById(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", id));
        return CinemaMapper.toDTO(screen);
    }

    @Transactional
    public ScreenDTO createScreen(Screen screen) {
        Cinema cinema = cinemaRepository.findById(screen.getCinema().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", screen.getCinema().getId()));
        
        screen.setCinema(cinema);
        Screen saved = screenRepository.save(screen);
        return CinemaMapper.toDTO(saved);
    }

    @Transactional
    public ScreenDTO updateScreen(Long id, Screen screenDetails) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", id));
        
        screen.setScreenNumber(screenDetails.getScreenNumber());
        screen.setTotalSeats(screenDetails.getTotalSeats());
        screen.setStatus(screenDetails.getStatus());
        
        Screen updated = screenRepository.save(screen);
        return CinemaMapper.toDTO(updated);
    }

    @Transactional
    public void deleteScreen(Long id) {
        if (!screenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screen", "id", id);
        }
        screenRepository.deleteById(id);
    }

    public List<SeatDTO> getSeatsByScreen(Long screenId) {
        if (!screenRepository.existsById(screenId)) {
            throw new ResourceNotFoundException("Screen", "id", screenId);
        }
        return seatRepository.findByScreenId(screenId).stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SeatDTO> generateSeatsForScreen(Long screenId, int rows, int seatsPerRow) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", screenId));
        
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
    }
}
