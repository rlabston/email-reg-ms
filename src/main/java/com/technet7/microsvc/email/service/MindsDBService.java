package com.technet7.microsvc.email.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technet7.microsvc.email.dto.MindsDBQueryRequest;

/**
 * Service for interacting with MindsDB API
 */
@Service
public class MindsDBService {

    private static final Logger logger = LoggerFactory.getLogger(MindsDBService.class);

    @Value("${mindsdb.api.url}")
    private String mindsdbUrl;

    @Value("${mindsdb.api.key:}")
    private String mindsdbApiKey; // Optional API key for MindsDB Cloud

    @Value("${mindsdb.cloud.email:}")
    private String mindsdbCloudEmail; // Optional email for basic auth

    @Value("${mindsdb.cloud.password:}")
    private String mindsdbCloudPassword; // Optional password for basic auth

    private final RestTemplate restTemplate;

    // Patterns for query validation
    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESCRIBE_PATTERN = Pattern.compile("^\\s*DESCRIBE\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern SHOW_PATTERN = Pattern.compile("^\\s*SHOW\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_MODEL_PATTERN = Pattern.compile("^\\s*CREATE\\s+MODEL\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_DATABASE_PATTERN = Pattern.compile("^\\s*CREATE\\s+DATABASE\\s+", Pattern.CASE_INSENSITIVE);

    // Dangerous patterns to block
    private static final Pattern DROP_PATTERN = Pattern.compile("\\bDROP\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DELETE_PATTERN = Pattern.compile("\\bDELETE\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRUNCATE_PATTERN = Pattern.compile("\\bTRUNCATE\\b", Pattern.CASE_INSENSITIVE);

    public MindsDBService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Execute a SQL query on MindsDB
     */
    public Map<String, Object> executeQuery(MindsDBQueryRequest request) {
        String query = request.getQuery();
        
        // Validate query
        if (!isValidQuery(query)) {
            throw new IllegalArgumentException("Query validation failed. Only SELECT, DESCRIBE, SHOW, and CREATE MODEL/DATABASE are allowed.");
        }

        logger.info("Executing MindsDB query: {}", query);

        try {
            // Use HttpURLConnection directly to avoid RestTemplate issues
            String endpoint = mindsdbUrl + "/api/sql/query";
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Create JSON request body
            String jsonBody = String.format("{\"query\":\"%s\"}", query.replace("\"", "\\\""));
            logger.info("Calling MindsDB endpoint: {} with body: {}", endpoint, jsonBody);

            // Write request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = conn.getResponseCode();
            logger.info("MindsDB response code: {}", responseCode);

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    // Parse JSON response to Map
                    ObjectMapper mapper = new ObjectMapper();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = mapper.readValue(response.toString(), Map.class);
                    logger.info("MindsDB query successful");
                    return result;
                }
            } else {
                // Read error response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    throw new RuntimeException("MindsDB API error: " + responseCode + " - " + errorResponse.toString());
                }
            }

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("Cannot connect to MindsDB at {}: {}", mindsdbUrl, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "MindsDB service is not available");
            errorResponse.put("detail", "Cannot connect to " + mindsdbUrl);
            errorResponse.put("suggestion", "1. Check if MindsDB is running, 2. Try MindsDB Cloud (cloud.mindsdb.com), or 3. Use Docker Desktop");
            throw new RuntimeException("MindsDB is not accessible. Please check connection. Details: " + e.getMessage());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("MindsDB HTTP error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("MindsDB API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error executing MindsDB query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute MindsDB query: " + e.getMessage(), e);
        }
    }

    /**
     * Validate that the query is safe to execute
     */
    private boolean isValidQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String trimmedQuery = query.trim();

        // Block dangerous operations
        if (DROP_PATTERN.matcher(trimmedQuery).find() ||
            DELETE_PATTERN.matcher(trimmedQuery).find() ||
            TRUNCATE_PATTERN.matcher(trimmedQuery).find()) {
            logger.warn("Blocked dangerous query: {}", trimmedQuery);
            return false;
        }

        // Allow only specific operations
        return SELECT_PATTERN.matcher(trimmedQuery).find() ||
               DESCRIBE_PATTERN.matcher(trimmedQuery).find() ||
               SHOW_PATTERN.matcher(trimmedQuery).find() ||
               CREATE_MODEL_PATTERN.matcher(trimmedQuery).find() ||
               CREATE_DATABASE_PATTERN.matcher(trimmedQuery).find();
    }

    /**
     * Check if a user registration is suspicious using ML model
     */
    public boolean isSuspiciousRegistration(String email, String username) {
        try {
            String query = String.format(
                "SELECT is_suspicious, is_suspicious_confidence " +
                "FROM fraud_detector " +
                "WHERE email='%s' AND username='%s'",
                sanitize(email), sanitize(username)
            );

            MindsDBQueryRequest request = new MindsDBQueryRequest(query);
            Map<String, Object> response = executeQuery(request);

            // Parse response to determine if suspicious
            // This is a simplified implementation - adjust based on actual response structure
            return parseSuspiciousFlag(response);

        } catch (Exception e) {
            logger.error("Error checking suspicious registration: {}", e.getMessage());
            // Fail open - don't block registration on ML error
            return false;
        }
    }

    /**
     * Sanitize input to prevent SQL injection
     */
    private String sanitize(String input) {
        if (input == null) {
            return "";
        }
        // Escape single quotes
        return input.replace("'", "''");
    }

    /**
     * Parse MindsDB response to extract suspicious flag
     */
    private boolean parseSuspiciousFlag(Map<String, Object> response) {
        // TODO: Implement based on actual MindsDB response structure
        // This is a placeholder implementation
        return false;
    }
}
