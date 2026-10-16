# Research: plan, verification and summary

- **For:** [SpartanLaboratories/GeneralTools#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7),
  *"Make GeneralTools Kotlin Multiplatform (Color and geometry usable from common code)"*. By the
  user's decision of 2026-10-12, the same change also resolves
  [SpartanLaboratories/GeneralTools#8](https://github.com/SpartanLaboratories/GeneralTools/issues/8),
  *"Read-only views for Point and Square so libraries can expose geometry without copying"*.
- **Release:** `3.0.0` (major), published to Maven Central by hand at the end of the run.
- **Written by:** the planner. The `<question-slug>.md` files beside this one belong to the
  researchers who wrote them and are never edited here.
- **Date:** 2026-10-12. Baseline: `master` @ `6926069`; `./gradlew build` passes with 185 tests
  and no failures.
- **Citations:** documents by section, code by symbol (outside repos at a pinned tag or commit).

This file has three parts: the research plan (written before any researcher started), the
verification results, and the summary that stage 3 designs from.

---

## Part 1: Research plan

### 1.1 Binding constraints

The user's interview answers (2026-10-12), one line each. Every researcher brief carries them.

| # | Constraint |
|---|---|
| B1 | Logging: common code logs through **kotlin-logging** (slf4j on the JVM, so consumers' backends are unchanged). **One central internal helper** logs the message at **WARN** and builds the `Result.failure`; every failure site uses it. Research must confirm kotlin-logging's current version, its `jvm`/`js`/`wasmJs` targets, and that MyGameTools can stay on Kotlin 2.2.0. If any of those fails, the planner returns to the user. |
| B2 | 3.0.0 = the multiplatform move **plus** these breaks: `Color`'s primary constructor becomes `internal`; `TwoDoubles.divideBy` returns `Result` (logging WARN through the helper, not ERROR); `TwoDoubles` is unsealed (stability tier per the user's rules, raised if not confident); `Point`/`Dimensions` become immutable; `evaluateList` takes `(T) -> Boolean` and moves to common; the whole `com.spartanlabs.logging` package is removed, with no deprecation cycle. |
| B3 | #8 is folded in: immutable `Point`/`Dimensions` plus whatever of #8 still applies (for example read-only interfaces). No issue is edited, re-parented, commented on or closed. Where #8's documents go is raised at the next halt. |
| B4 | Targets: `jvm`, `js` and `wasmJs`. If any existing test expectation cannot hold on a target without a behaviour change, the planner returns to the user before design. |
| B5 | JVM bytecode level 17. |
| B6 | Tests: every test in `com.spartanlabs.testing.<level>.<package>`; pure tests in `commonTest`, file/URL/AWT/slf4j tests in `jvmTest`; the stray test class `Point` folds into `PointTest`; the empty `MiscKtTest` is deleted; package-filtered level selection on every target's test task. |
| B7 | `src/test/resources/keys.txt` is deleted in this change. No issue is filed. |
| B8 | The `capitalizeEveryWord` comma bug is fixed in this change, with a test for the comma case. No separate issue. |
| B9 | The run ends with a manual Maven Central publish of `3.0.0` (`publishAndReleaseToMavenCentral` or equivalent, bare tag `3.0.0`, GitHub release). Coordinates `io.github.spartanlaboratories:GeneralTools` and package names stay; Maven-only consumers move to `GeneralTools-jvm`. |
| B10 | Re-derived assumptions the user did not object to: |
| | - The API dump changes by **exactly** the intended set of breaks, and the migration notes cover every break. |
| | - The MyGameTools pre-publish check is re-planned, because MyGameTools will no longer compile unchanged. |
| | - `read`, `asURL`, `org.slf4j.Logger.time` and `UserActions` stay JVM-only. |
| | - No Android or native targets. No CHANGELOG.md; migration notes go in the README and the GitHub release. |

### 1.2 Reuse first

- **This repo:** there are no earlier research files. The prior plans carry their research
  inline. That research is reused as context, not redone:
  - `docs/plans/3-geometry-primitives/plan.md` §2: the slab method and NaN handling, plus
    decisions D-mut and D-hash.
  - `docs/plans/1-color-methods/plan.md` §2: Color's rounding and the hex-format decisions.
- **SpartanGraphics working-tree research** (uncommitted, dated 2026-10-11, at
  `D:\Documents\Programming\SpartanGraphics\docs\roadmap\research\`):
  - [`web-target-kotlin-js-vs-wasm.md`](../../../../../SpartanGraphics/docs/roadmap/research/web-target-kotlin-js-vs-wasm.md)
    (N1) is reused for these points:
    - Kotlin/JS is Stable and Kotlin/Wasm is Beta.
    - The shared `webMain` source set exists since Kotlin 2.2.20.
    - kotlin-logging 8.0.4 publishes `js` and `wasmJs` variants.
    - The language-version compatibility rule for consumers.
    - The klib ABI position.

    The claims we rely on are re-verified in Part 2, because the file is uncommitted and in
    another repo.
  - `idiomatic-kotlin.md`, for its value-type and hot-path-math sections. Context for R6 and R7.
  - `consumer-needs-and-dependencies.md`: whether SpartanGraphics will depend on GeneralTools
    is still open there. Context for R8.
  - `android-build-and-platform-floors.md`: context only. The JVM level is settled by B5.

### 1.3 Decisions and questions

Each question informs one decision stage 3 must make that the interview did not settle.

| ID | Decision | Options | Criteria | Evidence that settles it | Research |
|---|---|---|---|---|---|
| D1 | Build configuration: Kotlin plugin version and compatibility pins, target and source-set layout, how JVM 17 is enforced, JS/Wasm test runtime, Gradle and CI | stay on Kotlin 2.2.0; move to current 2.4.x with `languageVersion`/`apiVersion`/`coreLibrariesVersion` pinned; `jvmToolchain(17)` versus `jvmTarget` + `-Xjdk-release` | B1 (MyGameTools stays on Kotlin 2.2.0), B4, B5; long-lived and low-maintenance | Kotlin compatibility guide and API guidelines; KGP docs and source; Gradle compatibility matrix | R1 [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md) |
| D2 | How 3.0.0 is published and checked before the irreversible release | vanniktech 0.36.0 versus newer; javadoc-jar mode; automatic release versus staged deployment tested before release; `publishToMavenLocal` smoke tests | B9; Central releases cannot be undone; the existing manual process | vanniktech docs, source and changelog; Sonatype Central Portal docs; KGP publishing docs | R2 [`kmp-maven-central-publishing.md`](kmp-maven-central-publishing.md) |
| D3 | Logging integration and the central failure helper's shape (B1's preconditions) | kotlin-logging release choice; `implementation` versus `api`; helper as a `KLogger` extension versus a top-level function; event builder versus plain messages | B1 | kotlin-logging releases, module metadata and POMs; its source and README; slf4j compatibility | R3 [`kotlin-logging-adoption.md`](kotlin-logging-adoption.md) |
| D4 | Whether every existing pure test holds on `js` and `wasmJs` (the B4 halt check), and the common replacements for JVM-only calls | per test class: holds / platform-specific expectation / behaviour change | B4, B8 ("unchanged behaviour" except the comma fix) | Kotlin stdlib JS and Wasm sources at pinned tags; official docs | R4 [`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md) |
| D5 | Test layout and level selection across `jvm`/`js`/`wasmJs`; test naming | per-level tasks per target; one property applied to all test tasks; keep or rename the 182 backtick names | B6; the user's 5-level standard; MyGameTools and SpartanGraphics precedent | KGP and Gradle docs and source (`KotlinJsTest`); Kotlin docs on identifiers; precedent repos | R5 [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md) |
| D6 | The 3.0.0 geometry model: immutable `Point`/`Dimensions`, unsealed `TwoDoubles`, `divideBy` returning `Result`, live mutable state, what of #8 still applies (`Square`, read-only views), the seam's stability tier | `var` reassignment versus a read-only/mutable pair; abstract base versus interface versus no base; `Square` mutable plus a read-only view versus immutable | B2, B3; the user's library-design rules; MyGameTools migration cost; hot-path performance | comparable libraries' code (OPENRNDR, Kool, KorGE/korma, Compose ui-geometry, JavaFX, libGDX, kotlin-math); JVM and JS allocation evidence | R6 [`immutable-geometry-model.md`](immutable-geometry-model.md) |
| D7 | Language mechanics for the other breaks, and the API-dump proof | `Color` as a data class with an internal constructor (copy visibility) versus a final class versus a value class; Result-returning naming; `evaluateList` signature; Misc facade naming; KGP `abiValidation()` versus the BCV plugin; `explicitApi()` | B2, B10; the user's clean-break rule | Kotlin docs, KEEPs and release notes; BCV and KGP docs | R7 [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) |
| D8 | Blast radius, adoption sweep, downstream migration size, and which pre-publish verification is meaningful | consumer smoke test only; uncommitted MyGameTools migration spike through a composite build; staged-deployment testing; pre-release version | B2, B3, B9, B10 | this repo, MyGameTools, MyGameClient, MyGameServer and WebTools code (read-only); git history | R8 [`downstream-blast-radius.md`](downstream-blast-radius.md) |

These decisions need no research:
- The JVM level (B5), and the target set and logging library choice (B3, B1).
- Tag and release conventions: a bare `X.Y.Z` tag and a GitHub release, per README and history.

### 1.4 Lenses

Each researcher works through three lenses inside its own question:
1. The best systems for the purpose.
2. Common practice and convention.
3. Idiomatic Kotlin, grounded in the repo's paradigm: OOP for domain modelling, FP for data
   transformation, and `Result` for expected failures.

R7 is the dedicated idiomatic-Kotlin question, because the public API's shape is itself a
decision.

### 1.5 Verification plan

- **Outside-world claims:** every findings file ends with its load-bearing claims. One
  `verifier` re-opens every source, together with the N1 claims reused in 1.2.
  - A claim that **fails** leaves the design inputs, and any decision resting on it reopens.
  - A claim that is **unclear** stays, with its weakness stated wherever it is used.
- **Claims about this codebase:** the planner spot-checks these against the code.

---

## Part 2: Verification results

*Pending.*

---

## Part 3: Summary

*Pending.*
