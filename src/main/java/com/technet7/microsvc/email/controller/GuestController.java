package com.technet7.microsvc.email.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.technet7.microsvc.email.security.JwtService;

/**
 * Minimal controller to issue short-lived guest JWTs for anonymous visitors.
 * The JWT contains roles=["GUEST"] and a subject of the form "guest:<uuid>".
 */
@RestController
@RequestMapping("/auth")
public class GuestController {

    private final JwtService jwtService;

    public GuestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/guest")
    public ResponseEntity<GuestTokenResponse> guestToken() {
        String subject = "guest:" + UUID.randomUUID().toString();
        String username = "guest";
        Set<String> roles = Set.of("GUEST");
        String token = jwtService.generateToken(subject, username, roles);
        long expiresIn = jwtService.getExpirationMs();
        GuestTokenResponse resp = new GuestTokenResponse(token, expiresIn, roles);
        return ResponseEntity.ok(resp);
    }

    public static final class GuestTokenResponse {
        private final String token;
        private final long expiresIn;
        private final Set<String> roles;

        public GuestTokenResponse(String token, long expiresIn, Set<String> roles) {
            this.token = token;
            this.expiresIn = expiresIn;
            this.roles = roles;
        }

        public String getToken() {
            return token;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public Set<String> getRoles() {
            return roles;
        }
    }
}
