package com.example.booking_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    @NotNull(message = "Showtime ID is required")
    private Long showtimeId;
    
    @NotNull(message = "Cinema ID is required")
    private Long cinemaId;
    
    @NotNull(message = "Movie ID is required")
    private Long movieId;
    
    @NotNull(message = "Seat IDs are required")
    private List<Long> seatIds;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    private List<ProductItem> productItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItem {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Product quantity is required")
        @Positive(message = "Product quantity must be positive")
        private Integer quantity;

        @Positive(message = "Product unit price must be positive")
        private BigDecimal unitPrice;
    }
}
