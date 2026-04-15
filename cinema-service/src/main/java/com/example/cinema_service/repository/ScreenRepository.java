package com.example.cinema_service.repository;

import com.example.cinema_service.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByCinemaId(Long cinemaId);
    List<Screen> findByCinemaIdAndStatus(Long cinemaId, Screen.ScreenStatus status);
}
