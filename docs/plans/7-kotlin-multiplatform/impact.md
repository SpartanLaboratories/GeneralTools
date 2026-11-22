# Impact: GeneralTools 3.0.0 on Kotlin Multiplatform

- **Covers:**
  - [SpartanLaboratories/GeneralTools#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7)
  - [SpartanLaboratories/GeneralTools#8](https://github.com/SpartanLaboratories/GeneralTools/issues/8)
  - the issues of the remaining units:
    [#26](https://github.com/SpartanLaboratories/GeneralTools/issues/26),
    [#20](https://github.com/SpartanLaboratories/GeneralTools/issues/20),
    [#24](https://github.com/SpartanLaboratories/GeneralTools/issues/24),
    [#25](https://github.com/SpartanLaboratories/GeneralTools/issues/25),
    [#23](https://github.com/SpartanLaboratories/GeneralTools/issues/23) and
    [#21](https://github.com/SpartanLaboratories/GeneralTools/issues/21), with
    [#17](https://github.com/SpartanLaboratories/GeneralTools/issues/17) folded in, plus the review of
    the built units and the release, each a sub-issue of #7.

  All of them are released together as `3.0.0`.
- **Design:** [`architecture.md`](architecture.md). The seven built units' plans, in landing order:
  1. [`plan-kmp-build.md`](plan-kmp-build.md)
  2. [`plan-explicit-api.md`](plan-explicit-api.md)
  3. [`plan-logging.md`](plan-logging.md)
  4. [`plan-commonize.md`](plan-commonize.md)
  5. [`plan-two-doubles.md`](plan-two-doubles.md)
  6. [`plan-box-model.md`](plan-box-model.md)
  7. [`plan-generaltools-breaks.md`](plan-generaltools-breaks.md)

  What was built against them is in [`final-implementation.md`](final-implementation.md). The eight
  remaining units (`architecture.md` §11.2) are each planned in a folder of their own beside these
  files. The earlier release plan is kept as
  [`superseded/plan-release-2026-11-22.md`](superseded/plan-release-2026-11-22.md).
- **Evidence:** [`research/README.md`](research/README.md). The downstream detail is in
  [`research/downstream-under-mutable-model.md`](research/downstream-under-mutable-model.md) and
  [`research/downstream-blast-radius.md`](research/downstream-blast-radius.md); the published shape
  after the module split in
  [`research/published-shape-after-the-split.md`](research/published-shape-after-the-split.md).

This file covers what 3.0.0 does to the code around it:
- what in this repository moves onto the new mechanisms;
- what breaks or changes for consumers;
- the work it leaves behind.

The `release` unit brings it up to date with what was actually built and published, at the release
landing.

---

## 1. Who is affected

| Consumer | Builds with | Reaches GeneralTools through | Effect of 3.0.0 |
|---|---|---|---|
| MyGameTools (`SpartanLabsGaming`) | Kotlin 2.2.0 | an `api` dependency on `GeneralTools` `2.2.0` | It keeps its coordinate: `GeneralTools:3.0.0` resolves to the umbrella, and through it to the core JVM jar and the desktop jar. It does not compile until migrated: 39 lines in 14 files name `Square`, and `VisibleObject` assigns `area.dimensions`. Its 40 in-place `Point`/`Dimensions` mutations and every `Color(...)` call keep compiling. |
| MyGameClient | Kotlin 2.1.20 | `gametools` 5.0.0 | Unaffected until it moves to a MyGameTools release built on 3.0.0. After that, 27 lines in 8 files name `Square`. |
| MyGameServer | declared Kotlin 1.9.24 | `gametools` 5.0.0 | Nothing. |
| WebTools, SpartanGraphics | – | – | Nothing. |
| Any build that resolves a published `gametools` (5.0.0 or 5.1.0) beside a GeneralTools 3.0.0 artifact | – | – | In Gradle, and in Maven when `GeneralTools` 3.0.0 itself is declared, one copy of the classes and a `NoClassDefFoundError: com/spartanlabs/geometry/Square` at run time (section 3.3). In Maven, with only `GeneralTools-core` or `GeneralTools-desktop` declared, two copies of every class until `GeneralTools` 2.2.0 is excluded. |
| Plain Maven consumers | – | `GeneralTools` | They keep their declaration and get real classes: the umbrella depends on the core and desktop modules, and the core's root POM points at `GeneralTools-core-jvm`. |
| Consumers on JDK 17 to 22 | – | – | New: they can resolve and run 3.0.0. The 2.2.0 artifacts needed JDK 23. |
| JS and Wasm consumers | Kotlin 2.4 or newer | `GeneralTools-core` | New: the library is usable from `js` and `wasmJs`. The `GeneralTools` umbrella is JVM-only, so common code declares `GeneralTools-core`. |
| Consumers on the Java module path | – | – | New limit: the core JVM jar and the desktop jar share a package, so they cannot be resolved together there. Each works alone, under its `Automatic-Module-Name`. No such consumer is known. |

A consumer that keeps its `2.x` dependency sees no change. The `3.0.0` artifacts reach only the builds
that ask for them, or a Gradle build that also asks for a new artifact, whose published constraint
raises `GeneralTools` to 3.0.0.

---

## 2. Adoption in this repository

Every class or file that uses a mechanism 3.0.0 replaces gets a verdict. The full tables are in
[`architecture.md`](architecture.md) §8.2.

| Replaced mechanism | Where | Verdict | Unit |
|---|---|---|---|
| slf4j loggers in code that became common | `TwoDoubles.kt`, `Point.kt`, `Segment.kt`, `Vectors.kt`, `Intersections.kt` | kotlin-logging, with the 2.x logger names | `logging` (built) |
| slf4j logger in `Misc` | `Misc.kt` | The common part logs through kotlin-logging, under the same name, for `evaluateList`. | `generaltools-breaks` (built) |
| slf4j loggers left in the JVM sources | `MiscJvm.kt`, `UserActions.kt` | kotlin-logging loggers with the same names, levels and message texts | `jvm-logging` (#20) |
| kotlin-logging 8.0.4 | the build | 8.0.7, the one version, still `implementation` only | `kotlin-logging-version` (#26) |
| `Result.failure` built at each site | 11 sites in `Point.kt`, `Segment.kt`, `Vectors.kt`, `Intersections.kt` | The internal failure helper. Exception texts stay byte-identical, and the WARN line keeps the 2.x diagnostics. | `logging` (built) |
| The throwing `divideBy` | `TwoDoubles.kt` | `Result<Unit>` through the same helper | `two-doubles` (built) |
| `com.spartanlabs.logging` | `logging/Logger.kt`, `logging/MessageBuilder.kt` | Deleted; there is no user anywhere | `logging` (built) |
| `javaClass` equality | `TwoDoubles.kt` | `this::class` with unchanged semantics, then the new equality contract | `commonize`, `two-doubles` (built) |
| `String.format` | `Color.toHex` | `toString(16).padStart(2, '0').uppercase()` | `commonize` (built) |
| `java.util.function.Predicate` | `evaluateList` | `(T) -> Boolean` | `generaltools-breaks` (built) |
| `String.capitalize()` | `capitalizeEveryWord` | `replaceFirstChar` | `generaltools-breaks` (built) |
| `Square` | `CenteredBox.kt` conversions, KDoc in three files, four test classes, the README | `Rectangle` | `box-model` (built) |
| Allocating box reads | `segmentIntersectsBox`, `rayIntersectsBox`, `boxProblem`, `AxisAlignedBox.contains` | The primitive accessors | `box-model` (built) |
| Version literals in the build script | `build.gradle.kts` | `gradle/libs.versions.toml` | `version-catalog` (#24) |
| The one build script | `build.gradle.kts` | Convention plugins in an included `build-logic` build; each module's script holds only what is its own | `convention-plugins` (#25) |
| The empty javadoc jars | the publication | Dokka output, generated with `failOnWarning` | `convention-plugins` (#25) |
| The floor tied to the toolchain | `jvmToolchain(jvmFloor)` | One floor constant driving `jvmTarget` and `-Xjdk-release`; no toolchain; `verifyJavaFloor` in `check` | `java-floor` (#23) |
| The single-module shape | the build, `src/`, `api/`, the coordinates | Three modules: `GeneralTools-core`, `GeneralTools-desktop` and the `GeneralTools` umbrella | `module-split` (#21, with #17) |

Not adopted, by design:
- `CenteredBox`, `Segment` and `Ray` keep their mutable parts and their "treat as immutable" KDoc.
- `Logger.time` keeps its slf4j receiver: it is the public JVM contract, and a twin on
  kotlin-logging's logger would put an internal dependency in the public API.
- The log-capture tests keep reading events at the logback backend, where a consumer's rules apply.
- `UserActions.kt` keeps its `java.awt.Rectangle` import.
- The throwing members keep reporting by exception: `/=`, the arithmetic `div`, `Color.fromHex` and
  `trimIfLongerThan`.
- No library file imports `com.spartanlabs.geometry.arithmetic`.

---

## 3. Blast radius

### 3.1 This repository

- **Sources.** Every main source file is touched.
  - The built units moved each one to `src/jvmMain/kotlin`, then everything but `UserActions.kt` and
    `MiscJvm.kt` on to `src/commonMain/kotlin`, gave each explicit-API modifiers, and added
    `Failure.kt`, `ComponentEquality.kt`, `ImmutableRectangle.kt`, `Arithmetic.kt`, the opt-in
    marker and `SupportedExtension`; `Square.kt` became `Rectangle.kt`.
  - `module-split` moves the common and JVM sources, unchanged, into the core module's folder, and
    `UserActions.kt` into the desktop module's.
  - `jvm-logging` changes the loggers of `MiscJvm.kt` and `UserActions.kt`.
- **Tests.**
  - Every test class sits in `com.spartanlabs.testing.<set>.<package>`.
  - `MiscTest` was split into common and JVM classes, `MiscKtTest` deleted, and the stray `Point`
    test class folded into `PointTest`. Eight backtick names changed: the seven that named `Square`
    or `toSquare`, and the inverted `Color` constructor test.
  - `jvm-logging` adds log-capture tests for `asURL`, `read` and the `UserActions` functions that run
    headless. `module-split` moves the desktop tests into the desktop module, with their own
    `logback-test.xml` and capture helper.
  - `built-units-review` audits the whole suite against the named test sets.
- **Build and CI.**
  - New: `gradle/libs.versions.toml`; the `build-logic/` build with its convention plugins;
    `gradle.properties` entries for the group, the version and Dokka's mode; a root aggregator
    script; one script per module.
  - `settings.gradle.kts` includes `build-logic` and the three modules, gives the root project its
    own name (`GeneralTools-build`) and loses the toolchain resolver.
  - The dumps move into module folders: `GeneralTools-core/api/GeneralTools-core.api`,
    `GeneralTools-core/api/GeneralTools-core.klib.api` and
    `GeneralTools-desktop/api/GeneralTools-desktop.api`. `.gitattributes` keeps them at LF, and its
    comment says what `checkKotlinAbi` compares. `kotlin-js-store/yarn.lock` stays at the root.
  - `check` gains `verifyJavaFloor` on every module and the umbrella's count checks. Until the
    API-documentation work adds a CI job, each remaining unit runs the root Dokka task among its own
    checks; it is not part of `check`.
  - CI is unchanged: it builds on Temurin 17, the floor JDK. Its time grows by the two web test runs,
    the ABI checks and the floor guards.
- **README.** Every section has changed or changes: the coordinates (six artifacts; `GeneralTools`
  for JVM builds in Gradle and Maven, `GeneralTools-core` for common code and the web), Modules,
  Build & test, the Java requirement, Logging, Releasing (the standing automatic release after a
  rehearsal), and the `3.0.0 migration notes`.
- **CHANGELOG.** `CHANGELOG.md` is new, in the Keep a Changelog layout. Each remaining landing adds
  its entry under `[Unreleased]`, the review adds the built units' entries, and the release turns the
  section into `[3.0.0] - <date>`, linking the README's migration notes.
- **Earlier plans.** `docs/plans/1-color-methods/plan.md` and `docs/plans/3-geometry-primitives/plan.md`
  each carry a dated pointer to this folder. Their bodies stay as they were.

### 3.2 Consumers: source

- **MyGameTools.** Its migration is about 17 files and 45 lines:
  - the rename in 14 files;
  - `VisibleObject`'s `area.dimensions = value`, which becomes `area.dimensions.setTo(value)`;
  - the dependency line, whose coordinate stays `GeneralTools`;
  - two README diagram lines;
  - a CHANGELOG entry.

  `Square` is in MyGameTools' public API (`VisibleObject.area`, `Space.bounds`, `TiledMap.bounds`,
  `Zone.bounds`), so the migration is a major release for its own consumers. No MyGameTools `var`
  holder uses a compound operator, so it can import the arithmetic package without that package's
  ambiguity cost.
- **MyGameClient.** After MyGameTools releases on 3.0.0, MyGameClient's own 27 lines in 8 files
  follow the rename.
- **Silent behaviour changes.** The migration notes state each one:
  - `VisibleObject.dimensions = d`, migrated to `setTo`, copies the values rather than adopting the
    instance.
  - `Actor.isAtDestination` changes only for NaN positions.
  - `Rectangle.toString()` prints `Rectangle(...)`.
  - A `Color` built from out-of-range values now throws. MyGameClient's `Portrait` builds one from
    network values, which belong with `Color.of`.
  - `Color.fromHex` given a sign character now throws.
  - `trimIfLongerThan` with a negative length throws a plain `IndexOutOfBoundsException` on the JVM,
    where 2.x threw its subtype `StringIndexOutOfBoundsException`.
  - `divideBy` results can differ by one unit in the last place.
  - An expected failure logs one WARN line; the throwing `divideBy` logged ERROR.
  - kotlin-logging 8 prints one startup line per process unless
    `-Dkotlin-logging.logStartupMessage=false` is set.

### 3.3 Consumers: binary

- **Published `gametools`.** Every published `gametools` (5.0.0 and 5.1.0) references
  `com/spartanlabs/geometry/Square`.
  - Resolved beside GeneralTools 3.0.0, it fails with `NoClassDefFoundError` at the first `new Square`:
    on constructing an `Actor` or `Alive` (through `VisibleObject`'s constructor), a `TiledMap` or a
    `ZoneGrid`. The error is derived from the bytecode references; the release rehearsal loads
    `gametools-core` 5.1.0 once so that the notes quote the real one.
  - Every `Point`, `Dimensions`, `TwoDoubles` and `Color` reference in it keeps linking.
  - The release notes say that no published `gametools` runs beside 3.0.0.
- **Mixed builds.**
  - In Gradle, a build that declares `GeneralTools` 3.0.0 resolves it over 2.2.0. A build that
    declares only `GeneralTools-core` or `GeneralTools-desktop` 3.0.0 beside a transitive
    `GeneralTools:2.2.0` meets the constraint both publish, which raises `GeneralTools` to the
    3.0.0 umbrella. Either way the build holds one copy of every class and fails at run time, not
    at resolution.
  - In Maven, a build that declares `GeneralTools` 3.0.0 gets one version of it, with the same
    outcome. A build that declares only a new artifact beside `GeneralTools:2.2.0` holds both jars,
    with two copies of every class, until it excludes `GeneralTools`. The migration notes show the
    `<exclusion>`.
- **Where a class ships.** `MiscKt` (with `read`, `asURL` and `Logger.time`) is in
  `GeneralTools-core-jvm`; `UserActionsKt` is in `GeneralTools-desktop`; both stay in the package
  `com.spartanlabs.generaltools`, so every import and linked call keeps working on the class path.
  Each jar carries its own `.kotlin_module` file.
- **Compiler-version changes outside the API dump.** Compiled by Kotlin 2.4.21, the class-side
  `contains(Point)` of `Rectangle` (2.x `Square`) and of `CenteredBox` carries `ACC_BRIDGE`, which
  2.x did not set, and the jars' Kotlin module files are named after their modules (2.x:
  `META-INF/GeneralTools.kotlin_module`). Callers link by name and descriptor, so no call site
  changes; reflection sees `Method.isBridge()` return `true` for those two methods.

### 3.4 Platform floors

- **JVM.** Bytecode 17, against the Java 17 API, on every JVM artifact, and `org.gradle.jvm.version`
  17 on every published JVM variant. The 2.2.0 artifacts were bytecode for Java 23 and declared 23,
  so 3.0.0 lowers the JDK a consumer needs. A Kotlin 2.2.0 consumer compiles against the JVM
  artifacts, and a Kotlin 2.1.20 compiler reads their metadata; every JVM artifact requires
  `kotlin-stdlib` 2.2.0.
- **JS and Wasm.** Kotlin 2.4 or newer, because a klib carries the ABI version of the compiler that
  built it. Kotlin/Wasm is Beta, and klib binary-compatibility guarantees are not final, so the web
  artifacts carry a weaker practical promise than the JVM ones. The `js` variant also brings
  `org.jetbrains.kotlin:kotlin-dom-api-compat:2.4.21`, which the Kotlin plugin adds to every Kotlin/JS
  compilation by default; a Kotlin/JS consumer's own plugin adds the same artifact, so the consumer
  resolves one version of it.

---

## 4. Follow-ups

### 4.1 In this repository

- [SpartanLaboratories/GeneralTools#11](https://github.com/SpartanLaboratories/GeneralTools/issues/11):
  make `EPSILON` a parameter of the geometry functions that use it. Additive overloads default to
  `EPSILON`, in a minor release after 3.0.0.
- [SpartanLaboratories/GeneralTools#12](https://github.com/SpartanLaboratories/GeneralTools/issues/12):
  a Gradle TestKit compile-fail test, in the integration set, for `TwoDoubles`' subclassing opt-in,
  in a minor release after 3.0.0. The same harness can hold the arithmetic package's `+=` ambiguity,
  which only the release smoke project checks today.
- [SpartanLaboratories/GeneralTools#13](https://github.com/SpartanLaboratories/GeneralTools/issues/13):
  `toImmutable()` for every `AxisAlignedBox`, not only `Rectangle`, so a consumer can hand out a
  `CenteredBox` without converting it first.
- [SpartanLaboratories/GeneralTools#14](https://github.com/SpartanLaboratories/GeneralTools/issues/14):
  a `screenshotArea` overload in `UserActions` that takes an `AxisAlignedBox`. It belongs to
  `GeneralTools-desktop`, whose API dependency on the core already brings that type.
- [SpartanLaboratories/GeneralTools#15](https://github.com/SpartanLaboratories/GeneralTools/issues/15):
  immutable counterparts for `Point` and `Segment`, the part of #8 that 3.0.0 leaves to a later release.
- [SpartanLaboratories/GeneralTools#16](https://github.com/SpartanLaboratories/GeneralTools/issues/16):
  a `Color` factory that returns a `Result`, beside the validating constructor.
- [SpartanLaboratories/GeneralTools#18](https://github.com/SpartanLaboratories/GeneralTools/issues/18):
  Node and Yarn caching in CI, which downloads both on every run.
- [SpartanLaboratories/GeneralTools#19](https://github.com/SpartanLaboratories/GeneralTools/issues/19),
  after 3.0.0 on `master`: the rest of the API-documentation standard, namely the Dokka job in CI,
  the project site, the Pages workflow and the branding. 3.0.0 ships the Dokka javadoc jars only.
- [SpartanLaboratories/GeneralTools#22](https://github.com/SpartanLaboratories/GeneralTools/issues/22),
  after 3.0.0 on `master`: the existing Kotlin files' imports regrouped, as one change with no other
  edit. The files the remaining units add follow the grouping from the start.
- **Device-family modules.** `-android` and `-web` modules join beside `GeneralTools-desktop` when
  those families exist, each as a new artifact that depends on the core in API scope. A web or
  Android family does not join the JVM-only umbrella.
- **The `TwoDoubles` subclassing seam** graduates from Experimental to Supported Extension
  (`@SupportedExtension`) once a consumer builds on it. That release's notes record the promotion.
- **Keeping the build current:**
  - A Kotlin Gradle plugin bump re-reads the Experimental `abiValidation()` DSL and runs
    `./gradlew kotlinUpgradeYarnLock`. Each version moves in one catalog entry.
  - The language and API version 2.2 rises only together with the consumers' own Kotlin. Kotlin 2.5
    deprecates 2.2 as a language version.
  - `-Xjdk-release` is an unstable compiler flag; if a later Kotlin drops or renames it, the floor
    guard still holds the class-file version and the published attribute.

### 4.2 In other repositories (notes only; nothing is filed or changed from here)

- **SpartanLabsGaming/MyGameTools.** After 3.0.0 is published, it migrates and releases a major
  (section 3.2), keeping the coordinate `GeneralTools`. Its `bounds` properties can move to the new
  types at the same time:
  - `Zone.bounds` and `TiledMap.bounds` to `ImmutableRectangle`;
  - `Space.bounds` to `AxisAlignedBox`;
  - `ZoneGrid`'s origin taken from one `space.bounds.toImmutable()` at construction.

  See [`research/downstream-under-mutable-model.md`](research/downstream-under-mutable-model.md) §3.4.
- **SpartanLabsGaming/MyGameClient.** It follows MyGameTools' release.
  - Its `Portrait` builds the subject's `Color` with `Color.of`, so a bad network value is clamped
    instead of throwing in the frame loop.
  - Its own `Color.lightened` extension has been shadowed by the GeneralTools member since 2.1.0, and
    can go.
- **`gametools` 5.1.0 on Maven Central** was not built from the git tag `v5.1.0` (`8938004`). The tag
  has no `gametools-world` module and pins GeneralTools `2.0.1`; the published artifacts require
  `2.2.0`.
- **SpartanLabsGaming/MyGameServer** declares `kotlin("jvm") 1.9.24`, while its build output comes
  from a Kotlin 2.2.x compiler.
- **SpartanLaboratories/SpartanGraphics.** If it adopts GeneralTools, it declares `GeneralTools-core`
  in its common code; beside its own planned `Rectangle`, a file that needs both aliases one import.
- **SpartanLaboratories/WebTools.** `verifyJavaFloor` is ported from its `build-logic`, with a switch
  added for a module whose jar is empty by design. The two copies can drift; WebTools needs the
  switch only if it gains a source-less module.
- **Credential in history.** `src/test/resources/keys.txt` held a credential-like value in this public
  repository. `master` deletes the file (PR #10), but the value remains in the history, so rotating
  the credential is the remedy. The 3.0.0 work never moves, modifies or quotes the file.
