package com.example.booking_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    
    // Queue names
    public static final String BOOKING_CREATED_QUEUE = "booking.created.queue";
    public static final String BOOKING_CONFIRMED_QUEUE = "booking.confirmed.queue";
    public static final String BOOKING_CANCELLED_QUEUE = "booking.cancelled.queue";
    
    // Routing keys
    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";
    public static final String BOOKING_CONFIRMED_ROUTING_KEY = "booking.confirmed";
    public static final String BOOKING_CANCELLED_ROUTING_KEY = "booking.cancelled";

    // Exchange
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(BOOKING_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", BOOKING_EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue bookingConfirmedQueue() {
        return QueueBuilder.durable(BOOKING_CONFIRMED_QUEUE)
                .withArgument("x-dead-letter-exchange", BOOKING_EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(BOOKING_CANCELLED_QUEUE)
                .withArgument("x-dead-letter-exchange", BOOKING_EXCHANGE + ".dlx")
                .build();
    }

    // Bindings
    @Bean
    public Binding bookingCreatedBinding(Queue bookingCreatedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingCreatedQueue)
                .to(bookingExchange)
                .with(BOOKING_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingConfirmedQueue)
                .to(bookingExchange)
                .with(BOOKING_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding bookingCancelledBinding(Queue bookingCancelledQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingCancelledQueue)
                .to(bookingExchange)
                .with(BOOKING_CANCELLED_ROUTING_KEY);
    }

    // Message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
