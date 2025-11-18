package com.technet7.microsvc.email.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var auth = jwtService.buildAuthentication(token);
                    if (auth instanceof AbstractAuthenticationToken aat) {
                        aat.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    }
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // Sliding renewal: issue a refreshed token that expires slidingExpirationMs from now
                    try {
                        var jws = jwtService.parseClaims(token);
                        var claims = jws.getBody();
                        String subject = claims.getSubject();
                        String username = claims.get("username", String.class);
                        @SuppressWarnings("unchecked")
                        java.util.Collection<String> rolesCol = (java.util.Collection<String>) claims.get("roles", java.util.Collection.class);
                        java.util.Set<String> roles = rolesCol == null ? java.util.Set.of() : java.util.Set.copyOf(rolesCol);
                        long ttl = jwtService.getSlidingExpirationMs();
                        String refreshed = jwtService.generateTokenWithTtl(subject, username, roles, ttl);
                        // Return refreshed token in a response header so client can update stored token
                        response.setHeader("X-New-JWT", refreshed);
                        response.setHeader("X-JWT-Expires-In", String.valueOf(ttl));
                    } catch (Exception e) {
                        // If anything fails while refreshing, ignore and continue with existing auth
                    }
                }
            }
        } catch (Exception e) {
            // On any JWT parsing/validation error, continue without setting auth
        }
        filterChain.doFilter(request, response);
    }
}
