package com.example.inventory_service.config;

import com.example.inventory_service.event.BookingEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String INVENTORY_BOOKING_CONFIRMED_QUEUE = "inventory.booking.confirmed.queue";
    public static final String INVENTORY_BOOKING_CANCELLED_QUEUE = "inventory.booking.cancelled.queue";
    public static final String BOOKING_CONFIRMED_ROUTING_KEY = "booking.confirmed";
    public static final String BOOKING_CANCELLED_ROUTING_KEY = "booking.cancelled";
    public static final String BOOKING_EVENT_TYPE_ID = "bookingEvent";

    @Bean(name = "bookingExchange")
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean(name = "inventoryBookingConfirmedQueue")
    public Queue inventoryBookingConfirmedQueue() {
        return QueueBuilder.durable(INVENTORY_BOOKING_CONFIRMED_QUEUE).build();
    }

    @Bean(name = "inventoryBookingCancelledQueue")
    public Queue inventoryBookingCancelledQueue() {
        return QueueBuilder.durable(INVENTORY_BOOKING_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding inventoryBookingConfirmedBinding(
            @Qualifier("inventoryBookingConfirmedQueue") Queue inventoryBookingConfirmedQueue,
            @Qualifier("bookingExchange") TopicExchange bookingExchange) {
        return BindingBuilder.bind(inventoryBookingConfirmedQueue)
                .to(bookingExchange)
                .with(BOOKING_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryBookingCancelledBinding(
            @Qualifier("inventoryBookingCancelledQueue") Queue inventoryBookingCancelledQueue,
            @Qualifier("bookingExchange") TopicExchange bookingExchange) {
        return BindingBuilder.bind(inventoryBookingCancelledQueue)
                .to(bookingExchange)
                .with(BOOKING_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
                BOOKING_EVENT_TYPE_ID, BookingEvent.class,
                "com.example.booking_service.event.BookingEvent", BookingEvent.class
        ));

        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setClassMapper(classMapper);
        return converter;
    }
}
