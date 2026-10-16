# kotlin-logging adoption for GeneralTools 3.0.0

- **Issue:** [SpartanLaboratories/GeneralTools#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7).
- **Date of research:** 2026-10-12. Repo baseline: `master` @ `6926069`.
- **Sources fetched:** Primary sources were fetched on 2026-10-12: Maven Central metadata, the published `.module` and `.pom` files, the `oshai/kotlin-logging` source at tag `8.0.4` (shallow clone, read only, deleted afterwards), GitHub releases and issues, and two consumer libraries' build files and published metadata.

## 1. Question

Does kotlin-logging fit GeneralTools 3.0.0, and what is the idiomatic shape of the integration, including the single central failure helper?

An answer has to meet these requirements:

- The targets are `jvm`, `js` and `wasmJs`.
- Common code logs through kotlin-logging, which delegates to slf4j on the JVM so consumers' logging backends are unchanged.
- One internal helper logs each expected failure at WARN and builds its `Result.failure`; every failure site uses it.
- The current kotlin-logging release publishes `jvm`, `js` and `wasmJs`.
- MyGameTools, a JVM-only consumer on Kotlin 2.2.0 with an `api` dependency, compiles against 3.0.0 without changing its Kotlin version.
- 3.0.0 makes exactly these breaks: `Color`'s primary constructor becomes `internal`; `TwoDoubles.divideBy` returns `Result` and logs at WARN through the helper; `TwoDoubles` is no longer `sealed`; `Point` and `Dimensions` become immutable; `evaluateList` takes `(T) -> Boolean` and moves to common code; the `com.spartanlabs.logging` package is removed.
- Tests live in `com.spartanlabs.testing.<level>.<package>`; pure tests in `commonTest`, file/URL/AWT/slf4j tests in `jvmTest`.
- `read`, `asURL`, `Logger.time` and `UserActions` stay JVM-only.

## 2. Bottom line

- **The three requirements hold** (current version, `jvm`/`js`/`wasmJs` targets, MyGameTools stays on Kotlin 2.2.0).
  1. **Current version: PASS.** The latest release is **8.0.4**, published 2026-05-27 (Maven Central `<release>8.0.4</release>`, GitHub release `8.0.4` "Latest"). Nothing newer exists as of 2026-10-12, and the master HEAD commit is "bump to semver 8.0.5" of the same day.
  2. **Targets: PASS.** The 8.0.4 Gradle module metadata publishes `jvm`, `js` and `wasmJs` (plus `wasmWasi`, Android, iOS/macOS/tvOS/watchOS and Linux/mingw native). The `.jar` and both `.klib` files exist on Maven Central (HTTP 200).
  3. **MyGameTools stays on Kotlin 2.2.0: PASS, with one condition that is not kotlin-logging's.**
     - kotlin-logging 8.0.4 is compiled with Kotlin **2.1.21**, `languageVersion = apiVersion = 2.0`, JVM target 1.8. Its only Kotlin dependency is `kotlin-stdlib 2.1.21` (plus `kotlin-dom-api-compat 2.1.21` on `js`). All of that is older than 2.2.0.
     - As an `implementation` dependency of a KMP `commonMain`, it is published to the JVM variant's runtime scope only. This is the same pattern as the MCP Kotlin SDK 0.15.0 and Koog (both inspected below). MyGameTools' 2.2.0 compiler never sees kotlin-logging on its compile classpath, and a newer runtime stdlib is harmless.
     - **Condition (build pins; see [kmp-build-and-kotlin-compat.md](kmp-build-and-kotlin-compat.md)):** the stdlib on MyGameTools' *compile* classpath comes from GeneralTools' own Kotlin pin, not from kotlin-logging. The build must pin `coreLibrariesVersion`/`apiVersion` so GeneralTools' published stdlib requirement is not newer than 2.2.x. kotlin-logging neither fixes nor worsens this.
- **Use `io.github.oshai:kotlin-logging:8.0.4` as `implementation` in `commonMain`.**
  - Do not use `api`: no public GeneralTools signature needs a `KLogger`, and `api` would put kotlin-logging in every consumer's compile classpath and semver surface.
  - Keep `jvmMain { api("org.slf4j:slf4j-api:2.0.16") }`. It is needed by the public `Logger.time` extension, and kotlin-logging-jvm declares **no** slf4j dependency (`compileOnly`), so the app must supply `slf4j-api`.
- **Declare loggers with explicit names**, e.g. `private val log = KotlinLogging.logger("com.spartanlabs.geometry.Intersections")`. On the JVM this calls `LoggerFactory.getLogger(name)` unchanged, so every existing logback `<logger name="com.spartanlabs…">` rule keeps matching. Explicit names also avoid the name-resolution bugs that `KotlinLogging.logger {}` has on JS and Wasm.
- **Helper shape.** One `internal` extension in `commonMain`, `internal fun <T> KLogger.failure(message: String): Result<T>`.
  - It builds `IllegalArgumentException(message)`, calls `warn { message }`, and returns `Result.failure(exception)`.
  - Log eagerly, with no throwable and no payload. `Result<out T>` is covariant, so one helper serves every `Result<X>` return.
- **A trade-off that needs a call, not a blocker: 8.x prints a startup line.** On first use of `KotlinLogging`, every 8.x version prints `kotlin-logging: initializing... active logger factory: Slf4jLoggerFactory` to stdout, on every platform, by default. A consumer such as MyGameTools would see a new stdout line. It is suppressible by the consumer (`-Dkotlin-logging.logStartupMessage=false` or env `KOTLIN_LOGGING_STARTUP_MESSAGE=false` on the JVM). Version 7.0.14 (2026-01-01) has the same stdlib, language level and targets and does not print it, but 7.x is the previous major.
- **Maturity.** The README still says "An experimental multiplatform support is available", but that sentence is stale. The project publishes 20+ targets, `js`/`wasm` use a built-in "direct" logger, and JetBrains' Koog and the official MCP Kotlin SDK use it in production KMP builds.
  - The real maturity risks are a one-maintainer project and no per-logger levels on JS/Wasm.

## 3. Findings

Tiers: **consensus** = standard/spec or multiple independent sources. **practice** = what mature libraries visibly do. **single source** = one party's statement.

### F1. Current release: 8.0.4 (2026-05-27); no newer. Tier: consensus (registry plus project page).
- `maven-metadata.xml` for `io.github.oshai:kotlin-logging`: `<latest>8.0.4</latest> <release>8.0.4</release>`, `<lastUpdated>20260527064716</lastUpdated>`.
- GitHub releases: 8.0.4 (2026-05-27, "Align to semver."), 8.0.03 (2026-05-19), 8.0.02 (2026-05-03), 8.0.01 (2026-02-16), 8.0.00-beta-01..04 (2026-01-05..09), 7.0.14 (2026-01-01).
- The 8.0.4 GitHub tag's `build.gradle.kts` says `version = "8.0.5"`. The maintainer explained it as an artefact of bumping before writing release notes ([issue #604](https://github.com/oshai/kotlin-logging/issues/604)). The published artefacts are 8.0.4.
- No release in the 4.5 months since. Not a red flag for a small facade, but it is the cadence to expect.

### F2. 8.x is a recent major rewrite. Tier: single source (release notes and PRs).
- 8.0.00-beta-01 is "Refactor: Unified Logger Provider Architecture" ([PR #584](https://github.com/oshai/kotlin-logging/pull/584)). 8.0.01 also adds a JPMS `module-info`, removes all deprecated `KLogger` methods ([PR #586](https://github.com/oshai/kotlin-logging/pull/586)), adds an event timestamp, and adds the `logStartupMessage` switch ([PR #589](https://github.com/oshai/kotlin-logging/pull/589)).
- v8 release notes were requested and not written ([issue #599](https://github.com/oshai/kotlin-logging/issues/599)). The API used here (`KotlinLogging.logger(name)`, `warn { }`, `KLogger`) is the stable 5.x–7.x surface.

### F3. "Experimental multiplatform": a stale README sentence, not a maturity statement. Tier: single source plus code evidence.
- README at 8.0.4 and on master: "An experimental multiplatform support is available." The same README's Download section still shows version `7.0.3`, so the text is evidently not maintained. The wiki's Multiplatform page does not use the word.
- Evidence of actual maturity:
  - 3,114 stars; created 2016-07; 30 contributors; 11 open issues; roughly one release a month in 2026.
  - Users: JetBrains Koog `agents-core` (8.0.01), the official MCP Kotlin SDK 0.15.0 (8.0.4), YouTrack, Kord, OPENRNDR, Misk, and others per the README.
  - Practically a one-maintainer project (`oshai`).
- What "experimental" means in practice is the **non-JVM backend**: a "direct" logger with a single global level and a console appender (F7). It works and is simple, but it is not a full logging system.

### F4. Targets in 8.0.4. Tier: consensus (published Gradle module metadata).
- Root `kotlin-logging-8.0.4.module` variants include `jsApiElements-published` (platform `js`), `wasmJsApiElements-published` (platform `wasm`, wasm target `js`), `jvmApiElements-published`, `wasmWasi…`, `release/debug` Android, and the native targets.
- The per-target modules redirect via `available-at` to `kotlin-logging-js`, `kotlin-logging-wasm-js` and `kotlin-logging-jvm`. The files `kotlin-logging-js-8.0.4.klib`, `kotlin-logging-wasm-js-8.0.4.klib` and `kotlin-logging-jvm-8.0.4.jar` return HTTP 200.
- 7.0.14 and 7.0.13 also publish `js`, `jvm`, `wasmJs` and `wasmWasi`.
- This confirms SpartanGraphics' web-target research (`web-target-kotlin-js-vs-wasm.md` F4, in the SpartanLaboratories/SpartanGraphics working tree) directly.

### F5. Compiler and stdlib compatibility (the critical question). Tier: consensus (build files and published metadata, plus the Kotlin API guidelines).
- At tag 8.0.4:
  - `gradle/libs.versions.toml`: `kotlin = "2.1.21"`, with the comment "kotlin version > 2.1.x do not support kotlin 2.0 competability".
  - `build.gradle.kts`: `apiVersion.set(KotlinVersion.KOTLIN_2_0)`, `languageVersion.set(KotlinVersion.KOTLIN_2_0)`, `jvmTarget = JvmTarget.JVM_1_8`.
  - Published `kotlin-logging-jvm-8.0.4.module`: `kotlin-stdlib` `requires 2.1.21` in both `jvmApiElements` and `jvmRuntimeElements`.
  - `js` additionally requires `kotlin-dom-api-compat 2.1.21`; `wasmJs` requires only `kotlin-stdlib 2.1.21`.
- The jar's classes are Java 8 bytecode (class major 52), and it also carries a `module-info.class` (major 61, Java 17).
- Kotlin API guidelines: the library's *language version* decides which compilers can compile against it, and its *API version* sets the minimum runtime stdlib. "On the JVM, consumers can use any compiler version from the previous language version onwards … On other platforms, consumers can use a compiler version the same as the library's configured language version or any later version." This is the compatibility rule quoted in full in claim 5 of section 7.
  - Language version 2.0 is below 2.2.0 on every platform, so this is satisfied for MyGameTools, for GeneralTools' own compiler on `js`/`wasmJs` (any ≥ 2.0), and for future KMP consumers.
- **History to learn from:** [issue #546](https://github.com/oshai/kotlin-logging/issues/546). A kotlin-logging release compiled with Kotlin 2.2 broke Kotlin 2.0 consumers ("Class 'kotlin.Unit' was compiled with an incompatible version of Kotlin. The actual metadata version is 2.2.0, but the compiler version 2.0.0 can read versions up to 2.1.0"). The maintainer reverted in 7.0.12 and pinned 2.1.21 since. The project actively holds its compile level low, which suits this repo. A future 8.x/9.x could raise it; pin the exact version and review the `.module` on every bump.

### F6. Dependency scope: `implementation` in `commonMain` gives runtime-only on the JVM variant. Tier: practice (two independent published artefacts) plus the Gradle definition.
- Gradle `java-library` docs: `implementation` dependencies "will, on the other hand, not be exposed to consumers, and therefore not leak into the consumers' compile classpath".
- Observed in the published MCP Kotlin SDK 0.15.0 (`commonMain { implementation(libs.kotlin.logging) }`):
  - `kotlin-sdk-core-jvm-0.15.0.module`: `jvmApiElements-published` has no kotlin-logging dependency, and `jvmRuntimeElements-published` requires `kotlin-logging 8.0.4`.
  - The `-jvm` POM lists `kotlin-logging-jvm` with `<scope>runtime</scope>`, and the root POM lists `kotlin-logging` at runtime scope.
  - The `js` and `wasm-js` modules: `…ApiElements` has none; `…RuntimeElements` requires `kotlin-logging 8.0.4`.
- **KMP quirk:** the *root* module's `metadataApiElements` (the common-metadata variant, used only by KMP consumers compiling `commonMain`) also lists `kotlin-logging`. Gradle/JVM-only consumers such as MyGameTools resolve the `jvm` variant and are unaffected. KMP consumers (SpartanGraphics) will see kotlin-logging's 2.1-level metadata on their metadata compile, which any compiler ≥ 2.0 reads.
- Koog uses the same `implementation(libs.oshai.kotlin.logging)` in `commonMain` of `agents-core` and many other modules.

### F7. JVM backend. Tier: consensus (source at 8.0.4).
- The default JVM factory is slf4j: `KotlinLoggingConfiguration.detectLogger()` returns `Slf4jLoggerFactory` unless a system property selects JUL, Logback-direct or Direct.
- `Slf4jLoggerFactory.logger(name)` is `wrapJLogger(LoggerFactory.getLogger(name))`. The logger name is passed through **verbatim**, so `KotlinLogging.logger("com.spartanlabs.geometry.Intersections")` is the same slf4j logger as today. Location-aware loggers (logback) get correct caller data (`LocationAwareKLogger`).
- slf4j-api is **`compileOnly`** in kotlin-logging (built against 2.0.17); the jvm module declares no slf4j dependency.
  - README: "Slf4j dependency is not provided anymore (users have to provide it). It means that >= 5.x can work with both slf4j 1 or 2."
  - GeneralTools' `api(slf4j-api:2.0.16)` stays and satisfies this.
  - Without it, the first `KotlinLogging.logger` call on the JVM would fail with `NoClassDefFoundError` (my inference from `Slf4jLoggerFactory` referencing `org.slf4j.LoggerFactory` unconditionally; not run).
- Compatibility with 2.0.16: the plain log path uses `LocationAwareLogger.log(...)`. The slf4j 2-only fluent path (`atLevel`, `addKeyValue`, `CallerBoundaryAware`) runs only when a `payload` is set. `CallerBoundaryAware` and `LoggingEventBuilder` are present in `slf4j-api-2.0.16.jar` (checked with `unzip -l`).
- JPMS caveat: the shipped `module-info.class` says `requires kotlinx.coroutines.core` (non-static) although the Gradle metadata does not declare coroutines. Only module-path users would be affected; not relevant to classpath consumers.

### F8. JS and Wasm backend. Tier: consensus (source at 8.0.4).
- Default factory `DirectLoggerFactory` (`KLoggerDirect`).
  - Default level: `Level.INFO`, one **global** level (`KotlinLoggingConfiguration.direct.logLevel`), with no per-logger levels ([issue #582](https://github.com/oshai/kotlin-logging/issues/582), "Per-object logging level", is open).
  - Default formatter: `DefaultMessageFormatter(includePrefix = true)` renders `WARN: [<logger name>] <message>` plus `, Caused by: '<msg>'` for causes. It does **not** render `payload`.
  - Default appender: `ConsoleOutputAppender`, `WARN → console.warn`, `ERROR → console.error`, `INFO → console.info`, `DEBUG/TRACE → console.log`. The Wasm version calls the same functions through `js("console.warn(message)")`.
- A consumer configures it with `KotlinLoggingConfiguration.direct.logLevel`, `.formatter` and `.appender`, where `Appender` and `Formatter` are public interfaces in `commonMain`. To mute only GeneralTools on JS/Wasm, a consumer wraps the appender and drops events whose `loggerName` starts with `com.spartanlabs`.
- The wiki's Multiplatform page is outdated (it documents `KotlinLoggingConfiguration.logLevel`); trust the source.
- Logger-name resolution for `KotlinLogging.logger {}` on JS/Wasm relies on parsing stack traces ([#315](https://github.com/oshai/kotlin-logging/issues/315), [#441](https://github.com/oshai/kotlin-logging/issues/441), [#580](https://github.com/oshai/kotlin-logging/issues/580)). Explicit string names avoid it.

### F9. Startup message. Tier: consensus (source and issues).
- `KotlinLogging` has `init { … if (KotlinLoggingConfiguration.logStartupMessage) println("kotlin-logging: initializing... active logger factory: ${factory::class.simpleName}") }`.
- Default on the JVM: `System.getProperty("kotlin-logging.logStartupMessage")?.toBoolean() ?: System.getenv("KOTLIN_LOGGING_STARTUP_MESSAGE")?.toBoolean() ?: true`. JS checks `window.KOTLIN_LOGGING_STARTUP_MESSAGE` or `process.env.KOTLIN_LOGGING_STARTUP_MESSAGE`.
- The line was introduced in 8.0.00 ([issue #587](https://github.com/oshai/kotlin-logging/issues/587), "especially useful for CLI tools where clean output is important"). The system-property/env switch came in 8.0.03 ([issue #598](https://github.com/oshai/kotlin-logging/issues/598)). 7.0.14's `KotlinLogging.kt` has no such `println`.
- Neither Koog nor the MCP SDK disables it (grep found no `logStartupMessage`).
- A library should not set this global switch on a consumer's behalf. Setting `KotlinLoggingConfiguration.logStartupMessage = false` from library code is global mutation of a shared singleton and would also have to run before `KotlinLogging` initialises.

### F10. "Powerful" features: which are worth using here. Tier: practice, from the API at 8.0.4 and how Koog/MCP use it (plain `logger.x { }`; neither wraps the event builder).
- **Lazy lambdas** (`warn { "…" }`): the idiom. A `KLogger` call checks `isLoggingEnabledFor` before invoking the lambda. Use it. The exception's message must exist anyway (see F11), so laziness here saves only the lambda body.
- **Event builder** (`atWarn { message = …; cause = …; payload = … }`): works, but allocates a `KLoggingEventBuilder` per event. Its main extra, `payload`, is invisible on JS/Wasm (the default formatter drops it), and on the JVM is visible only with a key-value-aware logback encoder.
  - Cargo cult for a failure helper whose whole payload is a message string.
  - Reserve it for cases with a real `cause` and structured data.
- **Markers:** pass-through to slf4j markers (`warn(marker, msg)`), and the name is prefixed on JS. No need for them here.
- **MDC:** `KotlinLoggingMDC` and `KotlinLoggingAsyncMDC` live in `jvmMain` only (JVM-only, not common code). Irrelevant to a stateless geometry library.
- **`entry/exit/throwing/catching`:** trace helpers; not needed.

### F11. Exception type and eager versus lazy message. Tier: first-principles from this repo, not a cited standard.
- All 12 sites use `IllegalArgumentException` (KDoc says `Result.failure(IllegalArgumentException)` in six places), and `TwoDoubles.divideBy` already throws `IllegalArgumentException("Cannot divide by zero")`. `kotlin.IllegalArgumentException(message: String?)` is available in common code on all targets. Keep it: the type is documented API.
- The failure message must be built eagerly because `Result.failure` needs the exception. So the helper takes a `String` and logs `warn { message }` (a captured string, no re-formatting).
- Today the log line and the exception message differ: the log carries the function name and the offending values (`"segmentIntersectsSegment: NaN coordinate in {} or {}"`), while the exception carries a short stable message. A single-message helper loses that detail unless the call site puts it in the message. See the recommendation for the optional lazy `detail`.
- Passing the exception to the logger (`warn(exception) { … }`) would add a stack trace to every expected failure, pointing into the helper. Do not.

## 4. How comparable projects do it

- **modelcontextprotocol/kotlin-sdk** (tag/branch `main` @ `9f4cca6`, 2026-10-09; Kotlin 2.4.20; kotlin-logging 8.0.4).
  - `kotlin-sdk-core/build.gradle.kts`: `commonMain { dependencies { … implementation(libs.kotlin.logging) } }`, with targets `jvm` (via plugin), `js`, `wasmJs` and Apple targets.
  - Logger declaration: `private val logger = KotlinLogging.logger { }` at file level (e.g. `shared/Protocol.kt`).
  - Published 0.15.0 artefacts show kotlin-logging in runtime scope only (F6).
  - It declares no slf4j dependency in the core. Tests use `runtimeOnly(libs.slf4j.simple)`.
  - It has no log-and-fail helper.
- **JetBrains/koog** (`main` @ `0740554`, 2026-10-08; Kotlin 2.3.10; kotlin-logging 8.0.01).
  - `implementation(libs.oshai.kotlin.logging)` in the `commonMain` dependencies of `agents-core`, `agents-mcp`, `a2a-*` and others. Same plain `logger.x { }` usage, and no log-and-fail helper.
  - One example module implements its own `KLogger` against the direct logger's config for JS.
- **No comparable log-then-`Result.failure` helper turned up.** I searched the two clones above, a few GitHub code queries (rate-limited partway through), and two web searches. The pattern here is first-principles design, not copied convention. The closest analogue is the stdlib's own `Result.failure<T>(exception)`, and the local precedent is the existing `fun org.slf4j.Logger.time(…)` extension in `Misc.kt`.
- **kotlin-logging's own approach to the helper question:** `KLogger` is an interface with only three abstract members (`name`, `isLoggingEnabledFor(level, marker)`, `at(level, marker, block)`); every `warn`/`info`/… overload is a default method delegating to `at`. A recording fake `KLogger` is therefore about ten lines, which makes an extension helper testable in `commonTest` without any backend.

## 5. Pitfalls and regrets

1. **Startup stdout line (F9).** New in 8.x, on by default, on every platform. It lands in every consumer that first touches GeneralTools' geometry or `Misc`. The consumer opt-out exists but is per-process configuration.
2. **`slf4j-api` is not transitively provided (F7).** Dropping `api(slf4j-api)` from `jvmMain` would break the JVM at the first logger. Keep it (it is needed for `Logger.time` anyway).
3. **Do not expose kotlin-logging in public signatures or public `inline` functions.** That would put it on consumers' compile classpath and into the semver surface (a public inline body is inlined into consumer bytecode and references `KLogger`).
4. **`KotlinLogging.logger {}` is unreliable on JS/Wasm (F8).** Empty or truncated names. Always pass the name string.
5. **Central helper changes log call-site information.** Logback patterns that print `%C/%M/%L` would show the helper, not the failing function. The repo's pattern (`%logger{36}`) shows the logger name, which the per-file logger preserves. If the function name matters, put it in the message (existing messages already start with `"segmentIntersectsSegment: …"`).
6. **Every failure is now a log line.** Hot loops that probe degenerate input will produce a WARN per call. On the JVM, consumers can silence `com.spartanlabs.geometry` via logback. On JS/Wasm there is only one global level, so the only selective mute is a wrapping `Appender` (F8). Document this in the migration notes.
7. **Default-level surprise on JS/Wasm.** Default INFO means `debug`/`trace` calls (Misc, UserActions) are silent and WARN prints. A JS test that wants TRACE must set `KotlinLoggingConfiguration.direct.logLevel = Level.TRACE` (global, and on the JVM it prints a "this config might be ignored" WARN via `internalCheckFactory`, so keep it out of `commonTest`).
8. **`{}` in messages.** slf4j-style `{}` placeholders disappear when converting to Kotlin string templates (good), but a message that contains a literal `{}` can misbehave with some JVM backends (log4j JSON layout, [issue #401](https://github.com/oshai/kotlin-logging/issues/401)). Not relevant to logback and unlikely for geometry values.
9. **Single maintainer, young major.** v8 is five months old. Pin the exact version, no ranges, and read the `.module` and the diff on every bump (F5, #546).
10. **Kotlin stdlib alignment is not kotlin-logging's problem but is the real compile-time risk (see [kmp-build-and-kotlin-compat.md](kmp-build-and-kotlin-compat.md)).** If GeneralTools 3.0.0 is built with a much newer Kotlin plugin and publishes `kotlin-stdlib` at that version on its API variant, MyGameTools' 2.2.0 compiler would read it from the compile classpath and fail with an incompatible-metadata error. That happens independent of kotlin-logging; the build's pins decide it.

## 6. Recommendation for this repo

**Adopt kotlin-logging 8.0.4, shaped as follows.**

1. **Dependency.** `commonMain { implementation("io.github.oshai:kotlin-logging:8.0.4") }` (exact version, no range). `jvmMain { api("org.slf4j:slf4j-api:2.0.16") }` stays, since the public JVM-only `Logger.time` uses it and the app must supply slf4j. Logback stays a **test** dependency of `jvmTest` only. Do not use `api` for kotlin-logging, because nothing in the public surface needs it, and `api` would put it on MyGameTools' compile classpath and in the semver contract.
2. **Logger declaration.** Keep the repo's file-level `private val log` convention but with explicit names: `private val log = KotlinLogging.logger("com.spartanlabs.geometry.Intersections")`, one per file, names unchanged from today. That keeps consumers' logback config matching and works the same on JS/Wasm.
3. **The helper.** A single `internal` file in `commonMain` (e.g. in package `generaltools`) holds `internal fun <T> KLogger.failure(message: String): Result<T>`.
   - Behaviour: create `IllegalArgumentException(message)`, call `warn { message }` (no throwable, no payload, no markers), return `Result.failure(exception)`.
   - Why a `KLogger` extension rather than the alternatives:
     - A top-level function taking the logger as a parameter is the same thing with worse call syntax.
     - A single shared logger would change every logger name, so existing `com.spartanlabs.geometry.*` logback rules would stop matching and there would be no per-file origin. Reject it.
     - The extension keeps each file's logger name, mirrors the existing `Logger.time` extension, and can be unit-tested in `commonTest` with a three-member recording `KLogger` fake.
   - Return type: `Result<T>` generic, mirroring `Result.failure<T>`. `Result<out T>` is covariant, so `Result<Nothing>` would also work; generic `T` keeps the signature identical to the stdlib's and forces a type at the call site.
   - Exception type: keep `IllegalArgumentException`, the documented contract, with no exception parameter until a site needs a different type. `TwoDoubles.divideBy`, which returns a `Result` in 3.0.0, uses the same helper at WARN.
   - Optional: add a second parameter `detail: (() -> Any?)? = null` appended to the *log line only* (`warn { if (detail == null) message else "$message: ${detail()}" }`). It preserves today's value-rich diagnostics (the offending segment, ray or vector) without changing the exception message tests assert, and it is the one place laziness pays off (the value's `toString` runs only when WARN is enabled). Without it, the helper still meets its contract, but logs lose the values.
4. **Every one of the 12 sites uses the helper.** `return log.failure("…")` replaces both `log.warn(…)` and `Result.failure(…)`. After conversion a grep for `Result.failure(IllegalArgumentException` outside the helper should find nothing (a cheap guard against a site forgetting to log).
5. **Tests.**
   - **Logback stays the JVM test backend:** `jvmTest { implementation("ch.qos.logback:logback-classic:1.5.13") }` plus `src/jvmTest/resources/logback-test.xml` (moved from `src/test/resources`; KMP source-set resources live in the directory named after the source set). Logger names are unchanged, so the file works as is.
   - `commonTest`: assert the helper with a recording fake `KLogger`: one WARN event, the same message, the failure exception is `IllegalArgumentException` with that message.
   - `js`/`wasmJs` test output: failures print through `console.warn` as `WARN: [com.spartanlabs.geometry.Intersections] <message>` at the default level; `debug`/`trace` are silent unless a JS-only test sets `KotlinLoggingConfiguration.direct.logLevel`. Each test run also prints the startup line unless `KOTLIN_LOGGING_STARTUP_MESSAGE=false` is set in the Node environment.
6. **Version choice that needs a call (not a failed requirement).** 8.0.4 is the current, widely used (Koog, MCP SDK) release and the right default, but it is the first major line to print a startup line. If silent consumers matter more than currency, 7.0.14 is the same on stdlib/language level/targets/API for this use. The README, migration notes and any consumer docs should state the opt-out (`-Dkotlin-logging.logStartupMessage=false`).
7. **Verification that cannot be done from here (do before release).** After the build is converted, run `publishToMavenLocal` and read the generated `GeneralTools-jvm-3.0.0.module`: confirm `jvmApiElements-published` has no kotlin-logging, `jvmRuntimeElements-published` requires it, and the stdlib requirement is ≤ 2.2.x.

## 7. Load-bearing claims

1. **Current release is 8.0.4 (2026-05-27).** Maven Central metadata: `<release>8.0.4</release>` and `<lastUpdated>20260527064716</lastUpdated>`. <https://repo1.maven.org/maven2/io/github/oshai/kotlin-logging/maven-metadata.xml> (fetched 2026-10-12). GitHub release 8.0.4 published 2026-05-27T06:26:38Z ("Align to semver."): <https://github.com/oshai/kotlin-logging/releases/tag/8.0.4>.
2. **8.0.4 publishes js and wasmJs (and jvm).** Gradle module file variants named `jsApiElements-published` and `wasmJsApiElements-published` with `available-at` urls `../../kotlin-logging-js/8.0.4/kotlin-logging-js-8.0.4.module` and `../../kotlin-logging-wasm-js/8.0.4/kotlin-logging-wasm-js-8.0.4.module`; files `kotlin-logging-js-8.0.4.klib` and `kotlin-logging-wasm-js-8.0.4.klib`. <https://repo1.maven.org/maven2/io/github/oshai/kotlin-logging/8.0.4/kotlin-logging-8.0.4.module> (8.0.4, fetched 2026-10-12).
3. **Compiled with Kotlin 2.1.21 at language/api 2.0.** `libs.versions.toml`: `kotlin = "2.1.21"` with comment "kotlin version > 2.1.x do not support kotlin 2.0 competability". `build.gradle.kts`: `apiVersion.set(KotlinVersion.KOTLIN_2_0)` and `languageVersion.set(KotlinVersion.KOTLIN_2_0)` and `jvmTarget = JvmTarget.JVM_1_8`. <https://github.com/oshai/kotlin-logging/blob/8.0.4/gradle/libs.versions.toml>, <https://github.com/oshai/kotlin-logging/blob/8.0.4/build.gradle.kts> (tag 8.0.4). Caveat: the tag's `build.gradle.kts` says `version = "8.0.5"` ([issue #604](https://github.com/oshai/kotlin-logging/issues/604)); the published module confirms stdlib 2.1.21 (claim 4).
4. **Published stdlib dependency is 2.1.21.** `kotlin-logging-jvm-8.0.4.module`: `"module": "kotlin-stdlib" … "requires": "2.1.21"` for `jvmApiElements-published` and `jvmRuntimeElements-published`; the `-js` module also requires `kotlin-dom-api-compat` `2.1.21`. <https://repo1.maven.org/maven2/io/github/oshai/kotlin-logging-jvm/8.0.4/kotlin-logging-jvm-8.0.4.module>, <https://repo1.maven.org/maven2/io/github/oshai/kotlin-logging-js/8.0.4/kotlin-logging-js-8.0.4.module> (8.0.4).
5. **Kotlin compiler/stdlib compatibility rule.** "On the JVM, consumers can use any compiler version from the previous language version onwards." and "On other platforms, consumers can use a compiler version the same as the library's configured language version or any later version." and "Your consumers also need to provide a Kotlin standard library version that is at least as new as your library's configured API version." <https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html>, "Choose compatible language and API versions" (fetched 2026-10-12).
6. **`implementation` stays off consumers' compile classpath.** "Dependencies found in the `implementation` configuration will, on the other hand, not be exposed to consumers, and therefore not leak into the consumers' compile classpath." <https://docs.gradle.org/current/userguide/java_library_plugin.html> (Gradle current docs, fetched 2026-10-12).
7. **KMP `commonMain` `implementation(kotlin-logging)` publishes runtime-only on the JVM variant.** `kotlin-sdk-core-jvm-0.15.0.pom`: `<groupId>io.github.oshai</groupId> <artifactId>kotlin-logging-jvm</artifactId> <version>8.0.4</version> <scope>runtime</scope>`; the module's `jvmApiElements-published` lists no kotlin-logging dependency, and `jvmRuntimeElements-published` requires `kotlin-logging` `8.0.4`. Source declaration: `implementation(libs.kotlin.logging)` in `kotlin-sdk-core/build.gradle.kts` `commonMain`. <https://repo1.maven.org/maven2/io/modelcontextprotocol/kotlin-sdk-core-jvm/0.15.0/kotlin-sdk-core-jvm-0.15.0.module> and `.pom` (0.15.0); <https://github.com/modelcontextprotocol/kotlin-sdk/blob/9f4cca65db7a8cc145d92c0d9666e568bca66430/kotlin-sdk-core/build.gradle.kts>.
8. **Koog uses the same scope.** `implementation(libs.oshai.kotlin.logging)` in `agents/agents-core/build.gradle.kts`; `oshai-logging = "8.0.01"` and `kotlin = "2.3.10"` in `gradle/libs.versions.toml`. <https://github.com/JetBrains/koog/blob/07405545c860ce76d6328c6cb5288a4d013b263c/agents/agents-core/build.gradle.kts> (commit `0740554`, 2026-10-08).
9. **README calls multiplatform support experimental.** "An experimental multiplatform support is available." <https://github.com/oshai/kotlin-logging/blob/8.0.4/README.md>, "Multiplatform" (8.0.4; unchanged on master 2026-10-12).
10. **slf4j is user-supplied and names pass through verbatim.** README: "Slf4j dependency is not provided anymore (users have to provide it). It means that >= 5.x can work with both slf4j 1 or 2." Source: `override fun logger(name: String): KLogger = wrapJLogger(jLogger(name))` with `internal fun jLogger(name: String): Logger = LoggerFactory.getLogger(name)` in `Slf4jLoggerFactory`; build: `compileOnly(libs.slf4j.api)` in `jvmMain`. <https://github.com/oshai/kotlin-logging/blob/8.0.4/README.md>; <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/javaMain/kotlin/io/github/oshai/kotlinlogging/slf4j/internal/Slf4jLoggerFactory.kt>; `build.gradle.kts` at 8.0.4.
11. **JVM defaults to slf4j.** `detectLogger()` ends `// default to SLF4J  return Slf4jLoggerFactory`. <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/jvmMain/kotlin/io/github/oshai/kotlinlogging/KotlinLoggingConfiguration.kt> (8.0.4).
12. **JS/Wasm default to direct logging at INFO to the console.** `public actual var loggerFactory: KLoggerFactory = DirectLoggerFactory`, `private var _logLevel: Level = Level.INFO`, `private var _appender: Appender = ConsoleOutputAppender()`; `ConsoleOutputAppender`: `Level.WARN -> console.warn(formattedMessage)`. <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/jsMain/kotlin/io/github/oshai/kotlinlogging/KotlinLoggingConfiguration.kt>, <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/jsMain/kotlin/io/github/oshai/kotlinlogging/ConsoleOutputAppender.kt> (8.0.4); Wasm equivalent in `wasmJsMain`.
13. **Configuration surface for non-JVM.** "On JVM/Android/Darwin, this ONLY applies if [loggerFactory] is set to use [DirectLoggerFactory]. On Native/JS/Wasm, this is the default mechanism." and `public var logLevel: Level; public var formatter: Formatter; public var appender: Appender`. <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/commonMain/kotlin/io/github/oshai/kotlinlogging/KotlinLoggingConfiguration.kt> (8.0.4).
14. **The default formatter ignores payload.** `DefaultMessageFormatter.formatMessage` appends only `prefix(level, loggerName)`, the marker name, `message` and the cause chain. <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/commonMain/kotlin/io/github/oshai/kotlinlogging/Formatter.kt> (8.0.4).
15. **Startup message default and opt-out.** `println("kotlin-logging: initializing... active logger factory: ${factory::class.simpleName}")` in `KotlinLogging` `init`; JVM default `System.getProperty("kotlin-logging.logStartupMessage")?.toBoolean() ?: System.getenv("KOTLIN_LOGGING_STARTUP_MESSAGE")?.toBoolean() ?: true`. <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/commonMain/kotlin/io/github/oshai/kotlinlogging/KotlinLogging.kt>, `…/jvmMain/…/KotlinLoggingConfiguration.kt` (8.0.4). Issue: "Add configuration to suppress `kotlin-logging: initializing...` startup message … (`8.0.00-beta-01`)" <https://github.com/oshai/kotlin-logging/issues/587>. The 7.0.14 `KotlinLogging.kt` has no `println`/`init` (checked 2026-10-12).
16. **`KLogger` has three abstract members.** `public val name: String`, `public fun isLoggingEnabledFor(level: Level, marker: Marker? = null): Boolean`, `public fun at(level: Level, marker: Marker? = null, block: KLoggingEventBuilder.() -> Unit)`; all `warn(…)` overloads are defaults such as `public fun warn(message: () -> Any?): Unit = warn(null, null, message)`. <https://github.com/oshai/kotlin-logging/blob/8.0.4/src/commonMain/kotlin/io/github/oshai/kotlinlogging/KLogger.kt> (8.0.4).
17. **`Result` is covariant, with a generic `failure`.** `public value class Result<out T> @PublishedApi internal constructor(` and `public inline fun <T> failure(exception: Throwable): Result<T> =`. <https://github.com/JetBrains/kotlin/blob/v2.2.0/libraries/stdlib/src/kotlin/util/Result.kt> (Kotlin 2.2.0).
18. **MDC is JVM-only.** `KotlinLoggingMDC.kt` and `coroutines/KotlinLoggingAsyncMDC.kt` exist only under `src/jvmMain`. <https://github.com/oshai/kotlin-logging/tree/8.0.4/src/jvmMain/kotlin/io/github/oshai/kotlinlogging> (8.0.4).
19. **Past Kotlin-2.2-compiled release broke older consumers; fixed by reverting.** Issue #546 quotes "The actual metadata version is 2.2.0, but the compiler version 2.0.0 can read versions up to 2.1.0", and the maintainer: "reverting in 7.0.12". <https://github.com/oshai/kotlin-logging/issues/546> (2025).
20. **7.0.14 also publishes the three targets with the same stdlib.** `kotlin-logging-7.0.14.module` variants `jsApiElements-published`, `jvmApiElements-published`, `wasmJsApiElements-published`; `kotlin-stdlib` `requires` `2.1.21`. <https://repo1.maven.org/maven2/io/github/oshai/kotlin-logging/7.0.14/kotlin-logging-7.0.14.module> (7.0.14, 2026-01-01).

## 8. Gaps

- **Not built or run.** Everything about GeneralTools' own published metadata, and what MyGameTools' Gradle resolves against it, is inferred from two published KMP libraries (MCP SDK 0.15.0, Koog) that use the identical `commonMain implementation` pattern. The `publishToMavenLocal` check in §6 item 7 is still required.
- **The stdlib seen on MyGameTools' compile classpath depends on the build's pins (see [kmp-build-and-kotlin-compat.md](kmp-build-and-kotlin-compat.md)).** I did not determine GeneralTools' Kotlin version. I only state that kotlin-logging adds nothing newer than 2.1.21 and contributes no compile-time constraint.
- **Runtime-harmlessness of a newer stdlib** (kotlin-logging compiled at 2.1.21 running on MyGameTools' 2.2.x stdlib) rests on the Kotlin guideline that API version only sets a *minimum* runtime stdlib. I did not find a sentence saying "newer is always fine" and did not run it. It is the normal Kotlin backward-compatibility expectation (low risk).
- **Klib ABI compatibility** of a 2.1.21-built `js`/`wasmJs` klib consumed by newer compilers: not verified from a primary source here (SpartanGraphics' web-target research, `web-target-kotlin-js-vs-wasm.md` in the SpartanLaboratories/SpartanGraphics working tree, says JetBrains has not finalised klib binary compatibility guarantees). The risk is limited to future KMP consumers that link against kotlin-logging's klib via GeneralTools.
- **No pattern found for a "log and return `Result.failure`" helper** in comparable libraries. The helper design (§6) is first-principles. I did not search for a published argument on whether to log at failure creation versus at the handling boundary; this repository logs at failure creation.
- **kotlin-logging 8.0.4 vs 7.0.14 on the JVM:** I did not diff 7.0.14 and 8.0.4 behaviour beyond the startup message, the removed deprecated methods and the JPMS descriptor. Whether 7.x still gets fixes is unknown.
- **JPMS:** the shipped `module-info.class` requires `kotlinx.coroutines.core` (non-static) while Gradle metadata does not declare coroutines. Only matters if a consumer runs on the module path. Not investigated further. It could be reported upstream as a kotlin-logging issue if it ever matters.
- **`src/jvmTest/resources` convention:** the Kotlin docs only say a source set's files are "usually stored in the directory with the name of the source set". The exact resources path in the Kotlin Gradle plugin for the `jvm` test compilation is for the build and test-layout research to confirm ([kmp-build-and-kotlin-compat.md](kmp-build-and-kotlin-compat.md), [kmp-test-levels-and-names.md](kmp-test-levels-and-names.md)).
- **Behaviour on `wasmJs` in a real browser/Karma run** (console output capture) was not observed; the claims come from source only.
- **Other gaps:** none blocking. MyGameTools' own source does not use kotlin-logging (grepped `oshai`/`kotlinlogging`: no matches), so no consumer-side version conflict exists today.
