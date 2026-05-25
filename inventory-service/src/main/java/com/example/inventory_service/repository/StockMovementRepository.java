package com.example.inventory_service.repository;

import com.example.inventory_service.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByCinemaInventoryIdOrderByCreatedAtDesc(Long cinemaInventoryId);
}
