package com.example.cinema_service.service;

import com.example.cinema_service.dto.CinemaDTO;
import com.example.cinema_service.dto.ScreenDTO;
import com.example.cinema_service.entity.Cinema;
import com.example.cinema_service.entity.Screen;
import com.example.cinema_service.mapper.CinemaMapper;
import com.example.cinema_service.repository.CinemaRepository;
import com.example.cinema_service.repository.ScreenRepository;
import com.example.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;

    public List<CinemaDTO> getAllCinemas() {
        return cinemaRepository.findAll().stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CinemaDTO getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));
        return CinemaMapper.toDTO(cinema);
    }

    @Transactional
    public CinemaDTO createCinema(Cinema cinema) {
        Cinema saved = cinemaRepository.save(cinema);
        return CinemaMapper.toDTO(saved);
    }

    @Transactional
    public CinemaDTO updateCinema(Long id, Cinema cinemaDetails) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));
        
        cinema.setName(cinemaDetails.getName());
        cinema.setAddress(cinemaDetails.getAddress());
        cinema.setCity(cinemaDetails.getCity());
        cinema.setState(cinemaDetails.getState());
        cinema.setPhoneNumber(cinemaDetails.getPhoneNumber());
        cinema.setEmail(cinemaDetails.getEmail());
        cinema.setDescription(cinemaDetails.getDescription());
        cinema.setStatus(cinemaDetails.getStatus());
        
        Cinema updated = cinemaRepository.save(cinema);
        return CinemaMapper.toDTO(updated);
    }

    @Transactional
    public void deleteCinema(Long id) {
        if (!cinemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cinema", "id", id);
        }
        cinemaRepository.deleteById(id);
    }

    public List<ScreenDTO> getScreensByCinema(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new ResourceNotFoundException("Cinema", "id", cinemaId);
        }
        return screenRepository.findByCinemaId(cinemaId).stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }
}
