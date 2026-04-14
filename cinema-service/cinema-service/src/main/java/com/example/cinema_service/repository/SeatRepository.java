package com.example.cinema_service.repository;

import com.example.cinema_service.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long screenId);
    List<Seat> findByScreenIdAndStatus(Long screenId, Seat.SeatStatus status);
}
