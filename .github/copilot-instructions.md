## Workflow - Order of Execution

**REQUIRED EXECUTION ORDER FOR ALL TASKS:**

1. **Get user input** — Receive and understand the user's request
2. **Consult policy file** — Review this file (.github/copilot-instructions.md) for relevant context, commands, and conventions
3. **Prepare strategy of execution** — Plan the approach, identify files to modify, commands to run, and potential risks
4. **Stream conversation and internal commentary** — Document your analysis, proposed execution plan, and reasoning in AGENT_PROGRESS.md
5. **Begin execution or ask permission from user** — Either proceed with implementation or request approval for significant changes. **BEFORE asking permission, update AGENT_PROGRESS.md with current status of all work completed so far.**
6. **Complete task** — Execute all planned steps until the task is fully resolved
7. **Update progress file** — Stream final results, commands executed, and outcomes to AGENT_PROGRESS.md
8. **Advise user of completion of task** — Provide brief confirmation that work is complete and ready for verification

**Note:** Steps 4 and 7 are critical for maintaining conversation continuity across sessions. Always update AGENT_PROGRESS.md with your thought process, not just the final result. Step 5 requires updating progress BEFORE asking for permission.

## Quick orientation

- This is a microservices project with three main components:
  1. **Backend** (Spring Boot): Email registration service and MindsDB chatbot integration on port 8081 (internal)
  2. **Gateway** (Spring Cloud Gateway): API gateway and static file server on port 8080 (public entry point)
  3. **Web Frontend** (Angular): SPA served by gateway, proxies API calls to backend
  4. **Mobile App** (React Native): Android app communicating via gateway at 10.0.2.2:8080
- Build automation uses Gradle wrapper for Java services, npm for web frontend, Gradle for Android
- Java toolchain is Java 25 (gateway requires 25, root build.gradle can specify different version if needed)

## What the codebase does (big picture)

- **Backend Service** (`src/main/java/com/technet7/microsvc/email/`): Email registration with role-based access control, MindsDB chatbot integration, admin endpoints
- **Gateway Service** (`gateway/src/main/java/com/technet7/microsvc/gateway/`): Routes requests, serves Angular static files, handles CORS
- **Web Frontend** (`web-frontend/src/app/`): Angular SPA with login, registration, email management, chatbot, admin panel, About, Contact pages
- **Mobile App** (`droid/src/screens/`): React Native app with same feature set as web (Home, Login, Chatbot, EmailList, About, Contact)
- **Architecture**: Gateway is single public entry point (8080), all clients communicate through it, backend (8081) and MindsDB (47334) are internal only

## How to build, run, and test (exact commands)

**Backend and Gateway:**
- Build: `./gradlew clean build`
- Build specific service: `./gradlew :gateway:bootJar` or `./gradlew bootJar` (backend)
- Start services: `./scripts/start-dev.sh` (starts backend on 8081, gateway on 8080)
- Stop services: `./scripts/stop-dev.sh`
- Run tests: `./gradlew test`
- Logs: `tail -f logs/gateway.log` or `tail -f logs/backend.log`

**Web Frontend:**
- Install: `cd web-frontend && npm install`
- Dev server: `npm start` (runs on 4200, proxies /api to gateway:8080)
- Production build: `npm run build` (outputs to dist/, served by gateway)

**Mobile App:**
- Build release APK: `cd droid/android && ./gradlew assembleRelease`
- Install on emulator: `adb install android/app/build/outputs/apk/release/app-release.apk`
- View logs: `adb logcat | grep ReactNative`

**Requirements:**
- Java 25+ (gateway requires Java 25)
- Node.js 18+ and npm
- Android SDK for mobile builds

## Project-specific conventions and notes for code changes

**Backend Package Structure:**
- Package root: `com.technet7.microsvc.email`
- Controllers in `controller/` package
- Services in `service/` package
- Entities in `entity/` package
- Database configured with H2 in-memory for dev

**Gateway:**
- Package root: `com.technet7.microsvc.gateway`
- Uses Spring Cloud Gateway (WebFlux, reactive)
- Dependency: `spring-cloud-gateway-server-webflux` + `spring-boot-starter-webflux`
- CORS configured in `CorsConfig.java`
- Static content served via `StaticContentController.java`

**Web Frontend:**
- Angular 19+ with standalone components
- Routing configured in `app.routes.ts`
- API calls use relative paths (/api/*) which gateway proxies to backend
- Menu items: Home, Login, Register, Emails, Chatbot, Admin, About, Contact
- **SPA Layout Architecture**: 
  - Header (persistent): Hamburger menu, welcome message, logout button
  - Content area: Router outlet where menu components load
  - Footer (persistent): Address info, company/website links
  - Components load in content section only, header/footer remain constant

**Mobile App:**
- React Native with single-file SPA architecture (App.js)
- API base URL: `http://10.0.2.2:8080` (Android emulator host mapping)
- Screens: HomeScreen, LoginScreen, ChatbotScreen, EmailListScreen, AboutScreen, ContactScreen
- Navigation via modal hamburger menu

**Java Version Policy:**
- Gateway requires Java 25 (specified in gateway/build.gradle)
- Root build.gradle can specify different version if needed
- When upgrading Java, update both build.gradle files and rebuild with `./gradlew clean`

## Integration points and external dependencies

- **MindsDB**: Chatbot AI service on localhost:47334 (or cloud), accessed via backend `/api/mindsdb/*` endpoints
- **Database**: H2 in-memory (dev), configured in `src/main/resources/application.properties`
- **Spring Cloud**: Gateway uses Spring Cloud Gateway 2025.0.0 BOM
- **Authentication**: JWT-based (implementation in backend controllers)
- **CORS**: Configured in gateway to allow localhost:4200 (dev) and localhost:8080 (prod)

## Examples the assistant can use when suggesting edits

**Add a backend REST endpoint:**
- File: `src/main/java/com/technet7/microsvc/email/controller/ExampleController.java`
- Annotate with `@RestController` and `@RequestMapping("/api/example")`
- Use `@CrossOrigin` if needed (though gateway handles CORS)

**Add a web frontend component:**
- Create component: `cd web-frontend && ng generate component example`
- Add route in `src/app/app.routes.ts`
- Add menu item in `src/app/home/home.component.ts`

**Add a mobile screen:**
- Create file: `droid/src/screens/ExampleScreen.js`
- Import and add case in `App.js` renderPage() switch
- Add menu item in App.js Modal menu

**Add a gateway route:**
- Update `gateway/src/main/java/com/technet7/microsvc/gateway/GatewayApplication.java`
- Add RouteLocator bean with `.route()` configuration

## Safety and minimal-change policy

- Prefer small, isolated changes: add new files rather than large refactors. If modifying `build.gradle`, make minimal dependency changes and run `./gradlew build` locally.

## Conversation logging requirement

- **REQUIRED**: Maintain `AGENT_PROGRESS.md` in the project root with a complete log of all conversations, questions, decisions, commands executed, and their outcomes.
- **STREAM VERBATIM**: Use `cat >> AGENT_PROGRESS.md` to append the FULL conversation as it happens - include the complete user request verbatim, your full analysis, all reasoning steps, file paths being modified, and exact commands being executed.
- **DO NOT SUMMARIZE**: Stream the actual conversation text, not summaries. The user should be able to read AGENT_PROGRESS.md and see exactly what was said and done.
- **Update continuously**: Write to AGENT_PROGRESS.md at multiple points throughout each task:
  - After receiving user input (stream their full request)
  - After analyzing the request (stream your complete analysis and plan)
  - During execution (stream commands as you run them, errors as they occur)
  - After completion (stream final results and outcomes)
- **Format for readability**: Use markdown headers with timestamps, code blocks for commands/code, and clear sections
- **Include everything**: User questions (verbatim), agent analysis (complete), commands run (exact syntax), errors encountered (full error text), solutions applied (detailed), and current status
- This provides conversation continuity across sessions and documents the project's evolution with full context.

## Files to inspect for context

**Build Configuration:**
- `build.gradle` — root build config, Java toolchain version
- `gateway/build.gradle` — gateway dependencies (requires Java 25)
- `web-frontend/package.json` — npm dependencies and scripts
- `droid/android/build.gradle` — Android build configuration

**Backend:**
- `src/main/java/com/technet7/microsvc/email/` — main application code
- `src/main/resources/application.properties` — runtime config, database, MindsDB settings

**Gateway:**
- `gateway/src/main/java/com/technet7/microsvc/gateway/GatewayApplication.java` — routes
- `gateway/src/main/java/com/technet7/microsvc/gateway/CorsConfig.java` — CORS
- `gateway/src/main/java/com/technet7/microsvc/gateway/StaticContentController.java` — serves web app

**Web Frontend:**
- `web-frontend/src/app/app.routes.ts` — routing
- `web-frontend/src/app/home/home.component.ts` — main navigation

**Mobile:**
- `droid/App.js` — main application file, all screens and navigation
- `droid/src/screens/` — individual screen components

**Documentation:**
- `ARCHITECTURE.md` — system architecture and communication flow
- `AGENT_PROGRESS.md` — complete conversation log (REQUIRED to maintain)

If any section is unclear or you want the file to be expanded with coding style rules, testing examples, or a proposed controller/entity template, tell me which area to expand.
