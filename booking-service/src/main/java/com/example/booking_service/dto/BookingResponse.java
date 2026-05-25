package com.example.booking_service.dto;

import com.example.booking_service.entity.Booking;
import com.example.booking_service.entity.BookingProduct;
import com.example.booking_service.entity.BookingSeat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    
    private Long id;
    private String bookingReference;
    private Long userId;
    private Long showtimeId;
    private Long cinemaId;
    private Long movieId;
    private Integer totalSeats;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private String paymentStatus;
    private LocalDateTime bookingDate;
    private LocalDateTime expiryTime;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private List<SeatItem> seats;
    private List<ProductItem> products;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BookingResponse fromEntity(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                .cinemaId(booking.getCinemaId())
                .movieId(booking.getMovieId())
                .totalSeats(booking.getTotalSeats())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus().name())
                .paymentStatus(booking.getPaymentStatus().name())
                .bookingDate(booking.getBookingDate())
                .expiryTime(booking.getExpiryTime())
                .confirmedAt(booking.getConfirmedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .seats(booking.getBookingSeats() == null ? List.of() : booking.getBookingSeats().stream()
                        .map(SeatItem::fromEntity)
                        .toList())
                .products(booking.getBookingProducts() == null ? List.of() : booking.getBookingProducts().stream()
                        .map(ProductItem::fromEntity)
                        .toList())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatItem {
        private Long seatId;
        private String seatRow;
        private Integer seatNumber;
        private String seatLabel;
        private String seatType;
        private BigDecimal seatPrice;

        public static SeatItem fromEntity(BookingSeat seat) {
            return SeatItem.builder()
                    .seatId(seat.getSeatId())
                    .seatRow(seat.getSeatRow())
                    .seatNumber(seat.getSeatNumber())
                    .seatLabel(seat.getSeatRow() + seat.getSeatNumber())
                    .seatType(seat.getSeatType().name())
                    .seatPrice(seat.getPrice())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public static ProductItem fromEntity(BookingProduct product) {
            BigDecimal unitPrice = product.getUnitPrice() != null ? product.getUnitPrice() : BigDecimal.ZERO;
            Integer quantity = product.getQuantity() != null ? product.getQuantity() : 0;
            return ProductItem.builder()
                    .productId(product.getProductId())
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                    .build();
        }
    }
}
