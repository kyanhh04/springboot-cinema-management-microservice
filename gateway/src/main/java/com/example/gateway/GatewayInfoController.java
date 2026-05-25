package com.example.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GatewayInfoController {

    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "service", "gateway",
                "status", "UP",
                "message", "Cinema Management API Gateway",
                "health", "http://localhost:8888/actuator/health",
                "routes", List.of(
                        "/api/auth/** -> user-service",
                        "/api/users/** -> user-service",
                        "/api/cinemas/**, /api/movies/**, /api/screens/**, /api/showtimes/** -> cinema-service",
                        "/api/bookings/**, /api/payments/** -> booking-service",
                        "/api/inventory/**, /api/products/** -> inventory-service",
                        "/api/notifications/** -> notification-service"
                )
        );
    }
}
