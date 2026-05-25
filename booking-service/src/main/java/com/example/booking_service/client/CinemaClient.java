package com.example.booking_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class CinemaClient {

    private static final String CINEMA_SERVICE_CIRCUIT = "cinemaService";

    private final DiscoveryClient discoveryClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final RestTemplate restTemplate;

    public CinemaClient(DiscoveryClient discoveryClient, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.discoveryClient = discoveryClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.restTemplate = new RestTemplate();
    }

    public Integer getAvailableSeats(Long cinemaId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CINEMA_SERVICE_CIRCUIT);
        return circuitBreaker.run(() -> {
            String baseUrl = getCinemaServiceBaseUrl();
            String url = baseUrl + "/api/cinemas/" + cinemaId + "/available-seats";
            return restTemplate.getForObject(url, Integer.class);
        }, throwable -> fallbackAvailableSeats(cinemaId, throwable));
    }

    public SeatInfo getSeatById(Long seatId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CINEMA_SERVICE_CIRCUIT);
        return circuitBreaker.run(() -> {
            String baseUrl = getCinemaServiceBaseUrl();
            String url = baseUrl + "/api/screens/seats/" + seatId;
            return restTemplate.getForObject(url, SeatInfo.class);
        }, throwable -> fallbackSeatInfo(seatId, throwable));
    }

    public void reserveSeats(Long showtimeId, int seats) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CINEMA_SERVICE_CIRCUIT);
        circuitBreaker.run(() -> {
            postShowtimeSeatOperation(showtimeId, seats, "reserve");
            return true;
        }, throwable -> fallbackSeatOperation(showtimeId, seats, "reserve", throwable));
    }

    public void releaseSeats(Long showtimeId, int seats) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CINEMA_SERVICE_CIRCUIT);
        circuitBreaker.run(() -> {
            postShowtimeSeatOperation(showtimeId, seats, "release");
            return true;
        }, throwable -> fallbackSeatOperation(showtimeId, seats, "release", throwable));
    }

    private void postShowtimeSeatOperation(Long showtimeId, int seats, String operation) {
        String baseUrl = getCinemaServiceBaseUrl();
        String url = baseUrl + "/api/showtimes/" + showtimeId + "/" + operation + "?seats=" + seats;
        try {
            restTemplate.postForEntity(url, null, Void.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to " + operation + " seats for showtime: " + showtimeId, e);
        }
    }

    private String getCinemaServiceBaseUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("cinema-service");
        if (instances.isEmpty()) {
            throw new RuntimeException("Cinema service not available");
        }
        return instances.get(0).getUri().toString();
    }

    private Integer fallbackAvailableSeats(Long cinemaId, Throwable throwable) {
        log.warn("Fallback getAvailableSeats for cinemaId={}: {}", cinemaId, throwable.getMessage());
        throw new RuntimeException("Cinema service is temporarily unavailable. Cannot check available seats.", throwable);
    }

    private SeatInfo fallbackSeatInfo(Long seatId, Throwable throwable) {
        log.warn("Fallback getSeatById for seatId={}: {}", seatId, throwable.getMessage());
        throw new RuntimeException("Cinema service is temporarily unavailable. Cannot load seat information.", throwable);
    }

    private Boolean fallbackSeatOperation(Long showtimeId, int seats, String operation, Throwable throwable) {
        log.warn("Fallback {} seats for showtimeId={}, seats={}: {}",
                operation, showtimeId, seats, throwable.getMessage());
        throw new RuntimeException("Cinema service is temporarily unavailable. Cannot " + operation + " seats.", throwable);
    }

    public static class SeatInfo {
        private Long id;
        private String seatRow;
        private Integer seatNumber;
        private String seatType;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSeatRow() {
            return seatRow;
        }

        public void setSeatRow(String seatRow) {
            this.seatRow = seatRow;
        }

        public Integer getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(Integer seatNumber) {
            this.seatNumber = seatNumber;
        }

        public String getSeatType() {
            return seatType;
        }

        public void setSeatType(String seatType) {
            this.seatType = seatType;
        }
    }
}
