# JWT Authentication Implementation Summary

## What Changed

I added JWT-based stateless authentication to back the SPA login flow:

> Note: In this workspace the gateway is the public entrypoint and listens on 127.0.0.1:8080. The backend Spring Boot service binds to 127.0.0.1:8081 by default; the gateway forwards `/api` requests to the backend. Use the gateway (port 8080) for client/API calls during development.

### Backend (Spring Boot)

1. **Dependencies** (`build.gradle`)
   - Added `io.jsonwebtoken:jjwt-api:0.11.5`, `jjwt-impl`, and `jjwt-jackson` for JWT signing and validation.

2. **Configuration** (`application.properties`)
   - `app.jwt.secret=dev-secret-change-me-at-least-32-chars-1234567890abcdef`
   - `app.jwt.expiration-ms=3600000` (1 hour)

3. **New Classes**
   - `JwtService` (`src/main/java/com/technet7/microsvc/email/security/JwtService.java`)
     - Generates JWT tokens with subject (email), username, and roles claims
     - Validates tokens and parses claims
     - Builds Spring Security `Authentication` objects from tokens
   
   - `JwtAuthenticationFilter` (`src/main/java/com/technet7/microsvc/email/security/JwtAuthenticationFilter.java`)
     - `OncePerRequestFilter` that extracts `Authorization: Bearer <token>` header
     - Sets `SecurityContext` when a valid JWT is present

4. **SecurityConfig Updates**
   - Switched to `SessionCreationPolicy.STATELESS` (no server-side sessions)
   - Registered `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
   - Added `JwtAuthenticationFilter` constructor injection

5. **EmailRegistrationController Updates**
   - On successful login:
     - Sets `SecurityContext.setAuthentication(...)` immediately (for same-request /auth/me calls)
     - Generates a JWT via `JwtService.generateToken(...)`
     - Returns `LoginResponse` with `token`, `expiresInMs`, `email`, `username`, `roles`, and `message`

6. **LoginResponse DTO**
   - Added `token` (String) and `expiresInMs` (Long) fields

### Frontend (Angular)

1. **Models** (`web-frontend/src/app/models/login.model.ts`)
   - Added optional `token?: string` and `expiresInMs?: number` to `LoginResponse`

2. **Login Component** (`web-frontend/src/app/login/login.component.ts`)
   - Stores `auth_token`, `auth_token_expires_in_ms`, and `auth_token_expires_at` in localStorage on successful login

3. **EmailRegistrationService** (`web-frontend/src/app/services/email-registration.service.ts`)
   - New method `authHeaders()` that reads `auth_token` from localStorage and adds `Authorization: Bearer <token>` header
   - All protected requests (`getAllRegisteredEmails`, `logout`, `deleteByEmail`, `getCurrentUser`) now send the token

4. **AuthService** (`web-frontend/src/app/services/auth.service.ts`)
   - Added `tokenKey` and `tokenExpiresAtKey` keys
   - `clear()` now also removes token from localStorage on logout

## How It Works

1. **Login**:
   - User submits email/password to `POST /api/emails/login`
   - Backend validates against `RegisteredEmail` table (BCrypt hash check)
   - On success, backend:
     - Sets Spring Security context for this request
     - Generates JWT with email (subject), username, roles
     - Returns JSON with `token`, `expiresInMs`, and user details
   - Frontend stores token in `localStorage.auth_token`

2. **Subsequent Requests**:
   - Angular service includes `Authorization: Bearer <token>` on all protected API calls
   - `JwtAuthenticationFilter` extracts token, validates signature, parses claims
   - Sets `SecurityContextHolder` with authenticated principal and authorities (roles prefixed with `ROLE_`)
   - Existing Spring Security `@PreAuthorize` and role checks work as before

3. **Logout**:
   - Frontend calls `POST /api/auth/logout`, clears localStorage (including token)
   - Backend can optionally track revoked tokens or rely on expiration

## Testing

### Manual Test Flow

1. **Register a user**:
   ```bash
   curl -X POST http://127.0.0.1:8080/api/emails/register \
     -H 'Content-Type: application/json' \
     -d '{"email":"test@example.com","username":"tester","password":"secret123"}'
   ```

2. **Login to get JWT**:
   ```bash
   curl -sS -X POST http://127.0.0.1:8080/api/emails/login \
     -H 'Content-Type: application/json' \
     -d '{"email":"test@example.com","password":"secret123"}' | jq
   ```
   Expected response:
   ```json
   {
     "email": "test@example.com",
     "username": "tester",
     "message": "Login successful",
     "roles": ["USER"],
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "expiresInMs": 3600000
   }
   ```

3. **Use token for protected endpoint** (example: list all emails requires USER or ADMIN role):
   ```bash
   TOKEN="<paste token from step 2>"
   curl -sS http://127.0.0.1:8080/api/emails/list \
     -H "Authorization: Bearer $TOKEN" | jq
   ```

4. **Try without token** (should return 401 or 403 depending on endpoint):
   ```bash
   curl -sS http://127.0.0.1:8080/api/emails/list | jq
   ```

### Frontend Flow

1. Start backend: `./gradlew bootRun` (ensure MySQL is running)
2. Start Angular dev server: `cd web-frontend && npm start`
3. Visit http://localhost:4200/login
4. Log in with registered email/password
5. Check browser DevTools > Application > Local Storage > `auth_token`, `auth_token_expires_at`
6. Navigate to / (main app) — protected requests should include `Authorization: Bearer ...` in Network tab

## Notes

- **Token Expiration**: Currently 1 hour (3600000 ms). Can be adjusted in `application.properties` (`app.jwt.expiration-ms`).
- **Security**: The `app.jwt.secret` is a dev placeholder. For production, use a strong secret (>= 32 bytes) and store securely (env variable, secrets manager, etc.).
- **Logout**: Stateless; the backend doesn't track issued tokens. To revoke tokens before expiration, implement a token blacklist/revocation service.
- **Refresh Tokens**: Not implemented. When token expires, user must re-login. You can add a refresh token flow if needed.
- **Role Mapping**: Backend roles are stored without `ROLE_` prefix in `RegisteredEmail.roles` but added by `JwtService` when building authorities. Frontend normalizes them to match.

## Known Issues

- **Backend Tests**: 3 tests in `RoleAssignmentServiceTest` are still failing (unrelated to JWT; pre-existing). These need separate fixes.
- **Build Status**: Backend compiles successfully; frontend builds successfully. Both are ready for runtime testing.

## Next Steps

1. **Fix failing tests** in `RoleAssignmentServiceTest` if needed.
2. **Add token expiration UI** in Angular to prompt re-login when token is about to expire.
3. **Implement refresh token** flow for better UX (optional).
4. **Production secrets**: Move `app.jwt.secret` to environment variables or a vault.
5. **Token blacklist** (optional): Track revoked tokens in Redis or DB if you need immediate logout enforcement across sessions.

## Files Modified/Created

**Backend**:
- `build.gradle` (added JWT deps)
- `src/main/resources/application.properties` (JWT config)
- `src/main/java/com/technet7/microsvc/email/security/JwtService.java` (new)
- `src/main/java/com/technet7/microsvc/email/security/JwtAuthenticationFilter.java` (new)
- `src/main/java/com/technet7/microsvc/email/config/SecurityConfig.java` (stateless + filter)
- `src/main/java/com/technet7/microsvc/email/controller/EmailRegistrationController.java` (JWT on login)
- `src/main/java/com/technet7/microsvc/email/dto/LoginResponse.java` (added token fields)

**Frontend**:
- `web-frontend/src/app/models/login.model.ts` (added token fields)
- `web-frontend/src/app/login/login.component.ts` (store token)
- `web-frontend/src/app/services/email-registration.service.ts` (send token)
- `web-frontend/src/app/services/auth.service.ts` (clear token on logout)

**Documentation**:
- This file: `JWT_AUTH_SUMMARY.md`
