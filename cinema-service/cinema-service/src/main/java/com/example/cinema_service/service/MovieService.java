package com.example.cinema_service.service;

import com.example.cinema_service.dto.MovieDTO;
import com.example.cinema_service.entity.Movie;
import com.example.cinema_service.mapper.CinemaMapper;
import com.example.cinema_service.repository.MovieRepository;
import com.example.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return CinemaMapper.toDTO(movie);
    }

    public List<MovieDTO> getNowShowingMovies() {
        return movieRepository.findNowShowingMovies().stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MovieDTO> getComingSoonMovies() {
        return movieRepository.findComingSoonMovies().stream()
                .map(CinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MovieDTO createMovie(Movie movie) {
        Movie saved = movieRepository.save(movie);
        return CinemaMapper.toDTO(saved);
    }

    @Transactional
    public MovieDTO updateMovie(Long id, Movie movieDetails) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
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
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie", "id", id);
        }
        movieRepository.deleteById(id);
    }
}
