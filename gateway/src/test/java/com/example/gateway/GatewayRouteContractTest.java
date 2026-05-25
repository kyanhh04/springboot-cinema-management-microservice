package com.example.gateway;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRouteContractTest {

    @Test
    void gatewayRoutesExposeAllServiceApiPrefixes() throws Exception {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        }

        assertThat(properties.getProperty("spring.cloud.gateway.mvc.routes[0].predicates[0]"))
                .contains("/api/auth/**", "/api/users/**");
        assertThat(properties.getProperty("spring.cloud.gateway.mvc.routes[1].predicates[0]"))
                .contains("/api/cinemas/**", "/api/movies/**", "/api/screens/**", "/api/showtimes/**");
        assertThat(properties.getProperty("spring.cloud.gateway.mvc.routes[2].predicates[0]"))
                .contains("/api/bookings/**", "/api/payments/**");
        assertThat(properties.getProperty("spring.cloud.gateway.mvc.routes[3].predicates[0]"))
                .contains("/api/inventory/**", "/api/products/**");
        assertThat(properties.getProperty("spring.cloud.gateway.mvc.routes[4].predicates[0]"))
                .contains("/api/notifications/**");
    }
}
