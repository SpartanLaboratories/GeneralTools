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

*Pending.*
