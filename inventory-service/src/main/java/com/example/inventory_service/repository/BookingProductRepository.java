package com.example.inventory_service.repository;

import com.example.inventory_service.entity.BookingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingProductRepository extends JpaRepository<BookingProduct, Long> {
    boolean existsByBookingId(Long bookingId);

    List<BookingProduct> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);
}
