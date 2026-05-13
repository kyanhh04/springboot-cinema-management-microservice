package com.example.inventory_service.listener;

import com.example.inventory_service.config.RabbitMQConfig;
import com.example.inventory_service.event.BookingEvent;
import com.example.inventory_service.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingInventoryEventListener {

    private final StockService stockService;

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_BOOKING_CONFIRMED_QUEUE)
    public void handleBookingConfirmed(BookingEvent event) {
        log.info("Received booking confirmed event for inventory update: {}", event.getBookingReference());
        stockService.deductStockForConfirmedBooking(event);
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_BOOKING_CANCELLED_QUEUE)
    public void handleBookingCancelled(BookingEvent event) {
        log.info("Received booking cancelled event for inventory update: {}", event.getBookingReference());
        stockService.restoreStockForCancelledBooking(event);
    }
}
