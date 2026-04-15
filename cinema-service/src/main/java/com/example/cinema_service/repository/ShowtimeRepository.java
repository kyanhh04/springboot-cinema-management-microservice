package com.example.cinema_service.repository;

import com.example.cinema_service.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);
    
    List<Showtime> findByScreenIdAndShowDate(Long screenId, LocalDate showDate);
    
    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.screen.cinema.id = :cinemaId AND s.showDate = :showDate")
    List<Showtime> findByMovieAndCinemaAndDate(
        @Param("movieId") Long movieId, 
        @Param("cinemaId") Long cinemaId, 
        @Param("showDate") LocalDate showDate
    );
    
    @Query("SELECT s FROM Showtime s WHERE s.showDate >= :startDate AND s.showDate <= :endDate ORDER BY s.showDate, s.showTime")
    List<Showtime> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
