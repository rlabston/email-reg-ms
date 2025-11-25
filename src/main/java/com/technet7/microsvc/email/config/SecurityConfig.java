package com.technet7.microsvc.email.config;

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
            .cors(cors -> cors.disable())  // Disable CORS - gateway handles it
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
                // Chatbot UI
                .requestMatchers("/chatbot.html").authenticated()
                // Listing of all emails requires authenticated user (USER or ADMIN)
                .requestMatchers(HttpMethod.GET, "/emails", "/emails/list").hasAnyRole(Roles.ADMIN, Roles.USER)
                // Single-email GET can be accessed by guest, user, or admin
                .requestMatchers(HttpMethod.GET, "/emails/*").hasAnyRole(Roles.ADMIN, Roles.USER, Roles.GUEST)
                .requestMatchers("/auth/**").permitAll()
                // Chatbot endpoints
                .requestMatchers("/api/chat/health").permitAll()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/mindsdb/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {});  // Enable HTTP Basic authentication for MindsDB endpoints

        // Add JWT auth filter for SPA token-based authentication
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
