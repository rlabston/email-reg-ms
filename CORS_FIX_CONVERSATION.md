# CORS and Gateway Configuration Fix - Conversation Export

**Date:** November 25, 2025  
**Branch:** feature/kotlin-2.3-upgrade  
**Issue:** Login button disabled, then 403 Forbidden error on login

---

## Problem Discovery

### Initial Issue: AI Catalog Directory Missing
- User couldn't see `ai-catalog/` directory in VS Code Explorer
- **Root Cause:** Directory was explicitly excluded in `.vscode/settings.json`
- **Fix:** Removed `"**/ai-catalog": true` from `files.exclude` setting

### Main Issue: Login Failure (403 Forbidden)

**Request Details:**
```
URL: http://135.148.149.138:8080/api/emails/login
Method: POST
Status: 403 Forbidden
Origin: http://135.148.149.138:8080
Referer: http://135.148.149.138:8080/login
```

**Initial Symptoms:**
1. Login button was disabled (fixed by page reload)
2. POST request to `/api/emails/login` returned 403 Forbidden

---

## Architecture Analysis

### Request Flow
```
Browser (135.148.149.138:8080)
    ↓ POST /api/emails/login
    ↓ Origin: http://135.148.149.138:8080
Gateway (port 8080)
    ↓ routes /api/** → backend
    ↓ strips /api prefix
Backend (port 8081 - localhost only)
    ↓ receives /emails/login
Controller endpoint
```

### Key Components

**Gateway Configuration** (`gateway/src/main/java/com/technet7/microsvc/gateway/GatewayConfig.java`):
```java
@Bean
public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("api_to_backend", r -> r.path("/api/**")
                .uri(backendBase))  // http://127.0.0.1:8081
        .build();
}
```

**Backend Security** (`src/main/java/com/technet7/microsvc/email/config/SecurityConfig.java`):
```java
.requestMatchers(HttpMethod.POST, "/emails/login").permitAll()
```

---

## Root Cause Analysis

### Why CORS Was the Issue

Even though requests go through the gateway, the browser's **Origin header is preserved** in the proxied request:

1. Browser makes request from `http://135.148.149.138:8080`
2. Gateway forwards to backend at `http://127.0.0.1:8081`
3. **Origin header still contains:** `http://135.148.149.138:8080`
4. Backend's CORS configuration only allowed `localhost:8080` and `127.0.0.1:8080`
5. Backend rejected the request with 403 Forbidden

### Original (Incorrect) CORS Configuration

**Backend CORS (too restrictive):**
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:4200",
    "http://127.0.0.1:4200",
    "http://localhost:8000",
    "http://127.0.0.1:8000",
    "http://localhost:8081",
    "http://127.0.0.1:8081",
    "http://localhost:8080",
    "http://127.0.0.1:8080"
));
```

**Gateway CORS:** Not configured (using defaults)

---

## Solution: Correct CORS Architecture

### Principle
In a gateway architecture:
- **Gateway** (public-facing) handles CORS for all browser requests
- **Backend** (internal) doesn't need CORS - only receives server-to-server requests

### Implementation

#### 1. Gateway CORS Configuration
**File:** `gateway/src/main/java/com/technet7/microsvc/gateway/CorsConfig.java` (NEW)

```java
package com.technet7.microsvc.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS configuration for the Gateway.
 * Gateway is the public-facing layer and should accept requests from browsers.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Allow all origins since gateway is public-facing
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
```

#### 2. Backend CORS Configuration
**File:** `src/main/java/com/technet7/microsvc/email/config/SecurityConfig.java` (MODIFIED)

**Changed from:**
```java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(csrf -> csrf.disable())
```

**Changed to:**
```java
http
    .cors(cors -> cors.disable())  // Disable CORS - gateway handles it
    .csrf(csrf -> csrf.disable())
```

**Removed:** The entire `corsConfigurationSource()` method and related imports

---

## Commands to Apply Fix

### Stop Current Services
```bash
pkill -f bootRun
```

### Rebuild and Restart Backend
```bash
cd /home/ubuntu/dev/mobile/spring/email-reg-ms
./gradlew clean build -x test
./gradlew bootRun &
```

### Rebuild and Restart Gateway
```bash
cd /home/ubuntu/dev/mobile/spring/email-reg-ms/gateway
../gradlew :gateway:clean :gateway:build -x test
../gradlew :gateway:bootRun &
```

### Alternative: Use Existing Scripts
```bash
cd /home/ubuntu/dev/mobile/spring/email-reg-ms
./scripts/stop-dev.sh
./scripts/start-dev.sh
```

### Check Services are Running
```bash
# Check backend (port 8081)
curl http://localhost:8081/emails/health || echo "Backend not responding"

# Check gateway (port 8080)
curl http://localhost:8080/actuator/health || echo "Gateway not responding"

# Check processes
ps aux | grep bootRun
```

### Monitor Logs
```bash
# Backend logs
tail -f /home/ubuntu/dev/mobile/spring/email-reg-ms/backend.log

# Gateway logs
tail -f /home/ubuntu/dev/mobile/spring/email-reg-ms/gateway.log

# Or combined
tail -f backend.log gateway.log
```

---

## Verification Steps

### Test Login from Browser
1. Navigate to: `http://135.148.149.138:8080/login`
2. Enter email and password
3. Click Login
4. **Expected:** Successful login (200 OK) and redirect to main page

### Test with cURL
```bash
# Test login endpoint
curl -v -X POST http://135.148.149.138:8080/api/emails/login \
  -H "Content-Type: application/json" \
  -H "Origin: http://135.148.149.138:8080" \
  -d '{"email":"test@example.com","password":"password123"}'

# Check for CORS headers in response
# Should see:
# Access-Control-Allow-Origin: http://135.148.149.138:8080
# Access-Control-Allow-Credentials: true
```

### Test CORS Preflight
```bash
# OPTIONS request (preflight)
curl -v -X OPTIONS http://135.148.149.138:8080/api/emails/login \
  -H "Origin: http://135.148.149.138:8080" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type"

# Should return 200 OK with CORS headers
```

---

## Files Changed

### Modified Files
1. `/home/ubuntu/dev/mobile/spring/email-reg-ms/src/main/java/com/technet7/microsvc/email/config/SecurityConfig.java`
   - Disabled CORS configuration
   - Removed `corsConfigurationSource()` bean
   - Removed unused imports

2. `/home/ubuntu/dev/mobile/spring/email-reg-ms/.vscode/settings.json`
   - Removed `ai-catalog` from `files.exclude`

### New Files
1. `/home/ubuntu/dev/mobile/spring/email-reg-ms/gateway/src/main/java/com/technet7/microsvc/gateway/CorsConfig.java`
   - New CORS configuration for gateway
   - Allows all origins with credentials

---

## Key Learnings

### Gateway Architecture Best Practices
1. **Gateway handles cross-cutting concerns:** CORS, rate limiting, authentication
2. **Backend services are internal:** No direct browser access, no CORS needed
3. **Origin headers are preserved:** Even when proxied through gateway

### CORS in Microservices
- **Public Gateway:** Permissive CORS (allows browser origins)
- **Internal Services:** No CORS (server-to-server only)
- **Never mix:** Don't configure CORS on both gateway and backend

### Spring Cloud Gateway vs Spring MVC
- Gateway uses **WebFlux** (reactive): `CorsWebFilter`
- Backend uses **MVC** (servlet): `CorsConfigurationSource`
- Different APIs, different filter chains

---

## Troubleshooting

### If 403 Still Occurs

**Check gateway is handling CORS:**
```bash
curl -v -X OPTIONS http://135.148.149.138:8080/api/emails/login \
  -H "Origin: http://135.148.149.138:8080" \
  -H "Access-Control-Request-Method: POST"
```

**Verify backend has CORS disabled:**
```bash
grep -A5 "cors(cors" src/main/java/com/technet7/microsvc/email/config/SecurityConfig.java
# Should show: .cors(cors -> cors.disable())
```

**Check gateway CORS filter is loaded:**
```bash
grep "CorsWebFilter" gateway.log
```

### If Login Still Fails

**Check endpoint is permitAll:**
```java
.requestMatchers(HttpMethod.POST, "/emails/login").permitAll()
```

**Verify JWT filter isn't blocking:**
- JWT filter should only parse tokens if present
- Should not block requests without tokens

**Check route configuration:**
```bash
# Verify /api/** routes to backend
curl http://135.148.149.138:8080/api/emails/health
```

---

## Additional Notes

### Frontend Configuration
The Angular frontend (`web-frontend/src/app/login/login.component.ts`) expects:
- Response with `email`, `username`, `roles`
- Optional `token` and `expiresInMs` fields
- Stores session in localStorage

### Security Considerations
- Gateway allows all origins with `allowedOriginPatterns: ["*"]`
- For production, restrict to specific domains:
  ```java
  corsConfig.setAllowedOriginPatterns(Arrays.asList(
      "https://yourdomain.com",
      "https://*.yourdomain.com"
  ));
  ```

### Environment-Specific Configuration
Consider using Spring profiles for different CORS configs:
```properties
# application-dev.properties
cors.allowed.origins=*

# application-prod.properties
cors.allowed.origins=https://yourdomain.com,https://www.yourdomain.com
```

---

## Summary

**Problem:** Login returned 403 because backend CORS didn't allow public IP origin  
**Solution:** Move CORS handling to gateway, disable CORS on backend  
**Result:** Gateway handles all browser CORS, backend receives clean requests  

**Architecture:**
```
Browser → Gateway (CORS enabled, allows all) → Backend (CORS disabled, localhost only)
```
