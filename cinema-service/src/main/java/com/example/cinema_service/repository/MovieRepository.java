package com.example.cinema_service.repository;

import com.example.cinema_service.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByStatus(Movie.MovieStatus status);
    
    @Query("SELECT m FROM Movie m WHERE m.status = 'NOW_SHOWING' ORDER BY m.releaseDate DESC")
    List<Movie> findNowShowingMovies();
    
    @Query("SELECT m FROM Movie m WHERE m.status = 'COMING_SOON' ORDER BY m.releaseDate ASC")
    List<Movie> findComingSoonMovies();
}
