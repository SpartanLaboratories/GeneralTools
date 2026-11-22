# Research: GeneralTools 3.0.0 on Kotlin Multiplatform

- **Issue:** [SpartanLaboratories/GeneralTools#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7),
  *"Make GeneralTools Kotlin Multiplatform (Color and geometry usable from common code)"*. The
  same change resolves
  [SpartanLaboratories/GeneralTools#8](https://github.com/SpartanLaboratories/GeneralTools/issues/8),
  *"Read-only views for Point and Square so libraries can expose geometry without copying"*.
- **Release:** `3.0.0`, a major version, released to Maven Central automatically when it lands on
  `master`.
- **Baseline:** the branch `feat/issue-7-kotlin-multiplatform` starts from `master` @ `17d30bc`, and
  the main sources are unchanged since `6926069`. `./gradlew build` passes there with 185 tests, no
  failures and none skipped. `master` has since moved to `c196d1a`, which only deletes
  `src/test/resources/keys.txt`.
- **Branch state for questions 11 to 13:** `29dd4ff`, where the build migration, explicit API,
  logging, common code, the `TwoDoubles` and box-model changes and the `Misc`/`Color` breaks are
  built. `./gradlew build` passes there with 392 JVM, 354 JS and 354 Wasm tests, none failing or
  skipped, and `checkKotlinAbi` green.
- **Citations:** documents by section, code by symbol; outside repositories at a pinned tag or
  commit.

This file holds the research questions behind the 3.0.0 design, then the verification of the
outside-world claims their answers rest on, then a topic-by-topic summary. Each question's
findings live in the file it names.

---

## What 3.0.0 is

- **Targets and modules.** `jvm` (bytecode 17), `js` and `wasmJs`, built by a shared core
  multiplatform module, `GeneralTools-core`. The JVM-only desktop code ships in a module of its
  own, `GeneralTools-desktop`, and `GeneralTools` is a source-less umbrella that depends on both.
  There are no Android or native targets.
- **Placement.** `Color`, the whole `com.spartanlabs.geometry` package and the platform-neutral
  `Misc` helpers live in common code. `read`, `asURL` and the slf4j `Logger.time` extension stay
  JVM-only in the core module's JVM target, because they share the JVM facade class `MiscKt` with
  the common `Misc` helpers. `UserActions` lives in `GeneralTools-desktop`, in the same package.
- **Logging.** All code, the JVM sources included, logs through kotlin-logging 8.0.7, which
  delegates to slf4j on the JVM, so a consumer's logging backend keeps working unchanged. One
  internal helper logs each expected failure at WARN and builds its `Result.failure`; every failure
  site goes through it. Exception messages stay as in 2.x, and the WARN line keeps the diagnostic
  values 2.x logged.
- **Build.** The versions of every dependency and plugin live in `gradle/libs.versions.toml`, and
  the configuration the modules share lives in convention plugins in an included `build-logic`
  build. The Java floor, 17, is set apart from the toolchain and guarded in `check`. Each published
  module's javadoc jar is its Dokka output.
- **Breaking changes, and only these.**
  - The library becomes multiplatform and is split into `GeneralTools-core` and
    `GeneralTools-desktop`. `GeneralTools` 3.0.0 is an umbrella over both, so a consumer of
    `GeneralTools` keeps its coordinate, in Gradle and in plain Maven alike.
  - `TwoDoubles` is no longer `sealed`. Subclassing it is Experimental, behind an ERROR-level
    opt-in marker.
  - `TwoDoubles.divideBy` divides in place and returns `Result<Unit>` instead of throwing. A zero
    divisor leaves the value unchanged and logs at WARN instead of ERROR. It divides each component
    directly, so its results can differ from 2.x's reciprocal product by one unit in the last place.
  - `evaluateList` takes a `(T) -> Boolean` and moves to common code.
  - The `com.spartanlabs.logging` package is removed.
  - `Square` is renamed `Rectangle`, and its `location` and `dimensions` references become
    read-only (`val`). The `Point` and `Dimensions` it holds stay mutable.
    `AxisAlignedBox.toSquare()` becomes `toRectangle()`.
  - `TwoDoubles` equality becomes lawful: NaN equals NaN, `-0.0` hashes like `0.0`, and `0.0`
    still equals `-0.0`.
  - `Color`'s public constructor validates its channels: a value outside `0..255` throws
    `IllegalArgumentException`, through `copy` as well. `Color.of` still clamps.
  - `trimIfLongerThan` throws `IndexOutOfBoundsException` for a negative length on every platform.
  - `capitalizeEveryWord` stops dropping commas that are part of its input, with a test for that
    case.

  Each break is clean, with no deprecation cycle. The public API dump changes by exactly the
  signature changes in this set; the behaviour changes get tests of their own.
- **Kept as in 2.x.**
  - `Point` and `Dimensions` stay mutable, with their in-place members.
  - `/=` still throws `IllegalArgumentException` for a zero divisor, now without a log line.
  - `Rectangle`'s `min` and `size` still return its live `location` and `dimensions`.
  - `Color` keeps its public constructor and its public `copy`.
- **Added.**
  - `Rectangle.toImmutable()` returns an `ImmutableRectangle`, a new immutable type.
  - `AxisAlignedBox` gains `minX`, `minY`, `maxX`, `maxY`, `width` and `height` accessors with
    default bodies, and its `contains` becomes an operator (`p in box`).
  - `Point` gains `unaryMinus`.
  - Every `TwoDoubles` destructures into its two components.
  - `plus`, `minus`, `times` and `div` for `Point` and `Dimensions` ship as extension operators in
    `com.spartanlabs.geometry.arithmetic`, which a file opts into by importing them. They are a
    Supported Extension, marked with the new `@SupportedExtension` annotation.
- **Not part of the change.** `src/test/resources/keys.txt` is already deleted on `master`. Moving
  the test sources neither moves nor modifies it, and bringing `master` into the branch removes it
  there.
- **Coordinates.** Group `io.github.spartanlaboratories`. The artifacts are `GeneralTools` (the
  umbrella), `GeneralTools-core` (with its `-jvm`, `-js` and `-wasm-js` artifacts) and
  `GeneralTools-desktop`, all at one version. Every package name stays.
- **Release notes.** `CHANGELOG.md`, in the Keep a Changelog layout, carries a `3.0.0` section that
  lists every change and links the README's migration notes, which hold the symptom, cause and fix
  of each break. The GitHub release notes open with that section.
- **Release mechanics.** There is no CI publish. Before the landing on `master`, a throwaway
  MyGameTools migration and a consumer smoke project, both kept outside the repository, are built
  against a file-repository publication of the exact tree that lands. The landing is then released
  automatically as one release event, tagged with a bare `3.0.0`, and given a GitHub release.
- **Downstream.** MyGameTools (SpartanLabsGaming/MyGameTools) consumes GeneralTools `2.2.0` as an
  `api` dependency on `master`, and builds with Kotlin 2.2.0. Of its published releases, `gametools`
  5.1.0 also requires GeneralTools `2.2.0`; 5.0.0, which MyGameClient and MyGameServer use,
  requires `2.0.1`. It stays on Kotlin 2.2.0 when it adopts 3.0.0. It will not compile
  unchanged against 3.0.0: it names `Square` in 14 files and assigns `area.dimensions` in
  `VisibleObject`. Its in-place `Point` and `Dimensions` updates keep compiling.

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
  commas that are part of its input, and the `Point.toString` test checks the layout rather than
  the platform's digits. JVM-only calls in code that moves to common are replaced:
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

### 6. What shape would an immutable geometry model take, and what would be left of read-only views if `Point` and `Dimensions` were immutable?

- **Options.** Live mutable state through a `var` that holds an immutable value, or through a
  read-only/mutable pair of types; `TwoDoubles` as an abstract base class, an interface, or no
  shared base; `Square` kept mutable with a read-only view, or made immutable.
- **Requirements.**
  - The model studied here makes `Point` and `Dimensions` immutable. 3.0.0 keeps them mutable;
    question 9 covers that model. `TwoDoubles` is no longer `sealed`, so consumers can define
    their own two-double types. `divideBy` returns a `Result` and logs through the failure helper
    instead of throwing.
  - A library can hand out geometry that its callers cannot mutate, without copying it. Issue #8
    names the cost today: MyGameTools' `ZoneGrid` and `UnzonedPointException` copy `Point`s, and
    `Zone.bounds` exposes a shared `Square` guarded only by a prose "do not mutate".
  - Equality requires the same concrete class and the same values.
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
- **Requirements.** Every `Color` holds channels in `0..255`. 3.0.0 keeps the constructor public
  and makes it validate; the study also covers an `internal` constructor that leaves the
  factories as the only way in. `evaluateList` takes `(T) -> Boolean` and lives in common
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
  (the throwing `divideBy`, the slf4j logger fields, `com.spartanlabs.logging`, and in the
  immutable model studied in question 6, mutable `Point`/`Dimensions`) gets a verdict: move onto
  the new model, or not. A Central release cannot be undone. The sweep here assumes immutable
  `Point` and `Dimensions`; question 10 repeats it for the mutable model.
- **Evidence that settles it.** This repository's code and history; the downstream repositories'
  code, read-only; `git log` and `git blame` on the areas touched.
- **Findings:** [`downstream-blast-radius.md`](downstream-blast-radius.md).

### 9. With `Point` and `Dimensions` mutable, what shape do `divideBy`, the in-place operators, the renamed `Square` and its immutable copy take?

- **Options.**
  - `divideBy` on a mutable type: divide in place and return `Result<Unit>`; divide in place and
    return the receiver inside the `Result` (`Result<Point>` on `Point`); or leave the receiver
    alone and return a new instance.
  - `/=` (`divAssign`) when the divisor is zero: log the failure and leave the value unchanged;
    throw; divide by IEEE rules (Infinity or NaN); or drop the operator.
  - Non-assigning operators (`div`, and with it `plus`, `minus`, `times`): add them or not. Kotlin
    reports `a += b` on a `var` as ambiguous when both `plus` and `plusAssign` exist.
  - The immutable copy: a type that holds four `Double`s, or one that holds immutable point and
    size types; whether it implements `AxisAlignedBox`, whose members return mutable `Point` and
    `Dimensions`; how it relates to `CenteredBox`.
  - Read-only views: whether `AxisAlignedBox` still protects anything when `min` and `size` return
    the live mutable parts; whether read-only interfaces in the shape issue #8 sketches
    (`ReadOnlyPoint`, `ReadOnlySquare`) earn a place.
  - Names for the renamed `Square` and for the immutable copy type.
- **Requirements.**
  - `Point` and `Dimensions` stay mutable, as in 2.x, with their in-place members: `var x`, `y`,
    `width` and `height`, `setTo`, `modBy`, `amplifyBy`, `plusAssign`, `minusAssign`, `timesAssign`
    and `divAssign`.
  - `TwoDoubles` is no longer `sealed`. Subclassing it is Experimental, gated by
    `@SubclassOptInRequired` with the ERROR-level marker `ExperimentalForInheritanceGeneralToolsApi`.
  - `divideBy` returns a `Result` and logs at WARN through the failure helper instead of throwing.
  - Equality requires the same concrete class and IEEE-equal components, except that NaN equals
    NaN; `-0.0` hashes like `0.0`.
  - `Square` gets a new name. Its `location` and `dimensions` references become `val`, and the
    parts stay mutable.
  - The renamed type provides a method that returns an immutable copy of itself, as a new
    immutable type in a stability tier of its own.
  - Library-design rules: open hierarchies at genuine extension points, policy behind parameters,
    additive and opt-in extension, and every public seam in one stability tier.
  - The 3.0.0 breaks are the listed set; an option that needs a further break says so.
  - Geometry sits on game hot paths (per-frame movement, raycasts).
- **Evidence that settles it.** Kotlin's operator-overloading rules; comparable libraries that pair
  a mutable rectangle with an immutable one (JavaFX, AWT, Android, Compose, libGDX, korlibs,
  OPENRNDR); the equality and opt-in findings in
  [`immutable-geometry-model.md`](immutable-geometry-model.md), which carry over; how MyGameTools
  uses the types.
- **Findings:** [`mutable-geometry-model.md`](mutable-geometry-model.md).

### 10. Under the mutable model, what does 3.0.0 break downstream, what changes in this repository, and what must the API dump show?

- **Options.** For each downstream site: it keeps compiling; it fails to compile; or it compiles
  and behaves differently. For the published `gametools` 5.0.0: whether it can still run beside
  GeneralTools 3.0.0.
- **Requirements.** The 3.0.0 break set above. MyGameTools stays on Kotlin 2.2.0. The downstream
  repositories are read, never changed, by this work, and nothing is filed against them. Every
  class here built on a mechanism that 3.0.0 changes gets a verdict. The API dump changes by
  exactly the signature part of the break set.
- **Evidence that settles it.** The downstream repositories' code, read-only; the bytecode of the
  published `gametools` 5.0.0; this repository's code; the consumer facts in
  [`downstream-blast-radius.md`](downstream-blast-radius.md) and the dump method in
  [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md).
- **Findings:** [`downstream-under-mutable-model.md`](downstream-under-mutable-model.md).

### 11. How are the core module, the desktop module and the source-less `GeneralTools` umbrella built and published, so that 2.x consumers keep their coordinate, plain-Maven consumers get real classes, and no consumer's build ends up with two copies of the same classes?

- **Options.**
  - The desktop module as a Kotlin/JVM module (one jar), or as a multiplatform module with a
    `jvm()` target only (a root artifact plus a `-jvm` artifact).
  - The umbrella as a JVM-only module with `api` dependencies on the core and desktop modules; as a
    multiplatform module whose `jvm` variant depends on both and whose `js` and `wasmJs` variants
    depend on the core module; or as a metadata-only publication. MyGameTools' `gametools`
    umbrella is prior art.
  - Against a `GeneralTools:2.2.0` that a published `gametools` 5.x still pulls in beside the new
    artifacts: dependency constraints from the new modules on `GeneralTools` 3.0.0 (as
    `kotlin-stdlib` 1.8.0 aligned `kotlin-stdlib-jdk7` and `-jdk8`), Gradle capabilities, or a
    statement in the migration notes only.
  - For the package `com.spartanlabs.generaltools`, present in both the core JVM jar and the desktop
    jar: `Automatic-Module-Name` entries, or none.
  - The release rehearsal: every module published to one file repository and resolved from there by
    the smoke matrix, then one `publishAndReleaseToMavenCentral` invocation for every module.
- **Requirements.**
  - The artifacts are `GeneralTools-core` (multiplatform: `jvm`, `js`, `wasmJs`, with the derived
    `-jvm`, `-js` and `-wasm-js` artifacts), `GeneralTools-desktop` (JVM) and `GeneralTools`, a
    source-less umbrella that depends on both. All carry one version, 3.0.0, under the group
    `io.github.spartanlaboratories`.
  - `GeneralTools` 3.0.0 works as a dependency of a Gradle JVM project, of a Gradle multiplatform
    project, and of a plain Maven project, which then gets real classes.
  - The core module's JVM variant keeps `read`, `asURL` and `Logger.time`, which share the JVM facade
    class `MiscKt` with common code; the desktop module holds `UserActions` (AWT, commons-io), in
    the same package.
  - JVM consumers on Kotlin 2.2.0 and 2.1.20 resolve and compile against the artifacts; web
    consumers need Kotlin 2.4.
  - Every artifact meets Maven Central's requirements (POM metadata, sources and javadoc jars,
    signatures), and every javadoc jar is Dokka output.
  - The release is one release event from one Gradle invocation, after a rehearsal against a
    file-repository publication of the exact tree that lands.
- **Evidence that settles it.** Gradle's documentation on module metadata, variant selection,
  dependency constraints, capabilities and how each maps to a POM; the vanniktech plugin's
  documentation and source at `0.37.0` (Kotlin/JVM and multiplatform configurations, a module with
  no sources, the upload of several modules as one deployment); Maven Central's publishing
  requirements; the published metadata of `kotlin-stdlib` 1.8.0; MyGameTools' umbrella build; the
  JPMS rules for split packages in automatic modules; published multiplatform libraries that ship
  an umbrella or split artifacts. It builds on
  [`kmp-maven-central-publishing.md`](kmp-maven-central-publishing.md).
- **Findings:** [`published-shape-after-the-split.md`](published-shape-after-the-split.md).

### 12. How is the build organised into convention plugins in an included `build-logic` build, with a version catalog, so that the core, desktop and umbrella modules share one configuration under Kotlin Gradle plugin 2.4.21 and Gradle 9.0.0?

- **Options.**
  - Convention plugins as precompiled script plugins (`kotlin-dsl`) or as plugin classes; how many,
    and how they compose (for example one for multiplatform libraries, one for JVM libraries, one
    for publishing). WebTools' and MyGameTools' `build-logic` builds are prior art.
  - Reading `gradle/libs.versions.toml` from `build-logic` (the generated `libs` accessors are not
    available inside precompiled script plugins without a workaround; plugin artifacts become
    `build-logic` dependencies) and from the root and module build scripts; where the settings
    plugin's version lives.
  - `includeBuild("build-logic")` inside `pluginManagement` or at the top level of the settings;
    the root build script's `apply false` plugins.
  - Dokka 2 in V2 mode, applied by the publishing convention plugin:
    `JavadocJar.Dokka("dokkaGeneratePublicationHtml")` for the multiplatform and the JVM modules,
    `failOnWarning`, the aggregation at the root (`dokka(project(...))`), and the umbrella's own
    javadoc jar.
  - Where the KGP ABI validation, the `-PtestLevel` selection and the committed
    `kotlin-js-store/yarn.lock` sit once there are several modules.
- **Requirements.**
  - The shared build configuration lives in convention plugins in a `build-logic` build that the
    settings include; the versions of every dependency and plugin live in
    `gradle/libs.versions.toml`; publishing goes through `com.vanniktech.maven.publish` with every
    publication signed; the Gradle wrapper stays at 9 or later.
  - Dokka 2 in V2 mode is applied in the convention plugin for published modules; every Dokka
    configuration sets `failOnWarning`; the Kotlin plugins sit on the root project's plugin
    classpath with `apply false`.
  - What is built keeps working unchanged: the language and API pins at 2.2, the published
    `kotlin-stdlib` 2.2.0 requirement, the web stdlib at the plugin's version, `explicitApi()`,
    `abiValidation()` with a committed dump per module, `-PtestLevel=<set>[,<set>]` on every test
    task, and the `jvm`, `js` and `wasmJs` targets on Node.
  - Each step of the restructuring leaves the published output identical except where it is meant
    to change (the javadoc jar gaining content, the split's new coordinates), and that is provable,
    for example by comparing the generated POM and module files before and after.
  - New Kotlin files follow the organization's import grouping.
- **Evidence that settles it.** Gradle's documentation on sharing build logic, precompiled script
  plugins and version catalogs in included builds; the Kotlin Gradle plugin 2.4.21 documentation;
  Dokka 2's documentation (V2 mode, multi-module builds, javadoc jars, multiplatform); the vanniktech
  plugin's documentation; WebTools' and MyGameTools' `build-logic`; the `build-logic` of published
  multiplatform libraries. It builds on
  [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md) and
  [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md).
- **Findings:** [`build-logic-and-version-catalog.md`](build-logic-and-version-catalog.md).

### 13. How does a multiplatform library's JVM target set its Java floor apart from the toolchain, and guard it in `check`, under Kotlin Gradle plugin 2.4.21?

- **Options.**
  - `jvmTarget` with `-Xjdk-release` on the `jvm()` compilations, or `jvmTarget` alone with a
    toolchain equal to the floor; which JDK the toolchain runs the build on.
  - Publishing the floor as `org.gradle.jvm.version` on the multiplatform JVM variants (set by hand
    today) and on the desktop and umbrella modules' variants.
  - A `verifyJavaFloor` task that reads the class-file versions of the built jar and its runtime
    classpath, as WebTools' does, with its inputs for a multiplatform `jvm` target and for a
    Kotlin/JVM module, wired into `check`.
- **Requirements.**
  - The floor is the oldest Java release the compiled output runs on, set on its own apart from the
    toolchain, and never below 11. WebTools is the worked example: one constant sets `jvmTarget` and
    `-Xjdk-release`, the floor is published as the `TargetJvmVersion` attribute, and a
    `verifyJavaFloor` task guards it in `check`.
  - GeneralTools' floor is 17 (class-file major 61) on every JVM artifact; the JVM classes built
    today are already 17.
  - The `js` and `wasmJs` compilations are unaffected.
  - One constant in `build-logic` sets the floor for every module.
- **Evidence that settles it.** The Kotlin Gradle plugin's documentation and source for
  `-Xjdk-release` in multiplatform JVM compilations; Kotlin's issue tracker on `-Xjdk-release`;
  Gradle's documentation on `TargetJvmVersion` and on the JVM variants of a multiplatform
  publication; WebTools' `VerifyJavaFloor.kt` and `JavaFloorScan.kt`. It builds on
  [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md).
- **Findings:**
  [`java-floor-on-a-multiplatform-jvm-target.md`](java-floor-on-a-multiplatform-jvm-target.md).

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

**Questions 9 and 10.** Their 27 load-bearing claims were re-opened at their sources on
2026-10-19. All hold, and none is unclear.

| Findings file | Claims checked | Hold | Do not hold |
|---|---|---|---|
| [`mutable-geometry-model.md`](mutable-geometry-model.md) | 19 | 19 | 0 |
| [`downstream-under-mutable-model.md`](downstream-under-mutable-model.md) | 8, plus the published artifacts it decodes | all | 0 |

The bytecode references re-check from the published `gametools-core` and `gametools-world` 5.1.0
jars: `com/spartanlabs/geometry/Square` is referenced exactly at the sites named, with the same
program counters. Two points carry their stated limits. The runtime `NoClassDefFoundError` is
derived from those references, not executed. SpartanGraphics' planned type names come from an
uncommitted planning draft.

Claims about this repository and its consumers, checked against the code directly for these two
questions:

- MyGameTools names `Square` on 39 code lines in 14 files.
- MyGameClient names it on 27 lines in 8 files, plus one comment-only reference.
- `VisibleObject`'s `dimensions` setter assigns `area.dimensions`.
- Maven Central serves `gametools`, `gametools-core` and `gametools-world` 5.1.0, and
  `gametools-core` 5.1.0 requires GeneralTools `2.2.0`.

**Questions 11 to 13.** Their 58 load-bearing claims were re-opened at their sources on 2026-11-21
and 2026-11-22. All hold and none is unclear. One of them is the result of a scratch build, which no
source can re-check.

| Findings file | Claims checked | Hold | Do not hold |
|---|---|---|---|
| [`published-shape-after-the-split.md`](published-shape-after-the-split.md) | 22, plus the published artifacts it decodes | all | 0 |
| [`build-logic-and-version-catalog.md`](build-logic-and-version-catalog.md) | 17 | 17 | 0 |
| [`java-floor-on-a-multiplatform-jvm-target.md`](java-floor-on-a-multiplatform-jvm-target.md) | 19 | 18, and one scratch-build result | 0 |

Four claims hold more narrowly than their wording:

- `kotlin-stdlib` 2.4.21's `dependencyConstraints` also pin `kotlin-stdlib-common` to 2.4.21; the
  quote in published-shape claim 3 shows only the two retired artifacts.
- Gradle's rule in published-shape claim 22 reads "Unless otherwise indicated, if at any point no
  candidates remain, resolution fails". Nothing here relies on an exception to it.
- Two of the version-catalog quotes in build-logic claim 4 describe `buildSrc`. The same page shows
  the `from(files("../gradle/libs.versions.toml"))` import, and Okio 3.18.2 uses that import from an
  included build (build-logic claim 8), which is the form this design takes.
- The compiler reports the conflict in java-floor claim 3 only when a `-jvm-target` is given that
  differs from the release. `jvmTarget` is therefore always set to the same value as the release.

Claims about this repository, checked against the code directly for these three questions:

- `settings.gradle.kts:4` names the root project `GeneralTools`.
- `build.gradle.kts:31` sets `jvmFloor = 17`, which feeds both `jvmToolchain` (`:35`) and the
  by-hand `TargetJvmVersion` attribute of the JVM target (`:54`).
- `UserActions.kt` uses no other declaration of the library: its imports (`:3-14`) are commons-io,
  slf4j, AWT, `java.io`, `java.net` and `javax.imageio`, and its `Rectangle` is the imported
  `java.awt.Rectangle`. It is the only file that uses AWT and commons-io.
- `Misc.kt:1-2` and `MiscJvm.kt:1-2` both declare `@file:JvmMultifileClass` and
  `@file:JvmName("MiscKt")`.
- The published 2.2.0 artifacts are class-file major 67, and their Gradle module file declares
  `org.gradle.jvm.version` 23.

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
  - The JVM floor is 17, set apart from any toolchain (see "Java floor" below). The JVM variant
    declares `org.gradle.jvm.version = 17` explicitly, since a multiplatform publication declares
    none.
  - Targets: `jvm()`, `js { nodejs() }` and `wasmJs { nodejs() }`. There is no `webMain` unless a
    shared JS/Wasm `actual` is needed, and no `browser()`.
  - `kotlin-js-store/yarn.lock` is committed. The versions live in a version catalog (see "Build
    structure" below).
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
- **Disagreement.** One widely read argument prefers `-Xjdk-release` to toolchains. The floor now
  holds through `jvmTarget` and `-Xjdk-release`, set apart from any toolchain, and CI runs the JVM
  tests on JDK 17, the floor itself (see "Java floor" below).
- **Rejected.**
  - Staying on the Kotlin Gradle plugin 2.2.0: Kotlin/Wasm is pre-Beta there, there is no `webMain`,
    and Gradle 9.0.0 is outside its documented range.
  - A global `coreLibrariesVersion` pin: it breaks the web compilations.
  - Kotlin Gradle plugin 2.3.21 with no stdlib pin: it works only while the build sits exactly one
    minor above its oldest consumer.

### Publishing

- **Recommended.** vanniktech `0.37.0`, the first release documented as tested against the Kotlin
  Gradle plugin 2.4.
  - `group` and `version` are set once for every module, and the coordinates are derived from
    them.
  - Each published module's javadoc jar is its Dokka output,
    `JavadocJar.Dokka("dokkaGeneratePublicationHtml")`, set by the build's conventions (see "Build
    structure" below).
  - The artifacts are those of the split set (see "Published shape after the split" below).

  Evidence: [`kmp-maven-central-publishing.md`](kmp-maven-central-publishing.md).
- **Release flow.** The landing on `master` is released automatically: one
  `publishAndReleaseToMavenCentral` invocation for the whole set, after a rehearsal against a
  file-repository publication of the exact tree (see "Published shape after the split" below). The
  tag is a bare `3.0.0`, and the GitHub release notes open with the CHANGELOG's 3.0.0 section. The
  README's "Releasing" section describes this flow.
- **Plain Maven.** A multiplatform root artifact holds no classes for a Maven build. The root POM of
  `GeneralTools-core` therefore depends on `GeneralTools-core-jvm`, the workaround Okio uses, so
  Maven consumers of `GeneralTools` and of `GeneralTools-core` get the JVM classes.

### Logging

- **Recommended.** kotlin-logging `8.0.7` as an `implementation` dependency, for the common code and
  the JVM sources alike (`MiscJvm.kt`, `UserActions.kt`). On the JVM it is runtime-only, so it stays
  off consumers' compile classpaths. Its JVM artifact requires kotlin-stdlib 2.1.21, as 8.0.4's does
  (its POM on Maven Central, read 2026-11-22), so it raises no consumer's stdlib floor.
  - `jvmMain` keeps `api(slf4j-api)`: `Logger.time` needs it, and kotlin-logging does not supply it.
  - Loggers keep their explicit names
    (`KotlinLogging.logger("com.spartanlabs.geometry.Intersections")`), so existing logback rules
    still match and JS and Wasm get real names.
  - One `internal fun <T> KLogger.failure(message: String, detail: (() -> Any?)? = null): Result<T>`
    builds the `IllegalArgumentException` from `message`, logs at WARN and returns the failure. The
    WARN line is `message`, followed by the rendered `detail` when one is given. `detail` is
    evaluated only when WARN is enabled, and it never reaches the exception.
  - All 12 failure sites use the helper. Each passes its 2.x exception message unchanged as
    `message`, and the diagnostics its 2.x log line carried (the function name and the offending
    values) as `detail`. So the exception text stays as in 2.x and the WARN line keeps the values.
  - Logback stays the JVM test backend.

  Evidence: [`kotlin-logging-adoption.md`](kotlin-logging-adoption.md).
- **Consumer-visible effect.** kotlin-logging 8 prints one startup line to stdout on first use, in
  every consumer, unless the consumer sets `-Dkotlin-logging.logStartupMessage=false`. The migration
  notes name the switch. Version 7.0.14 prints nothing, but it is the previous major.
- **JS and Wasm.** Output goes to the console through kotlin-logging's direct logger, which has one
  global level (INFO by default).

### Cross-platform behaviour

- **Found.** The tests that move to `commonTest` keep their expectations on `jvm`, `js` and
  `wasmJs`, with three exceptions whose subject changes by design: the `Point.toString` test, the
  `Square` test that reassigns `location` (the read-only references), and the `Color` test that
  expects the constructor not to clamp (the constructor now validates). The `divAssign`-by-zero
  test keeps its expectation, because `/=` still throws. Tests that name `Square` follow its
  rename. The one platform
  difference a test meets: `Point(1.0, 2.0).toString()` is `"1.0, 2.0"` on the JVM and Wasm but
  `"1, 2"` on JS, which prints a `Double` with JavaScript's number formatting. Evidence:
  [`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md).
- **`toString` follows the platform.** `TwoDoubles.toString` keeps each platform's own number
  formatting, and its KDoc says so. The `Point.toString` test checks the layout only: its expected
  text is built from the platform's own rendering (`"${1.0}, ${2.0}"`), or it uses values that print
  the same everywhere, such as `1.5` and `-2.25`. There is no normalising helper and no
  per-platform branch. Kotlin documents `Double.toString`'s format as unstable, and no code here or
  downstream parses or asserts on `Point` text.
- **Differences no test covers.**
  - On JS a `Float` is computed in double precision. `Color.lerp` with a non-dyadic fraction can
    therefore land one channel unit away from the JVM (about 0.01% of probed cases), and
    `luminance()` differs in its last digits. The KDoc states it; float32 emulation on JS is not
    added.
  - `trimIfLongerThan` with a negative length throws `IndexOutOfBoundsException` on every target,
    through an explicit check. Without it JS would return `""`; the JVM's own exception is a
    subtype of `IndexOutOfBoundsException`.
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
    - `MiscKtTest` is deleted. `src/test/resources/keys.txt` is already gone from `master`, and
      the move leaves it alone.
  - A `-PtestLevel=<level>[,<level>]` property adds
    `includeTestsMatching("com.spartanlabs.testing.<level>.*")` to every test task and sets
    `failOnNoMatchingTests = false`. Left unset, `check` runs every level on every target.

  Evidence: [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md).
- **Names.** The 182 backtick test names contain only letters, digits, spaces, hyphens and
  apostrophes, which JS and Wasm accept from language version 2.1, so the new targets force no
  rename.
  - The seven names that mention `Square` or `toSquare` follow the rename to `Rectangle` and
    `toRectangle`, and `SquareTest` becomes `RectangleTest`.
  - ColorTest's `primary constructor does not clamp out-of-range channels` takes a name that states
    its new assertion.
  - Every other name stays verbatim.
- **Rejected.**
  - Per-level test tasks per target: the plugin adds every test run to `check`, so each test would
    run once per level, and web test tasks cannot be registered by hand.
  - JUnit tags: common code cannot use them.

### Geometry model

- **Settled.**
  - `Point` and `Dimensions` stay mutable, as in 2.x, with every in-place member. Live state such
    as an entity's position updates in place without an allocation, and code that shares one
    `Point` between holders keeps working. The immutable model in
    [`immutable-geometry-model.md`](immutable-geometry-model.md) is therefore not used; its
    findings on equality, on the opt-in tier and on Kotlin's operator rules still apply.
  - `TwoDoubles` becomes an `abstract class` with `protected` constructors and no abstract members.
    Subclassing it is Experimental, through
    `@SubclassOptInRequired(ExperimentalForInheritanceGeneralToolsApi::class)`, and graduates once a
    consumer builds on it. Using `Point`, `Dimensions` or any `TwoDoubles` value needs no opt-in.
    `equals` and `hashCode` are `final`. `first`, `second` and the in-place verbs stay final, as
    they are today, and `toString` stays open.
  - Equality requires the same concrete class and IEEE-equal components, except that NaN equals
    NaN; `-0.0` hashes like `0.0`, and every NaN alike. No pair that is equal in 2.x becomes
    unequal; only NaN-versus-NaN pairs become equal. The migration notes state it as a contract
    change. Because the types are mutable, a `Point` used as a hash key must not change while it
    is in the map; the KDoc carries that warning and points to the immutable copy as the safe key.
  - `Square` is renamed `Rectangle`. Its `location` and `dimensions` references become `val`, so a
    holder can no longer swap the parts, while the parts themselves stay mutable. It stays a
    `data class`: `copy()` remains a shallow copy that shares the parts, as in 2.x, and its KDoc
    says so.
  - `Rectangle.toImmutable()` returns an `ImmutableRectangle`: a final, immutable type of four
    `Double`s (`x`, `y`, `width`, `height`) that implements `AxisAlignedBox`.
    - Its `equals`, `hashCode` and `toString` are written by hand to follow the same equality
      contract; a generated data class would split `0.0` from `-0.0`. It is never equal to the
      mutable type.
    - Its `min`, `max`, `center` and `size` return fresh `Point` and `Dimensions` values.
    - It is Stable Core: a final value type rather than an extension seam, stored the way JavaFX,
      korlibs and OPENRNDR store a rectangle, and needed wherever MyGameTools copies geometry
      defensively today.
  - `AxisAlignedBox` gains accessors with default bodies: `minX`, `minY`, `maxX`, `maxY`, `width`
    and `height` (Stable Core).
    - `Rectangle`, `CenteredBox` and `ImmutableRectangle` override them without allocating.
    - `contains` and the intersection functions read them, so the hot paths stop allocating per
      read. Without them, `rayIntersectsBox` makes up to nine allocations per call on a
      `CenteredBox`.
  - `AxisAlignedBox` is a shape contract, not a read-only guarantee. The immutable copy is what a
    library hands out when its callers must not mutate. There are no `ReadOnlyPoint` or
    `ReadOnlySquare` interfaces:
    - they would spread through every `Point` parameter;
    - they cannot carry `equals`;
    - as an untried seam they would need an opt-in, which defeats a type meant to be handed out.
  - **Names.** `Rectangle` is the mutable box and `ImmutableRectangle` its immutable copy. The plain
    name goes to the mutable type, as it does for `Point` and `Dimensions`, and `Immutable*` is the
    convention of Guava and kotlinx.collections.immutable. `java.awt.Rectangle` (which
    `UserActions.kt` imports), libGDX and SpartanGraphics' planned shape share the name; a file that
    needs two of them imports one under an alias.
  - **`divideBy`** divides in place and returns `Result<Unit>`, in the same family as `setTo`,
    `modBy` and `amplifyBy`, with no factory hook. A discarded result is harmless: a success has
    already happened, and a failure has already been logged with the value left unchanged. It
    divides each component directly. `49.0 * (1/49.0)` is `0.9999999999999999` where
    `49.0 / 49.0` is `1.0`, and a subnormal divisor's reciprocal overflows to Infinity. Results can
    therefore differ from 2.x by one unit in the last place, and the migration notes say so.
  - **`/=` with a zero divisor** throws `IllegalArgumentException("Cannot divide by zero")` without
    logging, exactly as 2.x does apart from the dropped ERROR line. The operator throws and the
    named function reports, as Kotlin's own `get` and `getOrNull` do. `divAssign` gets a block
    body, because over a `Result`-returning `divideBy` an expression body would no longer return
    `Unit`.
  - **`Rectangle.min` and `size`** keep returning the live `location` and `dimensions`, as in 2.x,
    while `max` and `center` return fresh points. An `AxisAlignedBox`-typed handle to a live
    `Rectangle` therefore still exposes its parts; the immutable copy is the protection.
  - **Operators.** Every operator that fits these types without making existing code ambiguous is
    added:
    - `Point.unaryMinus()` returns the negated point. `Dimensions` has none, because a negative size
      is not a valid size.
    - `component1()` and `component2()` on `TwoDoubles` return `first` and `second`, so
      `val (x, y) = point` and `val (w, h) = dimensions` destructure, as Compose's `Offset` and
      `Size` do. `Rectangle`, as a data class, already destructures into `location` and
      `dimensions`.
    - `AxisAlignedBox.contains` becomes an `operator`, so `p in box` reads naturally;
      `box contains p` still works.

    No downstream code declares its own operators on these types or destructures them, so none of
    the additions shadows or changes existing code. Not added:
    - `unaryPlus`, which has no meaning beyond identity and would either alias a mutable value or
      copy it unannounced;
    - `inc`, `dec`, `compareTo` and `rangeTo`, which have no geometric meaning;
    - `get`, because the components have names and an index would add an out-of-range failure;
    - `plus`, `minus`, `times` and `div` as *members* returning `Point` or `Dimensions`. With
      `plusAssign` and its siblings present, Kotlin reports `a += b` on a `var` as ambiguous when
      `plus` returns a subtype of the variable's type. Members would therefore break every `var`
      holder that uses `+=` in every file, and could not be added later in a minor either. The
      import-gated form below confines that cost to files that opt in.

  Evidence: [`mutable-geometry-model.md`](mutable-geometry-model.md).
- **Import-gated arithmetic.** `plus`, `minus`, `times` and `div` for `Point` and `Dimensions` ship
  as extension operators in `com.spartanlabs.geometry.arithmetic` (one file, `Arithmetic.kt`).
  - **Gating.** A file sees them only when it imports the package or one of its members. Code that
    does not import them resolves exactly as in 2.x: an extension outside its declaring package
    needs an import, and a member always wins over an extension
    ([`mutable-geometry-model.md`](mutable-geometry-model.md) claims 1 to 3).
  - **Which combinations.** Those with a geometric meaning in this library:
    - `Point + Point` and `Point - Point`: vector sum and difference;
    - `Point + Dimensions` and `Point - Dimensions`: a corner offset by a size, which are the
      library's own `Rectangle.max` and `CenteredBox.min`/`max` formulas;
    - `Point * Double`, `Point / Double` and `Double * Point`;
    - `Dimensions + Dimensions`, `Dimensions - Dimensions`, `Dimensions * Double`,
      `Dimensions / Double` and `Double * Dimensions`.
  - **Semantics.** Each operator returns a new value and leaves its operands unchanged. `div` by
    zero (`0.0` or `-0.0`) throws `IllegalArgumentException("Cannot divide by zero")` without
    logging, as `/=` does. It divides each component directly, so `p / k` equals what `p /= k`
    leaves.
  - **Not offered:**
    - `Dimensions ± Point`, because a size offset by a position has no meaning;
    - component-wise products;
    - operators on consumers' own `TwoDoubles` subclasses, which have no factory to build a
      result. For those subclasses, the package is the worked example to follow.
  - **Package name.** `arithmetic` rather than `operators`: the operators that cause no ambiguity
    (`unaryMinus`, `componentN`, `contains`) stay members, and the package gates only the
    value-returning arithmetic.
  - **Accepted costs**, which the KDoc and the migration notes state:
    - in a file that imports the package, `+=`, `-=`, `*=` and `/=` on a `var` `Point` or
      `Dimensions` holder are ambiguous and stop compiling. A `val` holder is unaffected.
    - a wildcard import of the package brings every operator in, and an IDE may add the import
      when `p + q` does not resolve.
    - a member operator added later would shadow the extension.
  - **Tier.** The operators are a Supported Extension: a likely but non-core need, with the same
    compatibility guarantee as Stable Core. They carry the new `@SupportedExtension` annotation, and
    the `TwoDoubles` subclassing seam moves onto it when it graduates. The annotation:
    - lives in `com.spartanlabs.generaltools`;
    - is `@MustBeDocumented`;
    - has `BINARY` retention, so a consumer's IDE shows it from the compiled library;
    - targets classes, functions, properties and type aliases;
    - is not an opt-in.
- **Rejected for `TwoDoubles`.** A recursive generic `TwoDoubles<T>`: it breaks every plain
  `TwoDoubles` reference. An interface: it cannot carry `equals`/`hashCode`.

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
     binary-compatibility-validator produces it in a scratch copy of the tag, with the tag's own
     build. Before the migration the baseline is restated as the plugin's own dump of the same
     classes, because the two tools do not list the same members (below). The restatement holds
     only if the two builds compile the same class files with the same members and descriptors,
     and every line it drops or adds names a member with the same descriptor and access flags in
     both builds, left out or listed by a rule in which the two tools differ.
  3. The build migration must leave the restated dump byte-identical.
  4. Each break lands in its own commit, and the combined diff is reviewed against a break ledger.
  5. Breaks a dump cannot show get compile or unit checks of their own: unsealing (a consumer's
     exhaustive `when` needs an `else`), the opt-in gate, equality, logging, `Color`'s validation
     and `trimIfLongerThan`.

  The ledger of dump lines for the mutable model is under "Downstream impact and pre-publish
  verification" below.
- **The two dump tools list different members.** Read at the sources on 2026-10-20:
  - The plugin's JVM dumper treats a `protected` member of a sealed class or of an enum class as
    not public: `return (memberVisibility == Visibility.PROTECTED) && (classKind == ClassKind.ENUM_CLASS || classKind == ClassKind.CLASS && modality == Modality.SEALED)`
    (`isEffectivelyInternal` in
    [`KotlinMetadataVisibilities.kt` @ `v2.4.21`](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/abi-validation/abi-tools/src/main/kotlin/org/jetbrains/kotlin/abi/tools/impl/jvm/KotlinMetadataVisibilities.kt)).
    It takes the visibility of a synthetic default-arguments constructor from the constructor
    that constructor serves:
    `name == "<init>" && "ILkotlin/jvm/internal/DefaultConstructorMarker;" in desc -> JvmMethodSignature(name, desc.replace("ILkotlin/jvm/internal/DefaultConstructorMarker;", ""))`
    (`alternateDefaultSignature` in
    [`KotlinMetadataSignature.kt` @ `v2.4.21`](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/abi-validation/abi-tools/src/main/kotlin/org/jetbrains/kotlin/abi/tools/impl/jvm/KotlinMetadataSignature.kt)).
  - binary-compatibility-validator `0.18.2` counts every `protected` member as public:
    `!isReified && isPublic(visibility, isPublishedApi)`, where `isPublic` accepts
    `Visibility.PROTECTED`
    ([`KotlinMetadataVisibilities.kt` @ `0.18.2`](https://github.com/Kotlin/binary-compatibility-validator/blob/0.18.2/src/main/kotlin/api/KotlinMetadataVisibilities.kt)).
  - For the 2.2.0 classes the two dumps differ by one line, the synthetic default-arguments
    constructor of the sealed `TwoDoubles`:
    `public synthetic fun <init> (DDILkotlin/jvm/internal/DefaultConstructorMarker;)V`.
    binary-compatibility-validator lists it; the plugin does not, because the primary constructor
    of a sealed class is `protected`. The tag's own build and the migrated build compile the same
    28 class files, `javap -p -s` prints the same text for each, and that constructor has the same
    descriptor and flags (`0x1001`, `ACC_PUBLIC, ACC_SYNTHETIC`) in both.
  - The same comparison shows what neither dump records. The 2.4.21 compiler marks the class-side
    `contains(Point)` of `Square` and `CenteredBox` `ACC_BRIDGE`, and its Kotlin metadata carries
    the module name `io.github.spartanlaboratories:GeneralTools` (2.2.0: `GeneralTools`) and
    different flags for data classes' generated `equals`, `hashCode` and `toString`. Callers link
    by name and descriptor, which are unchanged.
  - Once `TwoDoubles` is abstract, the plugin lists its `protected` constructors, a parameterless
    one and the synthetic default-arguments form. Its own test data dumps
    `public abstract class ProtectedConstructor protected constructor(public val a: Int = 0)` as
    `protected fun <init> ()V`, `protected fun <init> (I)V` and
    `public synthetic fun <init> (IILkotlin/jvm/internal/DefaultConstructorMarker;)V`
    ([`syntheticConstructors.txt` @ `v2.4.21`](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/abi-validation/abi-tools-tests/src/compiling/kotlin/cases/syntheticConstructors/syntheticConstructors.txt)).
  - The JVM backend adds that parameterless constructor, with the primary constructor's
    visibility, to a class whose primary-constructor parameters all have default values, and
    returns early for a sealed class (`irClass.modality == Modality.SEALED` in
    [`JvmDefaultConstructorLowering.kt` @ `v2.4.21`](https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/ir/backend.jvm/lower/src/org/jetbrains/kotlin/backend/jvm/lower/JvmDefaultConstructorLowering.kt)).
  - The plugin's klib dump lists no constructor of a sealed class: "Exclude constructors of sealed
    classes from ABI dump."
    ([`LibraryAbiReaderImpl.kt` @ `v2.4.21`](https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/util-klib-abi/src/org/jetbrains/kotlin/library/abi/impl/LibraryAbiReaderImpl.kt)).
  - The statement in [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md)
    recommendation 7 that "the same files are produced by both" holds for the text format, not for
    the members each tool lists.
- **`Color` keeps a public constructor that validates.** An `init` block requires every channel to
  be in `0..255` and throws `IllegalArgumentException` otherwise. `copy` goes through the same
  constructor, so it validates too and needs no copy-visibility annotation. `Color.of` still clamps.
  Every factory (`of`, `fromNormalized`, `fromPackedInt`, `fromHex`) and every transform already
  produces in-range channels, with one exception: `fromHex` given a sign character, such as
  `"#-1-1-1"`, parses each channel to `-1`. That input now throws `IllegalArgumentException`, which
  `fromHex`'s KDoc already promises for non-hex digits. A caller that passed out-of-range values,
  which 2.x accepted silently, now gets an exception, and the migration notes name both behaviour
  changes. A
  validating public constructor keeps every existing `Color(...)` call compiling while enforcing
  the `0..255` contract that the 2.x KDoc only stated, so the dump keeps `Color`'s constructor and
  `copy`. The `internal` constructor studied in
  [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) is not used.
- **Supported Extension marker.** `@SupportedExtension`, in `com.spartanlabs.generaltools` beside
  the opt-in marker, documents the tier and gates nothing; the import-gated arithmetic package is
  its first user. Its shape is under "Geometry model".
- **Opt-in marker.** `ExperimentalForInheritanceGeneralToolsApi` in `com.spartanlabs.generaltools`,
  declared `@RequiresOptIn(level = ERROR)`, `@Retention(BINARY)` and `@Target()`. It can therefore
  only be passed to `@SubclassOptInRequired` or `@OptIn`, as with kotlinx.serialization's
  `SealedSerializationApi`.
- **Disagreement.** KEEP-0127 calls `Result` unsuited to domain errors, and the Kotlin guidelines
  advise a deprecation cycle and no data classes in public APIs. 3.0.0 holds to the library's
  paradigm instead: expected failures are returned as a `Result`, which the guidelines permit. A
  major release also makes clean breaks with no deprecation cycle. The migration notes say so.

### Downstream impact and pre-publish verification

- **Found.** Downstream, the break is the rename plus one assignment.
  - 66 source lines in 22 files stop compiling: 39 lines in 14 files in MyGameTools, and 27 in 8
    in MyGameClient.
  - `VisibleObject`'s `area.dimensions = value` no longer compiles against a `val` reference.
  - MyGameTools' 40 in-place mutations, and every `Color(...)` call, keep compiling.
  - MyGameServer, WebTools and SpartanGraphics need nothing.
  - No downstream code uses `divideBy`, `/=`, `evaluateList`, `com.spartanlabs.logging`,
    `Color.copy`, `min` or `size` of a `Square`, an exhaustive `when` over `TwoDoubles`, or Java.

  MyGameTools' migration is about 17 files and 45 lines, plus a CHANGELOG entry. `Square` appears
  in MyGameTools' public API, so the migration is a major for MyGameTools' own consumers. Evidence:
  [`downstream-under-mutable-model.md`](downstream-under-mutable-model.md). The consumer map and
  Kotlin floors in [`downstream-blast-radius.md`](downstream-blast-radius.md) still hold.
- **Published `gametools` beside 3.0.0.** Every published `gametools` (5.0.0 and 5.1.0) references
  `com/spartanlabs/geometry/Square`.
  - A build that resolves one of them with GeneralTools 3.0.0 fails at runtime with
    `NoClassDefFoundError` at the first `new Square`. That happens when constructing any `Actor` or
    `Alive` (through `VisibleObject`'s constructor), a `TiledMap` or a `ZoneGrid`.
  - Every `Point`, `Dimensions`, `TwoDoubles` and `Color` reference in them keeps linking.

  The release notes say that no published `gametools` runs beside GeneralTools 3.0.0, and that
  MyGameTools' consumers wait for a MyGameTools release built on it.
- **Silent changes.** Each goes in the migration notes.
  - `VisibleObject.dimensions = d` becomes `area.dimensions.setTo(d)`, which copies the values
    rather than adopting the caller's instance.
  - `Actor.isAtDestination` (`==` on `Point`) changes only when both positions are NaN, where
    equality is now reflexive.
  - `Rectangle`'s `toString()` prints `Rectangle(...)` where 2.x printed `Square(...)`.
  - MyGameClient's `Portrait` builds a `Color` from channels it receives over the network. A value
    outside `0..255` now throws while rendering, so untrusted input belongs with `Color.of`, which
    clamps.
  - MyGameClient's own `Color.lightened` extension stays shadowed by the GeneralTools member of the
    same name, as it has been since 2.1.0.
- **In this repository.**
  - These files change: `TwoDoubles.kt`; `Square.kt`, which becomes `Rectangle.kt`;
    `AxisAlignedBox.kt`; `CenteredBox.kt`; `Intersections.kt` (the primitive accessors); `Color.kt`
    (validation and `toHex`); and `Misc.kt`, which splits.
  - `Square.kt` declares no top-level functions, so renaming the file changes no JVM facade.
    `CenteredBox.kt` keeps its file name, so its facade `CenteredBoxKt` stays.
  - New: `ImmutableRectangle`, the opt-in marker, `@SupportedExtension`, the arithmetic file
    `geometry/arithmetic/Arithmetic.kt`, and the internal helpers.
  - Every source file gains explicit-API visibility modifiers. Beyond those, `Point.kt` gains
    `unaryMinus`, and it, `Segment.kt` and `Vectors.kt` change only in logging and the failure
    helper.
  - `Ray.kt` and `UserActions.kt` change only by those modifiers, and `UserActions.kt` moves to
    JVM-only code and then to `GeneralTools-desktop`.
  - `com.spartanlabs.logging` is deleted.
  - Every slf4j logger field moves to kotlin-logging, and the 12 failure sites move to the failure
    helper. `UserActions.kt` and the JVM part of `Misc` log through kotlin-logging as well;
    `Logger.time` keeps its slf4j receiver.
  - Tests that change by design:
    - `SquareTest`'s reassignment of `location`, which uses `setTo` instead;
    - `ColorTest`'s "primary constructor does not clamp", which now expects
      `IllegalArgumentException` and takes a name that says so;
    - tests that name `Square`, which follow the rename in their code and their names.

    `PointTest`'s `divAssign`-by-zero test already expects `IllegalArgumentException`, and `/=`
    still throws it, so that test stays as it is.
- **The dump ledger.** Against the 2.2.0 baseline:
  - `Square` (19 members) and the four `com.spartanlabs.logging` classes leave.
  - `Rectangle` (24 members, no setters), `ImmutableRectangle`, the opt-in marker and the
    `SupportedExtension` annotation arrive.
  - The facade `com/spartanlabs/geometry/arithmetic/ArithmeticKt` arrives with twelve operator
    functions:
    - `plus` and `minus`, each for `Point` with `Point`, `Point` with `Dimensions`, and
      `Dimensions` with `Dimensions`;
    - `times` and `div` on each type with a `Double`;
    - `times` with a `Double` receiver for each type.
  - `AxisAlignedBox` gains six accessors (with `$DefaultImpls`), and `CenteredBox` six overrides.
  - `CenteredBoxKt`'s two conversions name `Rectangle`, and `toSquare` becomes `toRectangle`.
  - `MiscKt.evaluateList` takes a `Function1`.
  - `TwoDoubles` loses its two synthetic accessor constructors and `divideBy (D)V`. It gains three
    `protected` constructors (`()V`, `(DD)V` and `(Lcom/spartanlabs/geometry/TwoDoubles;)V`), the
    line of its synthetic default-arguments constructor (present in 2.2.0, and left out of the
    plugin's dump while the class is sealed), `divideBy-IoAF18A (D)Ljava/lang/Object;`,
    `component1 ()D` and `component2 ()D`, and its `equals` and `hashCode` become `final`.
  - `Point` gains `unaryMinus ()Lcom/spartanlabs/geometry/Point;`.
  - `Dimensions`, `Segment`, `Ray`, `Color`, `IntersectionsKt`, `VectorsKt` and `UserActionsKt`
    show no diff.

  The JVM dump does not record the `operator` modifier, so `contains` becoming an operator leaves
  it unchanged. What the dump cannot show gets a test or a smoke-build check of its own: unsealing,
  the opt-in gate, equality, `divideBy`'s semantics, `/=` still throwing for a zero divisor,
  `Rectangle.min` and `size` still returning the live parts, the operator forms (`-p`,
  `val (x, y) = p`, `p in box`), the import gating of the arithmetic operators, `Color`'s
  validation, `trimIfLongerThan` and `capitalizeEveryWord`.
- **Documentation.** The README's intro, coordinates, modules, build-and-test, releasing, `Color`
  and geometry quick-reference sections change, and a migration-notes section is added.
  `CHANGELOG.md` gains a 3.0.0 section that lists every change and links those notes.
  `docs/plans/1-color-methods/plan.md` and `docs/plans/3-geometry-primitives/plan.md` each get a
  dated one-line note at the top pointing to this folder.
- **Pre-publish sequence**, on the maintainer's machine, before the landing on `master`. Steps 1 and
  2 gate the landing.
  1. A throwaway MyGameTools migration in a scratch clone, built against this repository through
     `--include-build`. It settles API gaps while additions are still free.
  2. Publish the whole set to a file repository under `build/` and run a consumer smoke matrix
     against it: Kotlin 2.2.0 JVM and Kotlin 2.1.20 JVM on `GeneralTools`; Kotlin 2.4
     multiplatform with `GeneralTools-core` in common code and `GeneralTools-desktop` on the JVM;
     plain Maven on `GeneralTools`; and a Gradle build that also pulls `GeneralTools:2.2.0` through
     `gametools` 5.1.0. Re-run the migrated clone against the same repository. Load a published
     `gametools-core` 5.1.0 against the new artifacts once, so the migration notes quote the real
     error.
  3. The landing on `master` is released automatically as one release event, tagged `3.0.0`, with
     a GitHub release whose notes open with the CHANGELOG's 3.0.0 section. Then resolve the set once
     from plain `mavenCentral()`.

  A pre-release version is not used: it is permanent, and its bytes differ from the release. The
  smoke project and the MyGameTools clone live outside the repository and are discarded after the
  release.

### Published shape after the split

- **Recommended.**
  - `GeneralTools-core` is the multiplatform module (`jvm`, `js`, `wasmJs`). It publishes the root
    `GeneralTools-core` and `GeneralTools-core-jvm`, `-js` and `-wasm-js`. Its JVM target keeps
    `read`, `asURL` and `Logger.time`, which share the facade class `MiscKt` with common code.
  - `GeneralTools-desktop` is a Kotlin/JVM module with one jar, holding `UserActions` in the package
    `com.spartanlabs.generaltools`. A multiplatform module with a `jvm()` target only would add an
    empty root artifact and a `-jvm` artifact, and bring the empty-root problem back for Maven.
  - `GeneralTools` is a source-less umbrella built with `java-library`: no Kotlin plugin, so no ABI
    dump and no explicit-API rules on an empty module. It has `api` dependencies on both modules
    and publishes an empty jar, a POM and a Gradle module file, in 2.2.0's shape, with sources and
    Dokka jars aggregated from both modules, as MyGameTools' `gametools` umbrella does. Because the
    two modules share a package, a count check guards the folded sources jar against entries
    silently dropped as duplicates.
  - The root POM of `GeneralTools-core` depends on `GeneralTools-core-jvm` (Okio's rewrite of the
    multiplatform publication's POM, on the same plugin version), so a plain Maven consumer of
    `GeneralTools` or of `GeneralTools-core` gets the JVM classes. The Gradle module files are left
    as they are.
  - The umbrella serves JVM consumers: Gradle JVM projects, the JVM source sets of multiplatform
    projects, and plain Maven. Common code in a multiplatform project depends on
    `GeneralTools-core`; a JVM-only umbrella cannot resolve for `js` or `wasmJs`.
  - `GeneralTools-core`'s JVM variant and `GeneralTools-desktop` publish a dependency constraint:
    `io.github.spartanlaboratories:GeneralTools` requires 3.0.0, a string coordinate read from the
    project version, with `require` rather than `strictly`. A Gradle build that still pulls
    `GeneralTools:2.2.0`, through a published `gametools` 5.x, then selects the 3.0.0 umbrella and
    holds one copy of every class. Constraints do not reach Maven, whose consumers get a migration
    note with an `<exclusion>`. Nor do they make `gametools` 5.x work with 3.0.0: such a build fails
    at its first use of `Square` instead of silently mixing two copies of `Point`.
  - The JVM jars of `GeneralTools-core` and `GeneralTools-desktop` carry explicit
    `Automatic-Module-Name` entries. Because the two jars share the package
    `com.spartanlabs.generaltools`, they cannot be resolved together on the module path, whatever
    their names; on the class path, where 2.x consumers are, nothing changes. 2.2.0 had no module
    name.
  - Every published JVM variant, the umbrella's included, declares `org.gradle.jvm.version` 17.
  - The root project, which is not published, takes a name of its own, so that the umbrella is the
    only project named `GeneralTools` and a composite build's substitution of
    `io.github.spartanlaboratories:GeneralTools` is unambiguous. Each subproject carries its
    artifact's name.

  Evidence: [`published-shape-after-the-split.md`](published-shape-after-the-split.md).
- **Release.** One Gradle invocation of `publishAndReleaseToMavenCentral` uploads every module as
  one deployment: the plugin collects every module in one build service and zips them once. The set
  is therefore one release event. The plugin does not check that the set is complete, and a module
  the invocation does not reach is simply absent, so the release asserts the six components:
  `GeneralTools`, `GeneralTools-core`, `GeneralTools-core-jvm`, `GeneralTools-core-js`,
  `GeneralTools-core-wasm-js` and `GeneralTools-desktop`.
- **Rehearsal.** A file repository that the publishing convention defines receives the whole signed
  set from the exact tree (`publishAllPublicationsToRehearsalRepository`), and the consumer smoke
  matrix resolves from it, the rehearsal repository first and filtered to the group. The matrix
  includes a Gradle build that also pulls `GeneralTools:2.2.0` through `gametools` 5.1.0 and must
  end with one `Point` class, and a plain Maven build on `GeneralTools` 3.0.0. The rehearsal proves
  the metadata, the resolution and the completeness of the set, and the signing path. It does not
  prove the Portal's validation of the new artifact names, byte identity with the upload, or how
  the Portal treats one failing component in a deployment of several.
- **Rejected.**
  - A multiplatform umbrella: three more components, empty klibs, no prior art found, and no
    consumer that needs `GeneralTools` in common code.
  - A metadata-only umbrella: a Maven consumer that keeps its jar-type dependency would fail.
  - Capabilities for duplicate protection: core and the umbrella would collide on the ordinary path,
    and capabilities have no Maven form.
  - A multiplatform desktop module with a single target.

### Build structure: convention plugins and the version catalog

- **Recommended.**
  - An included `build-logic` build, through `pluginManagement { includeBuild("build-logic") }`,
    with precompiled script plugins for the entry points and plain Kotlin classes for logic:
    - a publishing convention: vanniktech, signing, the shared POM, Dokka with `failOnWarning`, and
      the rehearsal repository;
    - a multiplatform-library convention, for the core module;
    - a JVM-library convention, for the desktop module;
    - a test-set convention for `-PtestLevel`, applied by both library conventions.

    The umbrella applies `java-library` and the publishing convention only. The floor constant and
    the Dokka task name live in one Kotlin object.
  - `gradle/libs.versions.toml` holds every version: Kotlin 2.4.21, the language and stdlib floor
    2.2.0, Dokka 2.3.0, vanniktech 0.37.0, kotlin-logging 8.0.7, slf4j 2.0.16, logback 1.5.13 and
    commons-io 2.4. `build-logic` imports it with `from(files("../gradle/libs.versions.toml"))` and
    turns its plugin entries into its own dependencies with Gradle's documented helper, so each
    plugin version exists once. The conventions read entries through the catalog's string API
    behind a small helper; module scripts use the generated `libs` accessors.
  - The root build script applies both Kotlin plugins with `apply false` and Dokka, and aggregates
    the two modules' documentation. Without the Kotlin plugins on the root's classpath, Dokka
    renders empty pages with only a configuration-time warning, which `failOnWarning` does not
    catch, so a check counts the aggregated pages. `gradle.properties` sets Dokka's V2 mode
    explicitly (`V2Enabled`), the group and the version.
  - `abiValidation()` runs in both library conventions, with the dumps committed per module
    (`<module>/api/<module>.api`, and `.klib.api` for the core module); the umbrella has none.
    `kotlin-js-store/yarn.lock` stays at the repository root, where the Kotlin plugin keeps it.
  - Each restructuring step is proven by comparing the generated POM and Gradle module files of
    every publication, built at one path, before and after. Observed in a scratch build: the
    catalog and the conventions on the single module left all eight files byte-identical; the Dokka
    javadoc jars change only the javadoc jars; the split leaves the union of the JVM classes and of
    the JVM dumps as it was, and the klib dump differs only in the library's unique name.

  Evidence: [`build-logic-and-version-catalog.md`](build-logic-and-version-catalog.md).
- **Disagreement.**
  - The umbrella's plugin: a Kotlin/JVM module in the published-shape findings, `java-library` in
    the build-structure findings, where applying the Kotlin plugin to the empty module wrote an
    empty ABI dump and imposed explicit-API rules on it. `java-library` holds; the published files
    are the same either way.
  - The toolchain resolver: the build-structure findings keep the foojay resolver through a
    settings convention, while the Java-floor findings drop the toolchain altogether. With no
    toolchain requested, the resolver has nothing to provision, so it goes, and with it the only
    plugin a settings file would need from the catalog.
- **Rejected.** The `libs.javaClass` class-path workaround for typed accessors in conventions (it
  relies on an internal class); a third-party plugin for the same; `buildSrc` (the organization
  names a `build-logic` build); vanniktech's `configureBasedOnAppliedPlugins` (incubating and
  order-dependent).

### Java floor

- **Recommended.** One constant, 17, in `build-logic`.
  - Kotlin: `jvmTarget` 17 and `-Xjdk-release=17`, through `freeCompilerArgs`, since the Kotlin
    Gradle plugin 2.4.21 has no typed option for it. In the multiplatform module both sit inside
    `jvm { compilerOptions }`: on the `kotlin {}` extension the flag would also reach the JS
    compilation, which warns. In the Kotlin/JVM module they sit on the `kotlin {}` extension.
    `jvmTarget` fixes the class-file version; the flag hides every JDK API newer than 17, whatever
    JDK runs the build.
  - Java: `options.release` 17 on the Java compile tasks of the desktop and umbrella modules, from
    which Gradle derives `org.gradle.jvm.version` 17. The multiplatform module keeps setting that
    attribute on its `jvm` target by hand, because the Kotlin Gradle plugin never sets it.
  - No `jvmToolchain`: it would tie the floor to the toolchain. Gradle 9.0.0 itself needs JDK 17 to
    24, so the build JVM is never below the floor. CI on Temurin 17 runs the tests on the floor JDK,
    and a newer local JDK still produces 17 bytecode against the 17 API.
  - A `verifyJavaFloor` task, ported from WebTools, in `check` on all three modules. It reads the
    class-file versions of the module's jar and of its runtime classpath (`META-INF/versions/`
    excepted) and the `org.gradle.jvm.version` of every published JVM variant, and takes a switch
    for the umbrella, whose jar holds no class. Observed: kotlin-stdlib 2.2.0, kotlin-logging 8.0.7,
    slf4j-api 2.0.16 and commons-io 2.4 are all at most class-file major 61. kotlin-logging's
    `module-info.class` is exactly 61, so the floor cannot go below 17 unless that dependency
    changes.

  Evidence: [`java-floor-on-a-multiplatform-jvm-target.md`](java-floor-on-a-multiplatform-jvm-target.md).
- **Risks.** `-Xjdk-release` is an unstable `-X` flag, which Kotlin documents as not guaranteed to
  be effective for each JDK distribution (it needs the compile JDK's `ct.sym`); the guard is the
  backstop. The known issues with the flag concern the Java module system and Android, which this
  build does not use. Local JVM tests run on whichever JDK runs Gradle; CI is where they run on the
  floor.
- **Rejected.** `jvmToolchain(17)` as the floor (one setting for two things), and `jvmTarget` alone
  (the JDK API stays open).
