package com.technet7.microsvc.email.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Authentication controller for login/logout operations
 */

@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Logout endpoint - invalidates session and returns success response
     * POST /api/auth/logout
     * 
     * This is a stateless API, so logout is primarily a client-side operation.
     * However, this endpoint provides confirmation and could be extended to:
     * - Invalidate tokens (if using JWT)
     * - Clear server-side sessions (if session-based)
     * - Log logout events
     * - Cleanup any server-side cached data
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Invalidate session if it exists
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            response.put("success", true);
            response.put("message", "Logout successful");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Logout failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Return basic info about the currently authenticated principal.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Map<String, Object> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            response.put("authenticated", false);
            return ResponseEntity.status(401).body(response);
        }

        String username = auth.getName();
        response.put("authenticated", true);
        response.put("username", username);

        // Collect roles (strip ROLE_ prefix)
        var roles = auth.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
            .toList();
        response.put("roles", roles);

        return ResponseEntity.ok(response);
    }
}
