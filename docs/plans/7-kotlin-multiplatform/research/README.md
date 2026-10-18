# Research: GeneralTools 3.0.0 on Kotlin Multiplatform

- **Issue:** [SpartanLaboratories/GeneralTools#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7),
  *"Make GeneralTools Kotlin Multiplatform (Color and geometry usable from common code)"*. The
  same change resolves
  [SpartanLaboratories/GeneralTools#8](https://github.com/SpartanLaboratories/GeneralTools/issues/8),
  *"Read-only views for Point and Square so libraries can expose geometry without copying"*.
- **Release:** `3.0.0`, a major version, published to Maven Central by hand.
- **Baseline:** `master` @ `17d30bc`; the source is unchanged since `6926069`. `./gradlew build`
  passes with 185 tests, no failures and none skipped.
- **Citations:** documents by section, code by symbol; outside repositories at a pinned tag or
  commit.

This file holds the research questions behind the 3.0.0 design, then the verification of the
outside-world claims their answers rest on, then a topic-by-topic summary. Each question's
findings live in the file it names.

---

## What 3.0.0 is

- **Targets.** `jvm` (bytecode 17), `js` and `wasmJs`, built from one Kotlin Multiplatform
  module. There are no Android or native targets.
- **Placement.** `Color`, the whole `com.spartanlabs.geometry` package and the platform-neutral
  `Misc` helpers live in common code. `read`, `asURL`, the slf4j `Logger.time` extension and
  `UserActions` stay JVM-only.
- **Logging.** Common code logs through kotlin-logging, which delegates to slf4j on the JVM, so a
  consumer's logging backend keeps working unchanged. One internal helper logs each expected
  failure at WARN and builds its `Result.failure`; every failure site goes through it.
- **Breaking changes, and only these.**
  - `Color`'s primary constructor becomes `internal`.
  - `TwoDoubles` is no longer `sealed`.
  - `Point` and `Dimensions` become immutable.
  - `TwoDoubles.divideBy` returns a `Result` instead of throwing, and logs at WARN instead of
    ERROR.
  - `evaluateList` takes a `(T) -> Boolean` and moves to common code.
  - The `com.spartanlabs.logging` package is removed.

  Each break is clean, with no deprecation cycle. The public API dump changes by exactly this set.
- **Also in the change.** `capitalizeEveryWord` stops dropping commas that are part of its input,
  with a test for that case. `src/test/resources/keys.txt`, which no test reads, is deleted.
- **Coordinates.** `io.github.spartanlaboratories:GeneralTools` and every package name stay.
  Gradle consumers resolve the right variant automatically; Maven-only consumers switch to
  `GeneralTools-jvm`.
- **Release notes.** Migration notes covering every break go in the README and the GitHub
  release. The repository has no CHANGELOG.md.
- **Release mechanics.** A maintainer publishes by hand; there is no CI publish. The release is
  tagged with a bare `3.0.0` and gets a GitHub release.
- **Downstream.** MyGameTools (SpartanLabsGaming/MyGameTools) consumes GeneralTools `2.2.0` as an
  `api` dependency on `master` (its last published release, `gametools` 5.0.0, carries `2.0.1`) and
  builds with Kotlin 2.2.0. It stays on Kotlin 2.2.0 when it adopts 3.0.0. It will not compile
  unchanged against 3.0.0: it mutates `Point` and `Dimensions` in place (`setTo`,
  `dimensions.width = ...`) at 40 sites in 17 files.

---

## Sources reused

- **Earlier plans in this repository** carry their research inline. It is reused as context,
  not repeated:
  - `docs/plans/3-geometry-primitives/plan.md` §2: the slab method and its NaN handling. Its §7
    explains why `Segment`, `Ray` and `CenteredBox` document "treat as immutable" instead of
    copying their mutable `Point`s, and anticipates immutable `Point`/`Dimensions` in a future
    major.
  - `docs/plans/1-color-methods/plan.md` §2: `Color`'s rounding and hex formats.
- **SpartanGraphics research**, in the working tree of
  [SpartanLaboratories/SpartanGraphics](https://github.com/SpartanLaboratories/SpartanGraphics)
  at `docs/roadmap/research/` (dated 2026-10-11, not committed there):
  - `web-target-kotlin-js-vs-wasm.md` already answers several web-target points: Kotlin/JS is
    Stable and Kotlin/Wasm is Beta; the shared `webMain` source set exists since Kotlin 2.2.20;
    kotlin-logging 8.0.4 publishes `js` and `wasmJs` variants; non-JVM consumers need a compiler
    at least as new as a library's language version; klib binary-compatibility guarantees are not
    finalized; partial linkage is always on from Kotlin 2.4.0. Because the file is uncommitted and
    lives in another repository, the claims used here are re-verified below.
  - `idiomatic-kotlin.md`: value types and hot-path maths. Context for the geometry-model and
    API-mechanics questions.
  - `consumer-needs-and-dependencies.md`: whether SpartanGraphics will depend on GeneralTools is
    still open there. Context for the downstream question.
  - `android-build-and-platform-floors.md`: context only.

---

## Questions

### 1. How should the build pin Kotlin versions so a Kotlin 2.2.0 consumer can still compile against it, and how are targets, source sets, JVM 17, the JS/Wasm test runtimes, Gradle and CI set up?

- **Options.** Stay on Kotlin Gradle plugin 2.2.0; or move to the current 2.4.x with
  `languageVersion`, `apiVersion` and the published `kotlin-stdlib` pinned. For JVM 17:
  `jvmToolchain(17)`, or `jvmTarget` plus `-Xjdk-release`.
- **Requirements.** Targets are `jvm` (bytecode 17), `js` and `wasmJs`. At least one non-JVM
  target exists, so common code cannot quietly use JVM APIs. MyGameTools, a JVM-only consumer on
  Kotlin 2.2.0 with an `api` dependency, compiles against 3.0.0 without changing its Kotlin
  version. The setup is long-lived and cheap to maintain. The Gradle wrapper is 9.0.0 today.
- **Evidence that settles it.** The Kotlin compatibility guide and API guidelines; Kotlin Gradle
  plugin docs and source; the Gradle compatibility matrix; what comparable published libraries
  declare.
- **Findings:** [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md).

### 2. How is the multiplatform 3.0.0 published to Maven Central with the vanniktech plugin, and how are the real artifacts checked before the irreversible release?

- **Options.** vanniktech `0.36.0` (in use today) or newer; the javadoc-jar mode; automatic
  release, or a staged deployment tested before release; local smoke tests through
  `publishToMavenLocal` or a file repository.
- **Requirements.** Coordinates `io.github.spartanlaboratories:GeneralTools` stay; Maven-only
  consumers move to `GeneralTools-jvm`. A maintainer publishes by hand from a machine that holds
  the signing key and the Central credentials; there is no CI publish. A Central release cannot
  be undone. The release gets a bare `3.0.0` tag and a GitHub release.
- **Evidence that settles it.** vanniktech's docs, source and changelog; the Sonatype Central
  Portal docs; Kotlin Gradle plugin publishing docs; comparable libraries' published artifacts.
- **Findings:** [`kmp-maven-central-publishing.md`](kmp-maven-central-publishing.md).

### 3. Does kotlin-logging serve `jvm`, `js` and `wasmJs` without raising consumers' Kotlin floor, and what shape should its integration and the central failure helper take?

- **Options.** Which kotlin-logging release; `implementation` or `api` scope; the helper as a
  `KLogger` extension or a top-level function; kotlin-logging's event builder or plain messages.
- **Requirements.** Common code logs through kotlin-logging, which delegates to slf4j on the JVM
  so a consumer's backend keeps working unchanged. The current kotlin-logging release publishes
  `jvm`, `js` and `wasmJs` and does not stop a Kotlin 2.2.0 JVM consumer from compiling. One
  internal helper logs each expected failure at WARN and builds its `Result.failure`; every
  failure site uses it.
- **Evidence that settles it.** kotlin-logging's releases, Gradle module metadata and POMs; its
  source and README; slf4j compatibility; published libraries that use the same dependency shape.
- **Findings:** [`kotlin-logging-adoption.md`](kotlin-logging-adoption.md).

### 4. Does every existing pure test hold on `js` and `wasmJs`, and which common-code replacements serve the JVM-only calls?

- **Options.** For each test class: it holds unchanged on all three targets; it needs a
  platform-specific expectation; or it can pass only if behaviour changes on some target.
- **Requirements.** `Color` and the geometry types behave the same on `jvm`, `js` and `wasmJs`.
  The existing test expectations hold unchanged, except that `capitalizeEveryWord` stops dropping
  commas that are part of its input. JVM-only calls in code that moves to common are replaced:
  `String.format` in `Color.toHex`, `javaClass` in `TwoDoubles.equals`/`hashCode`, the
  `java.lang.Math.pow` import in `Point.kt`, `java.util.function.Predicate` in `evaluateList`,
  the deprecated `String.capitalize` in `capitalizeEveryWord`, and `Thread.sleep` in tests. Code
  that leans on JVM numeric behaviour is checked target by target: `minOf`/`maxOf` propagating
  NaN in the slab tests, `Float` arithmetic in `Color`, and `Double.toString` in
  `TwoDoubles.toString`.
- **Evidence that settles it.** The Kotlin standard library's JS and Wasm sources at pinned tags;
  official docs; the repository's own tests, read assertion by assertion.
- **Findings:** [`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md).

### 5. How are tests laid out and selected by level across `jvm`, `js` and `wasmJs`, and do the existing test names work on every target?

- **Options.** Separate per-level test tasks for each target, or one level property applied to
  every target's test task; keep the 182 backtick-quoted test names, or rename them.
- **Requirements.** Tests follow the 5-level hierarchy, each level in its own
  `com.spartanlabs.testing.<level>.<package>` package. Pure tests go to `commonTest`; file, URL,
  AWT and slf4j tests go to `jvmTest`. The stray test class `Point` in
  `src/test/kotlin/geometry/Point.kt` folds into `PointTest`, and the empty `MiscKtTest` is
  deleted. Every target's test task can select levels by package filter. `logback-test.xml` keeps
  serving the JVM tests.
- **Evidence that settles it.** Kotlin Gradle plugin and Gradle docs and source (`KotlinJsTest`,
  test filtering); Kotlin docs on identifiers per target; MyGameTools' and SpartanGraphics'
  level-selection setups.
- **Findings:** [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md).

### 6. What shape should the immutable geometry model take, and what is left of read-only views once `Point` and `Dimensions` are immutable?

- **Options.** Live mutable state through a `var` that holds an immutable value, or through a
  read-only/mutable pair of types; `TwoDoubles` as an abstract base class, an interface, or no
  shared base; `Square` kept mutable with a read-only view, or made immutable.
- **Requirements.**
  - `Point` and `Dimensions` become immutable. `TwoDoubles` is no longer `sealed`, so consumers
    can define their own two-double types. `divideBy` returns a `Result` and logs through the
    failure helper instead of throwing.
  - A library can hand out geometry that its callers cannot mutate, without copying it. Issue #8
    names the cost today: MyGameTools' `ZoneGrid` and `UnzonedPointException` copy `Point`s, and
    `Zone.bounds` exposes a shared `Square` guarded only by a prose "do not mutate".
  - The concrete types keep their `equals`/`hashCode` semantics.
  - Domain types are open to subclassing; members that maintain a class's invariants stay
    `final` or `internal`. Every new public seam sits in one stability tier: Stable Core,
    Supported Extension (the same semver promise, marked `@SupportedExtension`), or Experimental
    (behind `@RequiresOptIn`).
  - The 3.0.0 breaks are the listed set; everything else stays source-compatible.
  - Geometry sits on game hot paths (raycasts, per-frame movement), so allocation cost on the JVM
    and on JS matters.
- **Evidence that settles it.** Comparable libraries' code (OPENRNDR, Kool, KorGE/korma, Compose
  `ui-geometry`, JavaFX, libGDX, kotlin-math); JVM and JS allocation and escape-analysis
  evidence; how MyGameTools uses the types.
- **Findings:** [`immutable-geometry-model.md`](immutable-geometry-model.md).

### 7. Which Kotlin language mechanics best express the other 3.0.0 breaks, and how is it proven that the public API changes by exactly the intended set?

- **Options.** `Color` as a data class with an `internal` constructor (and what that does to
  `copy`), a plain final class, or a value class; naming for functions that return `Result`; the
  `evaluateList` signature; JVM facade names for the `Misc` functions split between common and
  JVM code (`@JvmName`, `@JvmMultifileClass`); the Kotlin Gradle plugin's `abiValidation()` or
  the binary-compatibility-validator plugin; `explicitApi()`.
- **Requirements.** `Color`'s primary constructor becomes `internal`, leaving the factories as
  the public way to build a `Color`. `evaluateList` takes `(T) -> Boolean` and lives in common
  code. The `com.spartanlabs.logging` package is removed. `read`, `asURL`, `Logger.time` and
  `UserActions` stay JVM-only. Breaks are clean, with no deprecation cycle. The public API dump
  changes by exactly the intended set of breaks, and migration notes cover every one.
- **Evidence that settles it.** Kotlin docs, KEEPs and release notes; the ABI-validation docs of
  the Kotlin Gradle plugin and of the binary-compatibility-validator; comparable libraries'
  setups.
- **Findings:** [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md).

### 8. What does 3.0.0 break downstream, which existing code should move onto the new geometry model, and which checks before publishing prove the release?

- **Options.** A consumer smoke test only; a throwaway MyGameTools migration, never committed,
  built against the new GeneralTools through a composite build; testing the staged Central
  deployment before release; a pre-release version.
- **Requirements.** MyGameTools stays on Kotlin 2.2.0 and does not compile unchanged against
  3.0.0; the size of its migration is part of the answer. The downstream repositories
  (MyGameTools, MyGameClient, MyGameServer, WebTools, SpartanGraphics) are read, never changed,
  by this work. Inside this repository, every class built on a mechanism that 3.0.0 replaces
  (mutable `Point`/`Dimensions`, the throwing `divideBy`, the slf4j logger fields,
  `com.spartanlabs.logging`) gets a verdict: move onto the new model, or not. A Central release
  cannot be undone.
- **Evidence that settles it.** This repository's code and history; the downstream repositories'
  code, read-only; `git log` and `git blame` on the areas touched.
- **Findings:** [`downstream-blast-radius.md`](downstream-blast-radius.md).

---

## Verification results

Every outside-world claim a recommendation rests on was re-opened at its source on 2026-10-18:
the numbered load-bearing claims of the eight findings files, and nine claims reused from
SpartanGraphics' `web-target-kotlin-js-vs-wasm.md` (its claims 1, 2, 5 and 12 to 17). Quotes that
the findings mark as fetcher-extracted turned out verbatim, apart from typography.

| Findings file | Claims checked | Hold | Do not hold |
|---|---|---|---|
| [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md) | 33 | 33 | 0 |
| [`kmp-maven-central-publishing.md`](kmp-maven-central-publishing.md) | 23 | 23 | 0 |
| [`kotlin-logging-adoption.md`](kotlin-logging-adoption.md) | 20 | 20 | 0 |
| [`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md) | 28 | 27 | 1 |
| [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md) | 20 | 20 | 0 |
| [`immutable-geometry-model.md`](immutable-geometry-model.md) | 20 | 20 | 0 |
| [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) | 26 | 25 | 1 |
| [`downstream-blast-radius.md`](downstream-blast-radius.md) | 9, plus the published artifacts it decodes | all | 0 |
| SpartanGraphics `web-target-kotlin-js-vs-wasm.md` (reused) | 9 | 9 | 0 |

No claim is unclear. The pinned versions are current: Kotlin 2.4.21 is the latest stable release
(2.5.0-Beta1 is the only newer tag), and vanniktech `0.37.0`, kotlin-logging `8.0.4`, the
binary-compatibility-validator `0.18.2`, `actions/setup-java` v6.1.0, `gradle/actions` v6.4.0 and
kotlinx.coroutines 1.11.0 are each the latest release.

**The two claims that do not hold, and what holds instead.**

- **Data-class `copy` visibility** ([`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md)
  claim 1). The warning on a data class with a non-public constructor was scheduled to become an
  error at language version 2.5. Kotlin 2.5.0-Beta1 postponed that with no new version
  ([KT-89123](https://youtrack.jetbrains.com/issue/KT-89123), "Keep the visibility inconsistency
  about data class copy and primary constructor as a warning"). It stays a warning outside
  progressive mode, both on the declaration and at consumers' `copy` calls. The choice of annotation
  for `Color` therefore rests on validation and on the set of breaks alone, not on a future build
  failure.
- **`KClass.qualifiedName` on Wasm** ([`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md)
  claim 19). It is enabled by default on Kotlin/Wasm since Kotlin 2.3.0; the stdlib KDoc that says
  otherwise predates the change. It is still always `null` on JS, so common code still avoids it.

**Claims about this repository and its consumers** were checked against the code directly:

- the baseline build: 185 tests, none failing or skipped;
- the 182 backtick test names, which use only letters, digits, spaces, hyphens (34 names) and
  apostrophes (5 names);
- 7 slf4j logger fields, and 12 failure sites: 11 `Result.failure` calls plus the throwing
  `divideBy`;
- 183 `log.` calls in the test tree, 173 of them in tests that move to `commonTest`;
- the inputs of the grazing-ray test;
- in today's jar, member functions that return `Result` have mangled JVM names
  (`distanceFrom-IoAF18A`, `asRay-d1pmJ48`) and top-level ones do not;
- MyGameTools' 40 in-place mutation sites in 17 files (6 in main sources), `VisibleObject` handing
  `area.location` to `GameObject`, and `Actor.isAtDestination` comparing with `==`.

---

## Summary

Each topic gives what was found, the recommended approach with the evidence behind it, where
sources disagree and which view holds, and the options that do not fit. A trade-off that needs a
call is stated with its options and a recommendation.

### Build and Kotlin compatibility

- **Recommended.** `kotlin("multiplatform") 2.4.21` replaces `kotlin("jvm")` and `java-library`
  (a Kotlin Multiplatform project with `java-library` is an error on Gradle 8.7 and later). The
  Gradle wrapper stays at 9.0.0, inside the documented range of the Kotlin Gradle plugin 2.4.21.
  - `languageVersion` and `apiVersion` are pinned to 2.2 on the `kotlin {}` extension.
  - `commonMain` declares `api(kotlin-stdlib:2.2.0)`. The web source sets keep the stdlib that
    matches the plugin, because the JS and Wasm compilers reject an older stdlib klib, so
    `coreLibrariesVersion` is not lowered.
  - `jvmToolchain(17)` with `jvmTarget = 17`. The JVM variant declares `org.gradle.jvm.version = 17`
    explicitly, since a multiplatform publication declares none.
  - Targets: `jvm()`, `js { nodejs() }` and `wasmJs { nodejs() }`. There is no `webMain` unless a
    shared JS/Wasm `actual` is needed, and no `browser()`.
  - `kotlin-js-store/yarn.lock` is committed. There is no version catalog.
  - CI moves to `actions/setup-java@v6` with Temurin 17 and current checkout and Gradle actions. It
    needs no Node step, because the Kotlin Gradle plugin downloads Node itself.

  Evidence: [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md).
- **Why exactly 2.2.** A Kotlin compiler reads class metadata up to one minor ahead of itself, and
  the metadata a build writes follows its `languageVersion`. MyGameTools builds with Kotlin 2.2.0,
  and MyGameClient, which reaches GeneralTools through `gametools`, builds with Kotlin 2.1.20. 2.2
  is therefore the highest value both can read ([`downstream-blast-radius.md`](downstream-blast-radius.md)
  §3.2), and raising it breaks Kotlin 2.1 consumers. Kotlin 2.5 deprecates 2.2 as a language
  version, so the pin moves together with the consumers' own Kotlin upgrades. This tightens the
  build findings' "2.2, or 2.3 at most", which considered only the Kotlin 2.2.0 consumer.
- **Consequences.**
  - JS and Wasm consumers need Kotlin 2.4 or newer. A klib carries the ABI version of the compiler
    that built it, and `languageVersion` does not lower it. No current consumer uses the web
    variants.
  - Only a Kotlin 2.2.0 consumer build proves the JVM floor; `jvmTest` cannot.
- **Disagreement.** One widely read argument prefers `-Xjdk-release` to toolchains. The Kotlin
  Gradle plugin's own documentation answers its premise for Kotlin. The toolchain holds here
  because it also runs the JVM tests on JDK 17, the floor itself.
- **Rejected.**
  - Staying on the Kotlin Gradle plugin 2.2.0: Kotlin/Wasm is pre-Beta there, there is no `webMain`,
    and Gradle 9.0.0 is outside its documented range.
  - A global `coreLibrariesVersion` pin: it breaks the web compilations.
  - Kotlin Gradle plugin 2.3.21 with no stdlib pin: it works only while the build sits exactly one
    minor above its oldest consumer.

### Publishing

- **Recommended.** vanniktech `0.37.0`, the first release documented as tested against the Kotlin
  Gradle plugin 2.4.
  - The existing `mavenPublishing {}` block stays. `group` and `version` are set on the project and
    the coordinates are derived from them.
  - An explicit
    `configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty(), sourcesJar = SourcesJar.Sources()))`
    keeps the empty javadoc jar a deliberate policy.
  - The artifacts are `GeneralTools` (root metadata, no classes), `GeneralTools-jvm`,
    `GeneralTools-js` and `GeneralTools-wasm-js`.

  Evidence: [`kmp-maven-central-publishing.md`](kmp-maven-central-publishing.md).
- **Release flow.**
  1. Upload without releasing: `publishToMavenCentral`, never the one-shot
     `publishAndReleaseToMavenCentral`.
  2. Test the validated deployment through the Central Portal's download endpoint.
  3. Press Publish.
  4. Tag `3.0.0` and create the GitHub release.

  The README's "Releasing" section moves to this flow and to the keyring-file signing properties.
- **Plain Maven.** For a Maven build the root artifact holds no classes, so Maven consumers depend on
  `GeneralTools-jvm`. Okio keeps its old artifact working for Maven with a root-POM workaround;
  that workaround is not adopted here.

### Logging

- **Recommended.** kotlin-logging `8.0.4` as an `implementation` dependency of `commonMain`. On the
  JVM variant it is runtime-only, so it stays off consumers' compile classpaths.
  - `jvmMain` keeps `api(slf4j-api)`: `Logger.time` needs it, and kotlin-logging does not supply it.
  - Loggers keep their explicit names
    (`KotlinLogging.logger("com.spartanlabs.geometry.Intersections")`), so existing logback rules
    still match and JS and Wasm get real names.
  - One `internal fun <T> KLogger.failure(message: String): Result<T>` builds the
    `IllegalArgumentException`, logs the message at WARN and returns the failure. All 12 failure
    sites use it.
  - Logback stays the JVM test backend.

  Evidence: [`kotlin-logging-adoption.md`](kotlin-logging-adoption.md).
- **Consumer-visible effect.** kotlin-logging 8 prints one startup line to stdout on first use, in
  every consumer, unless the consumer sets `-Dkotlin-logging.logStartupMessage=false`. The migration
  notes name the switch. Version 7.0.14 prints nothing, but it is the previous major.
- **JS and Wasm.** Output goes to the console through kotlin-logging's direct logger, which has one
  global level (INFO by default).

### Cross-platform behaviour

- **Found.** Of the 176 tests that move to `commonTest`, 163 hold unchanged on `jvm`, `js` and
  `wasmJs`, and 12 change because `Point` and `Dimensions` lose their in-place mutators. One cannot
  hold unchanged: `Point(1.0, 2.0).toString()` is `"1.0, 2.0"` on the JVM and Wasm but `"1, 2"` on
  JS, which prints a `Double` with JavaScript's number formatting. Evidence:
  [`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md).
- **Trade-off that needs a call: `toString` on JS.**
  - (A) A per-target expectation in the test.
  - (B) A five-line internal normaliser in common code that appends `.0` to integral output. Every
    target then prints `"1.0, 2.0"`, and JVM output cannot change.
  - (C) A formatter that reproduces the JVM layout for every magnitude.
  - (D) A format-agnostic assertion.

  Recommendation: B.
- **Differences no test covers.**
  - On JS a `Float` is computed in double precision. `Color.lerp` with a non-dyadic fraction can
    therefore land one channel unit away from the JVM (about 0.01% of probed cases), and
    `luminance()` differs in its last digits. The KDoc states it; float32 emulation on JS is not
    added.
  - `trimIfLongerThan` with a negative length returns `""` on JS and throws elsewhere. An explicit
    check makes every target throw `IndexOutOfBoundsException`, the supertype of what the JVM throws
    today.
- **Replacements for JVM-only calls.**
  - `Color.toHex` uses `Int.toString(16).padStart(2, '0').uppercase()`. It reproduces `%02X` for
    every non-negative channel, whereas `HexFormat` on bytes would wrap a channel above 255. This is
    preferred over the parity findings' first choice of `HexFormat` for that reason.
  - `equals` and `hashCode` use `this::class`, after an identity check.
  - The stray `java.lang.Math.pow` import goes.
  - `evaluateList` takes `(T) -> Boolean`.
  - `capitalizeEveryWord` becomes
    `split(' ').joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }`.
    It keeps commas and no longer depends on the JVM's default locale.
  - Tests drop `Thread.sleep` and the JVM-only `assert`.
- **Pre-existing defect.** `TwoDoubles.equals` uses IEEE `==` while `hashCode` hashes the bits.
  `Point(0.0, 0.0)` therefore equals `Point(-0.0, 0.0)` but hashes differently on the JVM and Wasm,
  and a point with a NaN coordinate is not equal to itself. The fix is under "Geometry model".

### Test layout and level selection

- **Recommended.**
  - Sources live in `src/{commonMain,commonTest,jvmMain,jvmTest}/kotlin`, and `logback-test.xml`
    lives in `src/jvmTest/resources`.
  - Every test sits in `com.spartanlabs.testing.<level>.<package>`:
    - `PointTest`, `SquareTest`, `TwoDoublesTest` and the pure `MiscTest` tests are level 2 in
      `commonTest`.
    - The `asURL` and `Logger.time` tests are level 2 in `jvmTest`.
    - The `read` test and `UserActionsTest` are level 3 in `jvmTest`.
    - The stray `Point` test class folds into `PointTest`.
    - `MiscKtTest` and `keys.txt` are deleted.
  - A `-PtestLevel=<level>[,<level>]` property adds
    `includeTestsMatching("com.spartanlabs.testing.<level>.*")` to every test task and sets
    `failOnNoMatchingTests = false`. Left unset, `check` runs every level on every target.

  Evidence: [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md).
- **Names.** All 182 backtick test names stay. They contain only letters, digits, spaces, hyphens
  and apostrophes, which JS and Wasm accept from language version 2.1.
- **Rejected.**
  - Per-level test tasks per target: the plugin adds every test run to `check`, so each test would
    run once per level, and web test tasks cannot be registered by hand.
  - JUnit tags: common code cannot use them.

### Geometry model

- **Recommended.**
  - Live state is a `var` holding an immutable value. There is no mutable twin type.
  - `TwoDoubles` becomes a non-generic `abstract class`. It has `val first` and `val second`,
    `containsNaN`, a `protected abstract fun create(first, second)` factory, `final`
    `equals`/`hashCode`, and `open infix fun divideBy(magnitude: Double): Result<TwoDoubles>`.
  - `Point` and `Dimensions` narrow `divideBy` to `Result<Point>` and `Result<Dimensions>`. It keeps
    today's reciprocal arithmetic.
  - `Point` and `Dimensions` stay final and keep their copy constructors. Both gain `plus`, `minus`,
    `times` and `copy`, and `Point` also gains `unaryMinus`. `+=` on a `var` compiles through
    `plus`.
  - There is no `div` operator. `p /= k` stops compiling rather than silently changing from "fails
    on zero" to "returns Infinity".
  - `Square` stays a mutable holder of immutable parts, and `AxisAlignedBox` is its read-only view.
    No `ReadOnlyPoint` or `ReadOnlySquare` type is needed: immutability and the existing interface
    resolve issue #8.

  Evidence: [`immutable-geometry-model.md`](immutable-geometry-model.md).
- **Rejected.**
  - A recursive generic `TwoDoubles<T>`: it breaks every plain `TwoDoubles` reference.
  - An interface: it cannot carry `equals`/`hashCode`.
  - A mutable twin: it splits equality and doubles the surface, and korlibs has deprecated its own.
  - An immutable `Square`: it is a further break.
- **Trade-off that needs a call: the equality contract.**
  - (C) Keep IEEE equality for the zeros, make NaN equal NaN, and hash `-0.0` like `0.0` and every
    NaN alike. Equality becomes lawful with a consistent hash, and no pair that is equal today
    becomes unequal.
  - (A) Fix only the hash. NaN points stay unequal to themselves.
  - (D) Total order, as Kotlin data classes use. It splits `0.0` from `-0.0`, which breaks
    MyGameTools' `isAtDestination`.
  - (E) Leave the defect.

  Recommendation: C, stated as a contract change in the migration notes. The parity findings
  proposed A; C covers A's hash fix and also makes NaN equality reflexive, which A leaves broken.
- **Trade-off that needs a call: the stability tier of subclassing `TwoDoubles`.** Nothing has
  subclassed it yet, because it was sealed. Its shape (the factory hook, the covariant `divideBy`)
  has no consumer evidence.
  - Experimental: gated by `@SubclassOptInRequired` with a new ERROR-level `@RequiresOptIn` marker,
    and graduating once a consumer builds on it.
  - Supported Extension: no opt-in, marked with a `@SupportedExtension` annotation that the library
    would add.
  - Stable Core.

  Recommendation: Experimental. In every option, using `Point`, `Dimensions` or a `TwoDoubles`
  value needs no opt-in.
- **Silent hazards.**
  - `p divideBy k` written as a statement compiles and does nothing, and nothing available at
    language version 2.2 can warn the caller.
  - Code that shared one `Point` between two holders loses the sharing when it switches to
    reassignment.
- **Proved only by a spike.** The covariant `Result<Point>` override, and `@SubclassOptInRequired`
  in common code at language version 2.2, have not been through a compiler. If the covariant
  override fails, `divideBy` moves to `Point` and `Dimensions` only. That is a further break for
  callers that hold a plain `TwoDoubles`; none exist downstream.

### API mechanics and proof of the API change

- **Recommended.**
  - `evaluateList(list: Iterable<T>, validator: (T) -> Boolean)`, not `inline`.
  - `divideBy` keeps its plain name. The `Catching` suffix marks a throwing/wrapping pair, and no
    throwing sibling remains.
  - The `Misc` functions split into `Misc.kt` (common) and `MiscJvm.kt` (JVM), both carrying
    `@file:JvmMultifileClass` and `@file:JvmName("MiscKt")`, so `read`, `asURL` and `Logger.time`
    stay on `MiscKt`. Every other file keeps its name, so its JVM facade is unchanged.
  - `com.spartanlabs.logging` is deleted.
  - `explicitApi()` turns on in a commit of its own, with no API change.

  Evidence: [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md).
- **Proof.**
  1. The Kotlin Gradle plugin's `abiValidation()` writes `api/GeneralTools.api` and
     `api/GeneralTools.klib.api`, and `check` runs `checkKotlinAbi`.
  2. The baseline JVM dump comes from tag `2.2.0`, whose `src/main` equals `master`. The
     binary-compatibility-validator produces it in a scratch copy of the tag.
  3. The build migration must leave that dump byte-identical.
  4. Each break lands in its own commit, and the combined diff is reviewed against a break ledger.
  5. Breaks a dump cannot show get compile or unit checks of their own: unsealing (a consumer's
     exhaustive `when` needs an `else`), the opt-in gate, and equality and logging behaviour.
- **Trade-off that needs a call: `Color.copy`.** A data class with an `internal` constructor
  carries a standing compiler warning unless it is annotated, so `Color` takes one of two
  annotations.
  - `@ExposedCopyVisibility` keeps `copy` public. The JVM dump then loses only the constructor, so
    the break set stays exactly as listed. But `copy` skips clamping, so it remains a public way to
    build an out-of-range `Color`, and every consumer `copy` call carries a compiler warning.
  - `@ConsistentCopyVisibility` makes `copy` internal too. That adds one break beyond the listed
    set: `Color.copy`, which no downstream code calls. It closes the last unvalidated way to build a
    `Color`, and the standard library recommends it for code that does not need binary
    compatibility.

  Recommendation: `@ConsistentCopyVisibility`. The API-mechanics findings preferred
  `@ExposedCopyVisibility` because it keeps the break set exact. Validated construction is the
  purpose of the internal constructor, and an exposed `copy` defeats that purpose.
- **Opt-in marker shape.** The marker is `@RequiresOptIn(level = ERROR)` with `@Retention(BINARY)`
  and `@Target()`. It can therefore only be passed to `@SubclassOptInRequired` or `@OptIn`, as with
  kotlinx.serialization's `SealedSerializationApi`. Its name and package go with the tier; the
  proposal is `ExperimentalForInheritanceGeneralToolsApi` in `com.spartanlabs.generaltools`.
- **Disagreement.** KEEP-0127 calls `Result` unsuited to domain errors, and the Kotlin guidelines
  advise a deprecation cycle and no data classes in public APIs. 3.0.0 holds to the library's
  paradigm instead: expected failures are returned as a `Result`, which the guidelines permit. A
  major release also makes clean breaks with no deprecation cycle. The migration notes say so.

### Downstream impact and pre-publish verification

- **Found.** Only MyGameTools and MyGameClient have source that stops compiling.
  - MyGameTools: 40 in-place mutation sites in 17 files, 2 `Color(...)` calls, and one design
    change. `GameObject.location` becomes an `open var`, which `VisibleObject` owns through
    `area.location`. Roughly 25 files and 150 to 200 lines change.
  - MyGameClient: 19 `Color(...)` calls in 6 files.
  - MyGameServer, WebTools, SpartanGraphics and KotlinBottools need nothing.
  - No downstream code uses `divideBy`, `evaluateList`, `com.spartanlabs.logging` or Java, and none
    has an exhaustive `when` over `TwoDoubles`.

  Evidence: [`downstream-blast-radius.md`](downstream-blast-radius.md).
- **Silent changes.** The migration notes list each one.
  - A mechanical `val` to `var` on `GameObject.location` desynchronises it from `area.location`.
  - `Alive`'s assignments of live `Point`s become snapshots.
  - The published `gametools` 5.0.0 calls `Point.setTo` and `plusAssign`. It fails with
    `NoSuchMethodError` if GeneralTools 3.0.0 resolves beside it.
  - GeneralTools has had a `Color.lightened` member since 2.1.0, and it shadows MyGameClient's own
    `Color.lightened` extension.
- **In this repository.**
  - `TwoDoubles`, `Point`, `Dimensions`, `Segment`, `Vectors.kt`, `Intersections.kt` and the common
    part of `Misc.kt` move onto the new model. Their logger fields and the 12 failure sites move to
    the helper.
  - `Square`, `AxisAlignedBox`, `Ray` and `CenteredBox` keep their shape. Their KDoc now states
    immutability as a guarantee.
  - `UserActions.kt` and the JVM part of `Misc` keep slf4j.
- **Pre-publish sequence**, on the maintainer's machine. Steps 1 to 3 gate step 4.
  1. A throwaway MyGameTools migration in a scratch clone, built against this repository through
     `--include-build`. It settles API gaps while additions are still free.
  2. Publish to a file repository under `build/` and run a consumer smoke matrix against it:
     Kotlin 2.2.0 JVM, Kotlin 2.1.20 JVM, Kotlin 2.4 multiplatform, and plain Maven on
     `GeneralTools-jvm`. Re-run the migrated clone against the same repository.
  3. Upload without releasing, and repeat both checks against the Portal's download endpoint.
  4. Press Publish, then check once from plain `mavenCentral()`.
  5. Tag and release.

  A pre-release version is not used: it is permanent, and its bytes differ from the release.
- **Trade-off that needs a call: where the smoke project lives.**
  - Committed as a separate `consumer-smoke/` build of about 30 lines, outside the main build and
    run by hand before each release. It then guards the Kotlin floor on every later release.
  - Kept outside the repository and discarded after the release.

  Recommendation: committed.
