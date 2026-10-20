# Impact: GeneralTools 3.0.0 on Kotlin Multiplatform

- **Covers:**
  - [SpartanLaboratories/GeneralTools#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7)
  - [SpartanLaboratories/GeneralTools#8](https://github.com/SpartanLaboratories/GeneralTools/issues/8)

  Both are released together as `3.0.0`.
- **Design:** [`architecture.md`](architecture.md). The plans, in landing order:
  1. [`plan-kmp-build.md`](plan-kmp-build.md)
  2. [`plan-explicit-api.md`](plan-explicit-api.md)
  3. [`plan-logging.md`](plan-logging.md)
  4. [`plan-commonize.md`](plan-commonize.md)
  5. [`plan-two-doubles.md`](plan-two-doubles.md)
  6. [`plan-box-model.md`](plan-box-model.md)
  7. [`plan-generaltools-breaks.md`](plan-generaltools-breaks.md)
  8. [`plan-release.md`](plan-release.md)
- **Evidence:** [`research/README.md`](research/README.md). The downstream detail is in
  [`research/downstream-under-mutable-model.md`](research/downstream-under-mutable-model.md) and
  [`research/downstream-blast-radius.md`](research/downstream-blast-radius.md).

This file covers what 3.0.0 does to the code around it:
- what in this repository moves onto the new mechanisms;
- what breaks or changes for consumers;
- the work it leaves behind.

Once implementation ends, it is brought up to date with what was actually built.

---

## 1. Who is affected

| Consumer | Builds with | Reaches GeneralTools through | Effect of 3.0.0 |
|---|---|---|---|
| MyGameTools (`SpartanLabsGaming`) | Kotlin 2.2.0 | an `api` dependency on `2.2.0` | It does not compile until migrated: 39 lines in 14 files name `Square`, and `VisibleObject` assigns `area.dimensions`. Its 40 in-place `Point`/`Dimensions` mutations and every `Color(...)` call keep compiling. |
| MyGameClient | Kotlin 2.1.20 | `gametools` 5.0.0 | Unaffected until it moves to a MyGameTools release built on 3.0.0. After that, 27 lines in 8 files name `Square`. |
| MyGameServer | declared Kotlin 1.9.24 | `gametools` 5.0.0 | Nothing. |
| WebTools, SpartanGraphics | – | – | Nothing. |
| Any build that resolves a published `gametools` (5.0.0 or 5.1.0) beside GeneralTools 3.0.0 | – | – | Fails at runtime with `NoClassDefFoundError: com/spartanlabs/geometry/Square` (section 3.3). |
| Plain Maven consumers | – | – | Depend on `GeneralTools-jvm`; the root artifact holds no classes. |
| JS and Wasm consumers | Kotlin 2.4 or newer | – | New: the library is usable from `js` and `wasmJs`. |

A consumer that keeps its `2.x` dependency sees no change. The `3.0.0` artifacts reach only the builds
that ask for them.

---

## 2. Adoption in this repository

Every class or file that uses a mechanism 3.0.0 replaces gets a verdict. The full table is in
[`architecture.md`](architecture.md) §8.2.

| Replaced mechanism | Where | Verdict | Unit |
|---|---|---|---|
| slf4j loggers in code that becomes common | `TwoDoubles.kt`, `Point.kt`, `Segment.kt`, `Vectors.kt`, `Intersections.kt` | kotlin-logging, with the 2.x logger names | `logging` |
| slf4j logger in `Misc` | `Misc.kt` | The common part gains a kotlin-logging logger of the same name for `evaluateList`. `MiscJvm.kt` keeps slf4j for `asURL` and `read`. | `generaltools-breaks` |
| `Result.failure` built at each site | 11 sites in `Point.kt`, `Segment.kt`, `Vectors.kt`, `Intersections.kt` | The internal failure helper. Exception texts stay byte-identical, and the WARN line keeps the 2.x diagnostics. | `logging` |
| The throwing `divideBy` | `TwoDoubles.kt` | `Result<Unit>` through the same helper | `two-doubles` |
| `com.spartanlabs.logging` | `logging/Logger.kt`, `logging/MessageBuilder.kt` | Deleted; there is no user anywhere | `logging` |
| `javaClass` equality | `TwoDoubles.kt` | `this::class` with unchanged semantics, then the new equality contract | `commonize`, `two-doubles` |
| `String.format` | `Color.toHex` | `toString(16).padStart(2, '0').uppercase()` | `commonize` |
| `java.util.function.Predicate` | `evaluateList` | `(T) -> Boolean` | `generaltools-breaks` |
| `String.capitalize()` | `capitalizeEveryWord` | `replaceFirstChar` | `generaltools-breaks` |
| `Square` | `CenteredBox.kt` conversions, KDoc in three files, four test classes, the README | `Rectangle` | `box-model` |
| Allocating box reads | `segmentIntersectsBox`, `rayIntersectsBox`, `boxProblem`, `AxisAlignedBox.contains` | The primitive accessors | `box-model` |

Not adopted, by design:
- `CenteredBox`, `Segment` and `Ray` keep their mutable parts and their "treat as immutable" KDoc.
- `UserActions.kt` keeps slf4j and its `java.awt.Rectangle` import.
- The throwing members keep reporting by exception: `/=`, the arithmetic `div`, `Color.fromHex` and
  `trimIfLongerThan`.
- No library file imports `com.spartanlabs.geometry.arithmetic`.

---

## 3. Blast radius

### 3.1 This repository

- **Sources.** Every main source file is touched.
  - Each one moves to `src/jvmMain/kotlin`, then everything but `UserActions.kt` and `MiscJvm.kt` moves
    on to `src/commonMain/kotlin`.
  - Each one gains explicit-API modifiers.
  - New files: `Failure.kt`, `ComponentEquality.kt`, `ImmutableRectangle.kt`, `Arithmetic.kt`, the
    opt-in marker and `SupportedExtension`.
  - `Square.kt` becomes `Rectangle.kt`.
- **Tests.**
  - Every test class moves into `com.spartanlabs.testing.<level>.<package>`.
  - `MiscTest` splits into common and JVM classes, `MiscKtTest` is deleted, and the stray `Point` test
    class folds into `PointTest`.
  - Eight backtick names change: the seven that name `Square` or `toSquare`, and the inverted `Color`
    constructor test.
- **Build and CI.**
  - `build.gradle.kts` is rewritten for Kotlin Multiplatform, and the CI workflow moves to current
    actions.
  - New tracked files: `api/GeneralTools.api`, `api/GeneralTools.klib.api`,
    `kotlin-js-store/yarn.lock`, and a `.gitattributes` that keeps the dumps and the lock in LF.
  - `gradlew` becomes executable in git, so the CI `chmod` step goes.
  - CI time grows by the two web test runs and the ABI check.
- **README.** Every section changes, and a `3.0.0 migration notes` section is added.
- **Earlier plans.** `docs/plans/1-color-methods/plan.md` and `docs/plans/3-geometry-primitives/plan.md`
  each carry a dated pointer to this folder. Their bodies stay as they were.

### 3.2 Consumers: source

- **MyGameTools.** Its migration is about 17 files and 45 lines:
  - the rename in 14 files;
  - `VisibleObject`'s `area.dimensions = value`, which becomes `area.dimensions.setTo(value)`;
  - the dependency line;
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
    where 2.x threw its subtype `StringIndexOutOfBoundsException`. On JS it throws where 2.x returned
    `""`.
  - `divideBy` results can differ by one unit in the last place.
  - An expected failure logs one WARN line; the throwing `divideBy` logged ERROR.
  - kotlin-logging 8 prints one startup line per process unless
    `-Dkotlin-logging.logStartupMessage=false` is set.

### 3.3 Consumers: binary

- **Published `gametools`.** Every published `gametools` (5.0.0 and 5.1.0) references
  `com/spartanlabs/geometry/Square`.
  - Resolved beside GeneralTools 3.0.0, it fails with `NoClassDefFoundError` at the first `new Square`:
    on constructing an `Actor` or `Alive` (through `VisibleObject`'s constructor), a `TiledMap` or a
    `ZoneGrid`.
  - Every `Point`, `Dimensions`, `TwoDoubles` and `Color` reference in it keeps linking.
  - The release notes say that no published `gametools` runs beside 3.0.0.
- **Mixed builds.** The coordinates are unchanged, so a Gradle build that mixes the two resolves to
  3.0.0. It fails at runtime, not at dependency resolution.
- **Compiler-version changes outside the API dump.** Compiled by Kotlin 2.4.21, the class-side
  `contains(Point)` of `Rectangle` (2.x `Square`) and of `CenteredBox` carries `ACC_BRIDGE`, which
  2.x did not set, and the jar's module file is
  `META-INF/io.github.spartanlaboratories_GeneralTools.kotlin_module` (2.x:
  `META-INF/GeneralTools.kotlin_module`). Callers link by name and descriptor, so no call site
  changes; reflection sees `Method.isBridge()` return `true` for those two methods.

### 3.4 Platform floors

- **JVM.** Bytecode 17. A Kotlin 2.2.0 consumer compiles against `GeneralTools-jvm`, and a Kotlin
  2.1.20 compiler reads its metadata.
- **JS and Wasm.** Kotlin 2.4 or newer, because a klib carries the ABI version of the compiler that
  built it. Kotlin/Wasm is Beta, and klib binary-compatibility guarantees are not final, so the web
  variants carry a weaker practical promise than the JVM variant.

---

## 4. Follow-ups

### 4.1 In this repository

- [SpartanLaboratories/GeneralTools#11](https://github.com/SpartanLaboratories/GeneralTools/issues/11):
  make `EPSILON` a parameter of the geometry functions that use it. Additive overloads default to
  `EPSILON`, in a minor release after 3.0.0.
- [SpartanLaboratories/GeneralTools#12](https://github.com/SpartanLaboratories/GeneralTools/issues/12):
  a Gradle TestKit compile-fail test (level 3) for `TwoDoubles`' subclassing opt-in, in a minor
  release after 3.0.0. The same harness can hold the arithmetic package's `+=` ambiguity, which only
  the release smoke project checks today.
- [SpartanLaboratories/GeneralTools#13](https://github.com/SpartanLaboratories/GeneralTools/issues/13):
  `toImmutable()` for every `AxisAlignedBox`, not only `Rectangle`, so a consumer can hand out a
  `CenteredBox` without converting it first.
- [SpartanLaboratories/GeneralTools#14](https://github.com/SpartanLaboratories/GeneralTools/issues/14):
  a `screenshotArea` overload in `UserActions` that takes an `AxisAlignedBox`.
- [SpartanLaboratories/GeneralTools#15](https://github.com/SpartanLaboratories/GeneralTools/issues/15):
  immutable counterparts for `Point` and `Segment`, the part of #8 that 3.0.0 leaves to a later release.
- [SpartanLaboratories/GeneralTools#16](https://github.com/SpartanLaboratories/GeneralTools/issues/16):
  a `Color` factory that returns a `Result`, beside the validating constructor.
- [SpartanLaboratories/GeneralTools#17](https://github.com/SpartanLaboratories/GeneralTools/issues/17):
  a root artifact usable from plain Maven, through a root-POM dependency on `GeneralTools-jvm`. It
  needs no coordinate change.
- [SpartanLaboratories/GeneralTools#18](https://github.com/SpartanLaboratories/GeneralTools/issues/18):
  Node and Yarn caching in CI, which downloads both on every run.
- **The `TwoDoubles` subclassing seam** graduates from Experimental to Supported Extension
  (`@SupportedExtension`) once a consumer builds on it. That release's notes record the promotion.
- **Keeping the build current:**
  - A Kotlin Gradle plugin bump re-reads the Experimental `abiValidation()` DSL and runs
    `./gradlew kotlinUpgradeYarnLock`.
  - The language and API version 2.2 rises only together with the consumers' own Kotlin. Kotlin 2.5
    deprecates 2.2 as a language version.

The reconciled version of this file, brought up to date with what was built and published, lands in the
as-built docs PR that follows the `3.0.0` tag.

### 4.2 In other repositories (notes only; nothing is filed or changed from here)

- **SpartanLabsGaming/MyGameTools.** After 3.0.0 is published, it migrates and releases a major
  (section 3.2). Its `bounds` properties can move to the new types at the same time:
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
- **SpartanLaboratories/SpartanGraphics.** If it adopts GeneralTools beside its own planned
  `Rectangle`, a file that needs both aliases one import.
- **Credential in history.** `src/test/resources/keys.txt` held a credential-like value in this public
  repository. `master` deletes the file (PR #10), but the value remains in the history, so rotating
  the credential is the remedy. The 3.0.0 work never moves, modifies or quotes the file.
