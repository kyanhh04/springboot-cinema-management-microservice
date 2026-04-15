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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;

    public List<CinemaDTO> getAllCinemas() {
        try {
            return cinemaRepository.findAll().stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all cinemas", e);
            throw e;
        }
    }

    public CinemaDTO getCinemaById(Long id) {
        try {
            Cinema cinema = cinemaRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Cinema not found with ID: {}", id);
                        return new ResourceNotFoundException("Cinema", "id", id);
                    });
            return CinemaMapper.toDTO(cinema);
        } catch (Exception e) {
            log.error("Error fetching cinema by ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public CinemaDTO createCinema(Cinema cinema) {
        try {
            Cinema saved = cinemaRepository.save(cinema);
            return CinemaMapper.toDTO(saved);
        } catch (Exception e) {
            log.error("Error creating cinema: {}", cinema.getName(), e);
            throw e;
        }
    }

    @Transactional
    public CinemaDTO updateCinema(Long id, Cinema cinemaDetails) {
        try {
            Cinema cinema = cinemaRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Cinema not found with ID: {}", id);
                        return new ResourceNotFoundException("Cinema", "id", id);
                    });
            
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
        } catch (Exception e) {
            log.error("Error updating cinema with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteCinema(Long id) {
        try {
            if (!cinemaRepository.existsById(id)) {
                log.error("Cinema not found with ID: {}", id);
                throw new ResourceNotFoundException("Cinema", "id", id);
            }
            cinemaRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting cinema with ID: {}", id, e);
            throw e;
        }
    }

    public List<ScreenDTO> getScreensByCinema(Long cinemaId) {
        try {
            if (!cinemaRepository.existsById(cinemaId)) {
                log.error("Cinema not found with ID: {}", cinemaId);
                throw new ResourceNotFoundException("Cinema", "id", cinemaId);
            }
            return screenRepository.findByCinemaId(cinemaId).stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching screens for cinema ID: {}", cinemaId, e);
            throw e;
        }
    }
}
