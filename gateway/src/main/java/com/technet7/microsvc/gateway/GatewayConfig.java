package com.technet7.microsvc.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure simple routes for Spring Cloud Gateway. By default all paths are forwarded
 * to the upstream backend configured by `gateway.backend.base-url`.
 */
@Configuration
public class GatewayConfig {

    @Value("${gateway.backend.base-url:http://127.0.0.1:8081}")
    private String backendBase;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        // Forward everything to backendBase preserving path and query
        return builder.routes()
                .route("all_to_backend", r -> r.path("/**")
                        .uri(backendBase))
                .build();
    }
}
