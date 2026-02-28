# Build Stability Guide (Windows / PowerShell)

## Root Cause Summary
- Cache corruption symptoms (`metadata.bin` missing) were caused by interrupted or inconsistent cache state under user Gradle cache.
- Windows file locking caused transform cache deletion failures when daemon/IDE/background Java processes still held files.
- JVM mismatch existed in Android daemon settings (required 21 while machine used JDK 17), causing deterministic build failure after cache reset.

## Official Version Strategy
- Keep official compatibility per module (no forced single Gradle version across modules):
  - Android module uses its wrapper-defined Gradle version.
  - Backend module uses its wrapper-defined Gradle version.
- Keep plugin stacks unchanged unless officially required.
- Run with JDK 17 for both modules in this repository.

## Deterministic Cache Isolation Workflow
Use repository-root scripts:
- `./rebuild-android.ps1 [-Clean]`
- `./rebuild-backend.ps1 [-Clean]`

What each script does:
1. Validates `java -version` is Java 17.
2. Prints `JAVA_HOME` and current `GRADLE_USER_HOME`.
3. Stops Gradle daemons via wrapper.
4. Terminates only Gradle/cache-related `java`/`gradle` processes.
5. Sets session-scoped isolated cache home (`GRADLE_USER_HOME`) by module+wrapper version.
6. Optionally clears isolated cache when `-Clean` is provided.
7. Runs `gradlew.bat --no-daemon --refresh-dependencies clean build`.

## Sequential Build Policy
- Do not run Android and backend builds in parallel when recovering from cache or lock issues.
- Close Android Studio/IntelliJ during deep clean rebuilds.
- Execute one module rebuild at a time.

## Defender / AV Recommendation
Exclude the following from real-time scanning (if policy allows):
- `%USERPROFILE%\.gradle-*`
- `<repo>\android\.gradle`
- `<repo>\backend\.gradle`

This reduces lock contention during transform extraction and cache cleanup.

## When to Use Normal vs Isolated Rebuild
- Normal build: day-to-day development when builds are already stable.
- Isolated rebuild (`rebuild-*.ps1`):
  - `metadata.bin` missing/corrupt errors,
  - transform/workspace deletion errors,
  - after interrupted updates or daemon lock contention,
  - after JVM/toolchain consistency changes.
