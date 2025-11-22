# Email Registration Microservice — Conversation Record (Nov 14, 2025)

This README captures the working session summary for the Android client, Spring Boot backend, and Angular web frontend integration work carried out during the conversation.

## Session Summary

- Primary objectives tackled:
  - Restart the Android app
  - Fix Android client failure to connect to network
  - Resolve “Failed to connect to localhost port 8080 after 0 ms”
  - Fix 400 error when client app makes request
  - Add REST endpoint to list all registered emails
  - Add Android/Angular list API calls and basic UI
  - Provide a way to view the registered email list on Android
  - Show the Android client app
  - Create a login page on the Android app that requires an email and correct password

- Environment/assumptions:
  - Linux host, bash shell
  - Android emulator uses http://10.0.2.2:8080/ to reach host services
   - The gateway is the public entrypoint and listens on port 8080; the backend Spring Boot service runs on port 8081 and is reachable via the gateway at `/api`.

## Chronological Highlights

1. App restart and environment check
   - Stopped Gradle daemons; rebuilt Android app; installed to emulator; launched app successfully.
   - Discovered correct Android package: `com.technet7.microsvc.email.client`.

2. Network connection failures
   - Confirmed emulator-to-host addressing via 10.0.2.2:8080; Android client already set correctly.
   - Backend was not running; started Spring Boot; resolved port conflict on 8080.
   - Confirmed Tomcat started with context path `/api`; verified API endpoints via curl; ensured backend stays up (nohup).

3. 400 errors on registration
   - Backend validation error seen: “Password must be at least 8 characters”.
   - Added client-side validation in Android (min length 8) and helper text in layout; rebuilt and reinstalled.

4. Add list endpoint and UIs
   - Implemented `GET /api/emails` returning a safe DTO without password.
   - Verified via curl response contains `id`, `email`, `username`, `registrationDate` only.
   - Android: added Retrofit GET, model, RecyclerView adapter and item layout; added Load button and auto-load on start; logs showed “Loaded 2 registered emails”.
   - Angular: added DTO/interface, service method, UI to load and display list in a table; auto-refresh after registration.

5. Add login feature (email + password)
   - Backend: added `LoginRequest`/`LoginResponse` DTOs, `authenticate()` (PasswordEncoder.matches), and `POST /api/emails/login` endpoint (200 on success, 401 on failure).
   - Android: created `LoginActivity` UI and logic; on success, persist session to SharedPreferences and navigate to `MainActivity`; `MainActivity` enforces session and supports Logout.

## Technologies and Decisions

- Android (Java): Retrofit 2 + Gson, OkHttp logging, RecyclerView, Material components, SharedPreferences session, minSdk 24, targetSdk 34.
-- Spring Boot 3.5.x: embedded Tomcat, JPA/Hibernate, PasswordEncoder for hashing & verifying; backend service binds to port 8081 by default and exposes APIs under context path `/api` (the gateway on 8080 forwards `/api` to this backend).
- Angular: Signals for UI state, HttpClient service to `/api` endpoints; list view added.
- Emulator networking: `http://10.0.2.2:8080/api/` used by Android client.

## Endpoints

- POST `/api/emails/register` — Validates input (password min 8).
- GET `/api/emails` and GET `/api/emails/list` — Returns safe list DTO (no password fields).
- GET `/api/emails/{email}` — Returns safe DTO for a specific email.
- POST `/api/emails/login` — Verifies email + password; returns 200 with user on success, 401 on failure.

### Home Screen Endpoints (Reactive Catalog)

- GET `/api/home/data` — Returns aggregated home screen data including welcome title/subtitle and a featured services subset.
- GET `/api/home/featured` — Returns only the featured services list.
- GET `/api/home/services` — Returns categorized services (all available service items).
- GET `/api/home/health` — Lightweight health check for home feature availability.

Notes:
- Base path `/home` is exposed through the gateway under `/api/home/*`.
- Android/Angular/Compose clients use `/api/home/data` as the primary fetch and fall back to a local static placeholder if unavailable.

## Code Artifacts Touched (high level)

Backend:
- `src/main/java/com/technet7/microsvc/email/dto/RegisteredEmailDto.java` — Safe response model.
- `src/main/java/com/technet7/microsvc/email/dto/LoginRequest.java` — Login request DTO.
- `src/main/java/com/technet7/microsvc/email/dto/LoginResponse.java` — Login response DTO.
- `src/main/java/com/technet7/microsvc/email/controller/EmailRegistrationController.java` — Endpoints: list, alias, by-email, login.
- `src/main/java/com/technet7/microsvc/email/service/EmailRegistrationService.java` — `authenticate(email, password)` using `PasswordEncoder.matches`.

Android:
- Manifest: `LoginActivity` set as launcher; `MainActivity` accessible.
- Layouts: `activity_login.xml`, `activity_main.xml` with form, RecyclerView, Load + Logout buttons, helper text for password length.
- Models/Retrofit: `EmailService.java` with register/list/login; `RegisteredEmailItem.java` and `RegisteredEmailAdapter.java`.
- Activities: `LoginActivity.java` (session persistence); `MainActivity.java` (session check, list auto-load, logout).

Angular:
- `web-frontend/src/app/models/registered-email.model.ts` — Interface.
- `web-frontend/src/app/services/email-registration.service.ts` — `getAllRegisteredEmails()`.
- `web-frontend/src/app/app.ts` and `app.html` — List UI and refresh after registration.

## Current State

- Gateway running on `:8080` and forwarding `/api` requests to the backend on `:8081`; API endpoints are reachable via the gateway (verified via curl).
- Android client baseUrl set to emulator host mapping; app installs and launches; login page is entry screen; session stored in SharedPreferences.
- List auto-load verified (“Loaded 2 registered emails”).
- Angular list UI is functional.

## Recent Android UX Enhancements

- Added “Welcome, {username}” header in `MainActivity`.
- Added pull-to-refresh via `SwipeRefreshLayout` around the RecyclerView; it invokes the existing list loader and shows a spinner.

## Optional Next Steps

- Android: Swipe-to-refresh was added; consider empty state visuals and periodic auto-refresh.
- Angular: Auto-load on startup; optional login UI if desired.
- Backend: Add rate limiting and stronger validation/error payloads as needed.

---

Generated from the active development session on Nov 14, 2025. This document summarizes the conversation-driven changes and current system behavior for quick reference.

## How-tos

### Backend (Spring Boot)

- Build

   ```bash
   ./gradlew clean build
   ```

 - Run (dev)

    ```bash
    # Start the backend service (binds to 127.0.0.1:8081 by default). Start the gateway separately to expose APIs on 127.0.0.1:8080.
    ./gradlew bootRun
    ```

- Run packaged jar

   ```bash
   ./gradlew bootJar
   java -jar build/libs/*.jar
   ```

### Running with MySQL (production / CI / managed dev)

If you want to run the service against a MySQL database (recommended for
production and realistic development), provide the datasource credentials via
environment variables and (optionally) activate the `mysql` profile. This
keeps secrets out of version control and lets you keep local defaults for
convenience.

Recommended environment variables:

- `SPRING_DATASOURCE_URL` — JDBC URL for your MySQL server (example: `jdbc:mysql://db-host:3306/email_reg_db`)
- `SPRING_DATASOURCE_USERNAME` — DB user
- `SPRING_DATASOURCE_PASSWORD` — DB password
- `SPRING_PROFILES_ACTIVE` — optionally `mysql` to load `application-mysql.properties`

Example (local/CI):

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://mysql-host:3306/email_reg_db'
export SPRING_DATASOURCE_USERNAME='my_prod_user'
export SPRING_DATASOURCE_PASSWORD='my_prod_secret'
export SPRING_PROFILES_ACTIVE='mysql'
./gradlew bootRun
```

Notes:

- `application-mysql.properties` contains MySQL-specific overrides (for
   example, the Hibernate dialect). Activating the `mysql` profile is optional
   because `application.properties` will read the JDBC URL from
   `SPRING_DATASOURCE_URL` and fall back to local defaults when the env var is
   not set.
- In CI / production, set the env vars using your CI secret store or your
   process manager (systemd, Docker, Kubernetes secrets, etc.). Do not commit
   production credentials to Git.


- Run in background and follow logs

   ```bash
   nohup ./gradlew bootRun > backend.log 2>&1 &
   tail -f backend.log
   ```

 - Free port 8080 if occupied

   Note: port 8080 is used by the gateway (the backend uses 8081 by default). To free it:

   ```bash
   lsof -ti:8080 | xargs kill -9 2>/dev/null
   ```

 - Quick health and list checks

   ```bash
   # Use the gateway (127.0.0.1:8080) as the public entrypoint. The gateway forwards to the backend on 8081.
   curl -v http://localhost:8080/api/health
   curl -v http://localhost:8080/api/emails
   ```

- Register and login (examples)

   ```bash
   # register (via gateway on 8080)
   curl -sS -X POST http://localhost:8080/api/emails/register \
      -H 'Content-Type: application/json' \
      -d '{"email":"user@example.com","username":"user","password":"password123"}'

   # login (via gateway on 8080)
   curl -sS -X POST http://localhost:8080/api/emails/login \
      -H 'Content-Type: application/json' \
      -d '{"email":"user@example.com","password":"password123"}'
   ```

Notes

- Context path is `/api`; adjust calls accordingly.
- Password must be at least 8 characters (client/server validation).

### Android client

- Build (from project root)

   ```bash
   ./gradlew :android-client:assembleDebug
   ```

- Install to emulator/device

   ```bash
   adb devices
   adb install -r android-client/build/outputs/apk/debug/android-client-debug.apk
   ```

- Launch the app

   ```bash
   adb shell monkey -p com.technet7.microsvc.email.client -c android.intent.category.LAUNCHER 1
   ```

- Verify activity launches (optional)

   ```bash
   adb logcat -d | grep -E "cmp=com.technet7.microsvc.email.client/.LoginActivity|cmp=com.technet7.microsvc.email.client/.MainActivity" | tail -10
   ```

Android networking tip

- When using the Android emulator, use `http://10.0.2.2:8080/api/` to reach the backend running on your host.

### Angular web frontend

- Start the dev server

   ```bash
   cd web-frontend
   npm ci
   npm start
   ```

-- The app serves at `http://localhost:4200/` by default and calls the API via the gateway at `http://localhost:8080/api/`.

### Gateway (local dev)

- The gateway is the intended public entrypoint for local development and listens on port 8080. It forwards `/api` requests to the backend service on port 8081.

- Build and run the gateway (from repo root):

```bash
./gradlew :gateway:bootRun
# or build and run the jar:
./gradlew :gateway:bootJar -x test
nohup java -jar gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar > gateway.log 2>&1 &
tail -f gateway.log
```

### Dev note: static fallback login (quick dev UX)

- The file `web-frontend/src/index.html` contains a small static fallback login overlay shown immediately on `/login` to avoid SSR/hydration timing issues during development. This overlay is intended as a lightweight, resilient fallback while the SPA hydrates.
- By default the static form posts to `http://127.0.0.1:8080/api/emails/login` (this matches the local Spring Boot backend used in this workspace). If you prefer to use a relative path and a dev-server proxy, change the fetch URL to `/api/emails/login` and add a proxy config to forward `/api` to `http://127.0.0.1:8080` (or update `vite`/Angular dev server settings).
- On successful login the script will set `localStorage.auth_email` (and `localStorage.auth_username` when returned by the server), display a short success toast, and then navigate to `/`. The redirect logic now attempts multiple navigation strategies (replace/href/assign) with retries to be robust when the SPA is also hydrating the page.
- If the page does not redirect for you, check the browser DevTools Console for logs beginning with `[static-login]` and the Network panel for the POST to `http://127.0.0.1:8080/api/emails/login`. That will quickly show whether the request succeeded and why the client might not have navigated.

If you'd like, I can update the dev server to proxy `/api` to the backend so the static fallback can simply POST to `/api/emails/login` (no hard-coded host/port). Tell me if you prefer the proxy change and I'll add it.

### Troubleshooting

- Backend won’t start / port in use
   - Kill whatever is on 8080: `lsof -ti:8080 | xargs kill -9 2>/dev/null` and try again.

- Android app cannot reach backend
   - Ensure backend is running and reachable via `curl http://localhost:8080/api/health` from the host.
   - Use `10.0.2.2` from the emulator, not `localhost`.

- 400 on registration
   - Ensure password length is at least 8 characters; client and server enforce this.

- Verify list endpoint
   - `curl http://localhost:8080/api/emails` should return a list with `id`, `email`, `username`, and `registrationDate`.

### Logrotate installer

A small helper installer is provided to render the logrotate template and install it system-wide.

- Template: `scripts/logrotate/email-reg-ms.conf.template` (contains an `@ROOT@` placeholder that will be replaced with the repository root at install time).
- Installer script: `scripts/install-logrotate.sh` — expands the template and writes to `/etc/logrotate.d/email-reg-ms`.

To install (requires root):

```bash
sudo bash scripts/install-logrotate.sh
```

To test the installed config (debug-only, does not rotate):

```bash
sudo logrotate --debug /etc/logrotate.d/email-reg-ms
```

To remove the installed config:

```bash
sudo rm /etc/logrotate.d/email-reg-ms
```
