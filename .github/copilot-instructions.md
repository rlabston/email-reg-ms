## Quick orientation

- This is a small Spring Boot microservice project (Java 17) with a single application entrypoint: `src/main/java/com/example/email_reg_ms/RegisterEmail.java`.
- Build and task automation uses the included Gradle wrapper. The main build file is `build.gradle` which configures Java toolchain (17) and includes `spring-boot-starter-web` and `spring-boot-starter-data-jpa`.

## What the codebase does (big picture)

- The project is a demo email-registration service scaffolded as a Spring Boot app. The `RegisterEmail` class is annotated with `@SpringBootApplication` and registers a `CommandLineRunner` that prints Spring beans at startup. There are no controllers, services, or repositories in the repository yet — expect to add them under the same base package `com.example.email_reg_ms`.
- JPA is present as a dependency but no datasource properties are provided in `src/main/resources/application.properties`. Do not assume a database is configured.

## How to build, run, and test (exact commands)

- Use the included Gradle wrapper (recommended):

  - Build: `./gradlew clean build`
  - Run app: `./gradlew bootRun` (or run the produced jar: `./gradlew bootJar && java -jar build/libs/*.jar`)
  - Run tests: `./gradlew test`

## Project-specific conventions and notes for code changes

- Package root: `com.example.email_reg_ms`. Add controllers, services and repositories under `src/main/java/com/example/email_reg_ms/` to be picked up by component scanning.
- If you add persistence models or JPA repositories, add datasource configuration to `src/main/resources/application.properties` (this file currently only contains `spring.application.name=demo`).
- Avoid changing the Java toolchain version in `build.gradle` without a good reason; the project targets Java 17.
- When adding dependencies, update `build.gradle` and keep versions consistent with Spring Boot 3.5.x dependency management.

## Integration points and external dependencies

- Spring Boot starters (web, data-jpa) are the main integration surface. There are currently no explicit external service clients, messaging, or DB configuration files; any new integrations should be explicitly added and configured.

## Examples the assistant can use when suggesting edits

- Add a simple REST controller skeleton:

  - File: `src/main/java/com/example/email_reg_ms/EmailController.java`
  - Annotate with `@RestController` and `@RequestMapping("/emails")` and add one GET/POST example.

- Add a JPA entity and repository only after adding datasource properties to `application.properties` (show the properties in suggestions).

## Safety and minimal-change policy

- Prefer small, isolated changes: add new files rather than large refactors. If modifying `build.gradle`, make minimal dependency changes and run `./gradlew build` locally.

## Files to inspect for context

- `build.gradle` — build and dependency management (Java 17, Spring Boot 3.5.x)
- `src/main/java/com/example/email_reg_ms/RegisterEmail.java` — application entrypoint and example `CommandLineRunner`
- `src/main/resources/application.properties` — runtime config (currently minimal)

If any section is unclear or you want the file to be expanded with coding style rules, testing examples, or a proposed controller/entity template, tell me which area to expand.
