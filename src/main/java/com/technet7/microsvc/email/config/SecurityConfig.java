package com.technet7.microsvc.email.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.technet7.microsvc.email.security.JwtAuthenticationFilter;

/**
 * Security configuration for password encoding and endpoint security.
 * Provides a PasswordEncoder bean for use throughout the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())  // Disable CSRF for API endpoints
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Admin endpoints require ADMIN
                .requestMatchers("/admin/**").hasRole(Roles.ADMIN)
                // DELETE operations on emails must be ADMIN
                .requestMatchers(HttpMethod.DELETE, "/emails/**").hasRole(Roles.ADMIN)
                // Registration and login remain public
                .requestMatchers(HttpMethod.POST, "/emails/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/emails/login").permitAll()
                // Listing of all emails requires authenticated user (USER or ADMIN)
                .requestMatchers(HttpMethod.GET, "/emails", "/emails/list").hasAnyRole(Roles.ADMIN, Roles.USER)
                // Single-email GET can be accessed by guest, user, or admin
                .requestMatchers(HttpMethod.GET, "/emails/*").hasAnyRole(Roles.ADMIN, Roles.USER, Roles.GUEST)
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/mindsdb/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {});  // Enable HTTP Basic authentication for MindsDB endpoints

        // Add JWT auth filter for SPA token-based authentication
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://127.0.0.1:4200",
            // common static/dev servers used locally
            "http://localhost:8000",
            "http://127.0.0.1:8000",
            "http://localhost:8081",
            "http://127.0.0.1:8081",
            // gateway public entrypoint
            "http://localhost:8080",
            "http://127.0.0.1:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
