package com.example.inventory_service.repository;

import com.example.inventory_service.entity.CinemaInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CinemaInventoryRepository extends JpaRepository<CinemaInventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select inventory
            from CinemaInventory inventory
            where inventory.cinemaId = :cinemaId
              and inventory.product.id = :productId
            """)
    Optional<CinemaInventory> findByCinemaIdAndProductIdForUpdate(
            @Param("cinemaId") Long cinemaId,
            @Param("productId") Long productId);

    @Query("""
            select inventory
            from CinemaInventory inventory
            where inventory.cinemaId = :cinemaId
              and inventory.product.id = :productId
            """)
    Optional<CinemaInventory> findByCinemaIdAndProductId(
            @Param("cinemaId") Long cinemaId,
            @Param("productId") Long productId);

    List<CinemaInventory> findByCinemaId(Long cinemaId);

    @Query("""
            select inventory
            from CinemaInventory inventory
            where inventory.quantity <= inventory.minStockLevel
            """)
    List<CinemaInventory> findLowStock();
}
