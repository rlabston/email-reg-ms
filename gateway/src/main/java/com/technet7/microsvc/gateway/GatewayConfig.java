package com.technet7.microsvc.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure routes for Spring Cloud Gateway.
 * - /api/** routes to backend microservice
 * - Static files (Angular app) served from classpath:/static/
 */
@Configuration
public class GatewayConfig {

    @Value("${gateway.backend.base-url:http://127.0.0.1:8081}")
    private String backendBase;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route API calls to backend microservice
                .route("api_to_backend", r -> r.path("/api/**")
                        .uri(backendBase))
                // Route chatbot page to backend
                .route("chatbot_to_backend", r -> r.path("/chatbot.html")
                        .uri(backendBase))
                .build();
    }
}
