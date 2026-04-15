package com.example.booking_service.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class CinemaClient {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    public CinemaClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = new RestTemplate();
    }

    public Integer getAvailableSeats(Long cinemaId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("cinema-service");
        if (instances.isEmpty()) {
            throw new RuntimeException("Cinema service not available");
        }

        String baseUrl = instances.get(0).getUri().toString();
        String url = baseUrl + "/api/cinemas/" + cinemaId + "/available-seats";
        
        return restTemplate.getForObject(url, Integer.class);
    }
}
