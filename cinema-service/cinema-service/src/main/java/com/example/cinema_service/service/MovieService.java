package com.example.cinema_service.service;

import com.example.cinema_service.dto.MovieDTO;
import com.example.cinema_service.entity.Movie;
import com.example.cinema_service.mapper.CinemaMapper;
import com.example.cinema_service.repository.MovieRepository;
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
public class MovieService {

    private final MovieRepository movieRepository;

    public List<MovieDTO> getAllMovies() {
        try {
            return movieRepository.findAll().stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all movies", e);
            throw e;
        }
    }

    public MovieDTO getMovieById(Long id) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Movie not found with ID: {}", id);
                        return new ResourceNotFoundException("Movie", "id", id);
                    });
            return CinemaMapper.toDTO(movie);
        } catch (Exception e) {
            log.error("Error fetching movie by ID: {}", id, e);
            throw e;
        }
    }

    public List<MovieDTO> getNowShowingMovies() {
        try {
            return movieRepository.findNowShowingMovies().stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching now showing movies", e);
            throw e;
        }
    }

    public List<MovieDTO> getComingSoonMovies() {
        try {
            return movieRepository.findComingSoonMovies().stream()
                    .map(CinemaMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching coming soon movies", e);
            throw e;
        }
    }

    @Transactional
    public MovieDTO createMovie(Movie movie) {
        try {
            Movie saved = movieRepository.save(movie);
            return CinemaMapper.toDTO(saved);
        } catch (Exception e) {
            log.error("Error creating movie: {}", movie.getTitle(), e);
            throw e;
        }
    }

    @Transactional
    public MovieDTO updateMovie(Long id, Movie movieDetails) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Movie not found with ID: {}", id);
                        return new ResourceNotFoundException("Movie", "id", id);
                    });
            
            movie.setTitle(movieDetails.getTitle());
            movie.setDescription(movieDetails.getDescription());
            movie.setDurationMinutes(movieDetails.getDurationMinutes());
            movie.setGenre(movieDetails.getGenre());
            movie.setSubtitleType(movieDetails.getSubtitleType());
            movie.setReleaseDate(movieDetails.getReleaseDate());
            movie.setDirector(movieDetails.getDirector());
            movie.setCast(movieDetails.getCast());
            movie.setRating(movieDetails.getRating());
            movie.setPosterUrl(movieDetails.getPosterUrl());
            movie.setTrailerUrl(movieDetails.getTrailerUrl());
            movie.setStatus(movieDetails.getStatus());
            
            Movie updated = movieRepository.save(movie);
            return CinemaMapper.toDTO(updated);
        } catch (Exception e) {
            log.error("Error updating movie with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteMovie(Long id) {
        try {
            if (!movieRepository.existsById(id)) {
                log.error("Movie not found with ID: {}", id);
                throw new ResourceNotFoundException("Movie", "id", id);
            }
            movieRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting movie with ID: {}", id, e);
            throw e;
        }
    }
}
