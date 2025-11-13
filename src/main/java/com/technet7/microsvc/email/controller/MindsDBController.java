package com.technet7.microsvc.email.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.technet7.microsvc.email.dto.MindsDBQueryRequest;
import com.technet7.microsvc.email.service.MindsDBService;

import jakarta.validation.Valid;

/**
 * REST controller for MindsDB proxy endpoints
 * Provides secure access to MindsDB functionality through the Spring Boot API
 */
@RestController
@RequestMapping("/mindsdb")
public class MindsDBController {

    private static final Logger logger = LoggerFactory.getLogger(MindsDBController.class);

    private final MindsDBService mindsDBService;

    public MindsDBController(MindsDBService mindsDBService) {
        this.mindsDBService = mindsDBService;
    }

    /**
     * Execute a SQL query on MindsDB
     * POST /api/mindsdb/query
     * 
     * Request body: { "query": "SELECT * FROM mindsdb.models" }
     * 
     * Allowed operations: SELECT, DESCRIBE, SHOW, CREATE MODEL, CREATE DATABASE
     * Blocked operations: DROP, DELETE, TRUNCATE
     */
    @PostMapping("/query")
    public ResponseEntity<?> executeQuery(@Valid @RequestBody MindsDBQueryRequest request) {
        try {
            logger.info("Received MindsDB query request");
            Map<String, Object> result = mindsDBService.executeQuery(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid query: {}", e.getMessage());
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error executing query: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to execute query: " + e.getMessage()));
        }
    }

    /**
     * List all MindsDB models
     * GET /api/mindsdb/models
     */
    @GetMapping("/models")
    public ResponseEntity<?> listModels() {
        try {
            MindsDBQueryRequest request = new MindsDBQueryRequest("SELECT * FROM mindsdb.models");
            Map<String, Object> result = mindsDBService.executeQuery(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error listing models: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to list models: " + e.getMessage()));
        }
    }

    /**
     * List all connected databases
     * GET /api/mindsdb/databases
     */
    @GetMapping("/databases")
    public ResponseEntity<?> listDatabases() {
        try {
            MindsDBQueryRequest request = new MindsDBQueryRequest("SHOW DATABASES");
            Map<String, Object> result = mindsDBService.executeQuery(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error listing databases: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to list databases: " + e.getMessage()));
        }
    }

    /**
     * Check if a registration appears suspicious
     * POST /api/mindsdb/check-fraud
     * 
     * Request body: { "email": "test@example.com", "username": "testuser" }
     */
    @PostMapping("/check-fraud")
    public ResponseEntity<?> checkFraud(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String username = request.get("username");

            if (email == null || username == null) {
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Both email and username are required"));
            }

            boolean suspicious = mindsDBService.isSuspiciousRegistration(email, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("username", username);
            response.put("is_suspicious", suspicious);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking fraud: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to check fraud: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * GET /api/mindsdb/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            MindsDBQueryRequest request = new MindsDBQueryRequest("SELECT 1");
            mindsDBService.executeQuery(request);
            return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "mindsdb",
                "message", "MindsDB connection is working"
            ));
        } catch (Exception e) {
            logger.error("MindsDB health check failed: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "status", "unhealthy",
                    "service", "mindsdb",
                    "error", e.getMessage()
                ));
        }
    }
}
