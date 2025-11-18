JDK setup (recommended)

This project recommends using Eclipse Temurin (Adoptium) OpenJDK binaries for local development and CI.
Temurin is a well-maintained OpenJDK distribution with broad platform coverage, timely security updates and
an easy programmatic download API (used by `scripts/setup-jdk.sh`). Temurin binaries are TCK-tested and
suitable for most open-source and enterprise projects.

Why Temurin?
- Official OpenJDK builds with frequent security updates.
- Easy to download and script via the Adoptium API.
- No Oracle commercial licensing restrictions for most use cases.
- Widely used in CI and by many OSS projects.

Using the provided helper
- To download and install Temurin JDK 21 (into `~/.jdks`) without modifying your shell:

  ./scripts/setup-jdk.sh --version 21

- To preview the chosen download URL (dry run):

  ./scripts/setup-jdk.sh --version 21 --dry-run

- To persist exports into your shell rc (e.g. `~/.bashrc`):

  ./scripts/setup-jdk.sh --version 21 --persist

Switching to another OpenJDK provider
- SDKMAN (recommended for interactive use):
  - Install SDKMAN: https://sdkman.io/install
  - Install and use a provider/version, e.g.: `sdk install java 21.0.9-tem` or `sdk use java 21.0.9-tem`

- Amazon Corretto (example):
  - Download from https://aws.amazon.com/corretto/ or use package manager on your OS.
  - Set JAVA_HOME to point to the Corretto installation directory.

- Azul Zulu / Liberica / Oracle JDK:
  - Download the vendor binary, extract, and set JAVA_HOME.
  - Make sure you understand vendor licensing if you choose Oracle for production.

CI recommendations
- Pin a JDK version in CI (e.g., Temurin 21 or 25) and install that JDK on the runner.
- Commit the Gradle wrapper (`gradle/wrapper/gradle-wrapper.properties` and jar) so CI uses the project Gradle version.
- Example GitHub Actions step using Temurin:

  - name: Set up JDK
    uses: actions/setup-java@v4
    with:
      distribution: 'temurin'
      java-version: '21'

Notes
- The project currently compiles Java code with a Gradle toolchain set to Java 17 in the root `build.gradle`.
  Changing the compilation target is a separate migration step (update toolchain settings and run full CI).
- Android modules in this repo use Java 11 compatibility. Keep Android builds on the JDK recommended by your AGP version.

Android builds and JDK 25
- For Android subprojects (e.g., `ai-catalog` and `android-client`), use JDK 21 for Gradle and plugin execution.
- As of now, building with JDK 25 can fail in Kotlin tooling (Gradle Kotlin DSL / Kotlin compiler runtime) with errors like:
  `java.lang.IllegalArgumentException: 25 at org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion.parse`.
- Recommended matrix:
  - Gradle JVM: Temurin 21 (LTS)
  - AGP: 8.13.x (aligned in this repo)
  - Kotlin: project-managed via versions catalog; upgrades should be validated on JDK 21 first.
- If you must keep a system-wide JDK 25, run Android builds with an explicit JDK 21 by setting JAVA_HOME per-invocation, e.g.:
  `JAVA_HOME=~/.jdks/jdk-21-temurin ./gradlew :app:assembleDebug` (or set `GRADLE_JAVA_HOME` in your environment).

Server (Spring Boot) builds
- The backend compiles with a Java 17 toolchain; running builds/tests on JDK 21 is supported and recommended.
- Running the server on JDK 25 may work, but it is not yet a supported baseline in this repo; prefer JDK 21 until the broader toolchain (Kotlin/AGP/Gradle/ByteBuddy) documents full JDK 25 support.

If you want, I can add alternate provider quick-links to `scripts/setup-jdk.sh` (Corretto/Azul) or add a CI workflow example to `.github/workflows`.