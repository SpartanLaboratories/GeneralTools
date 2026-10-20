# Plan: `explicit-api` - strict explicit API mode, no API change

- **Covers:** SpartanLaboratories/GeneralTools#7, "Make GeneralTools Kotlin Multiplatform (Color and
  geometry usable from common code)". The same change set resolves
  SpartanLaboratories/GeneralTools#8, "Read-only views for Point and Square so libraries can expose
  geometry without copying"; that issue is planned by the `box-model` unit and nothing in this unit
  touches it.
- **Architecture:** [`architecture.md`](architecture.md), unit `explicit-api` (section 11.2, row 2; system
  S2, "API proof harness").
- **Landing order:** unit 2 of 8. Depends on `kmp-build`. `logging` and `commonize` follow.
- **State this unit starts from:** the tree after `kmp-build`. Every source file sits unchanged under
  `src/jvmMain/kotlin/<same subdirectory>/` and every test file unchanged under
  `src/jvmTest/kotlin/<same subdirectory>/`. The multiplatform plugin is applied, `jvm()`, `js` and
  `wasmJs` are declared (the web targets compile empty), `api/GeneralTools.api` holds the tag `2.2.0`
  baseline, and `check` runs `checkKotlinAbi`. Line numbers below are the line numbers of the files
  today in `src/main/kotlin`; the move changes no content, so they hold in `src/jvmMain/kotlin`.
- **Branch:** `feat/issue-7-kotlin-multiplatform` (shared by all units).
- **Commit:** TBD. **PR:** TBD. This document is committed in the same commit as the implementation it
  describes.
- **What this plans:** turn on `explicitApi()` and make the 118 declarations that the compiler then
  rejects (or that sit beside them) carry `public` and, where the type was inferred, an explicit type
  equal to the inferred one. No API change; the JVM dump stays byte-identical.
- **Status:** planning only.

---

## 1. Requirements

| # | Requirement | Acceptance check |
|---|---|---|
| R1 | `explicitApi()` is set on the `kotlin {}` extension in `build.gradle.kts`. | `grep -n "explicitApi()" build.gradle.kts` shows one hit inside the `kotlin { }` block. |
| R2 | Every declaration in `src/jvmMain/kotlin` that is public API states `public`, except those the compiler exempts (primary constructors, constructor properties of data classes, overrides, accessors, enum entries, value parameters, local declarations). | `./gradlew compileKotlinJvm` reports no `visibility must be specified in explicit API mode` and no `return type must be specified in explicit API mode`. |
| R3 | Every public function with an expression body and every public property without a declared type gets an explicit type, and that type is the one the compiler inferred. | The compiled `jvmMain` class files are byte-identical before and after (section 9, Level 1 step 2). |
| R4 | No API change. | `git diff --stat HEAD~1 HEAD -- api/` is empty and `./gradlew checkKotlinAbi` passes without `updateKotlinAbi` being run. |
| R5 | No behaviour change. | The 185 existing `jvmTest` tests pass unchanged. |
| R6 | From this commit on, a public declaration without `public` or without an explicit type fails the build. | Removing one `public` from a throwaway edit makes `compileKotlinJvm` fail with the diagnostic above (done locally, not committed). |
| R7 | The README states the rule for contributors. | Section 8. |
| R8 | The change is one commit, with this plan in it. | Section 11. |

## 2. Current state

**Baseline.** `master` @ `17d30bc` is identical to tag `2.2.0` in `src/main`; `./gradlew build`
passes with 185 tests. `master` @ `c196d1a` (which only deletes `src/test/resources/keys.txt`) is merged
into the branch before the first implementation commit. After `kmp-build` the sources and tests have
moved to `jvmMain` / `jvmTest` without content changes. No file under `src/` mentions `explicitApi`;
`build.gradle.kts` does not set it.

**What the compiler requires.** Verified from the Kotlin compiler source at tag `v2.4.21`,
[`FirExplicitApiDeclarationChecker.kt`](https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/fir/checkers/src/org/jetbrains/kotlin/fir/analysis/checkers/syntax/FirExplicitApiDeclarationChecker.kt)
(lines 97-130 and 148-159):

- Only declarations whose effective visibility is public API are checked (`:59`), and not those inside
  a non-public container (`:61-68`). Local declarations, `private` and `internal` members therefore need
  nothing.
- A **visibility modifier** is required unless the declaration is: a primary constructor; a property
  accessor; a value parameter; an anonymous function; an enum entry; an `override`; a local declaration;
  or a **property of a data class, value class or annotation class** (`:109-130`). The data-class rule
  covers constructor properties and also properties declared in the class body.
- A **return type** is required for every property with an inferred type, and for every function whose
  body is a single expression with an inferred type (`:148-159`). Block-body functions are exempt
  because an omitted type there means `Unit`. Overrides are not exempt from this rule, but none here has
  an inferred type.
- A visibility modifier is not exempted for interface members, companion objects, nested classes or
  `main`; each needs `public`.

**Test sources are out of scope for explicit API mode.** In the Kotlin Gradle plugin, the compiler flag is
only configured for main compilations and for the common metadata compilation: `AbstractKotlinCompileConfig.kt:126-143`
at `v2.4.21`
([source](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/tasks/configuration/AbstractKotlinCompileConfig.kt)),
whose comment reads "Plugin explicitly does not configure 'explicitApi' mode for test sources
compilation, as test sources are not published". `jvmTest` (and later `commonTest`) compile without it.
No test file changes in this unit. `jsMain` and `wasmJsMain` are main compilations, so the mode applies to
them once they hold code; today they are empty.

**Planning-time verification** (a standalone Kotlin 2.2.0 compiler run, language and API version 2.2, in
a scratch directory outside the repository; no repository file was changed):

1. Compiling today's `src/main/kotlin` with `-Xexplicit-api=strict` yields 155 diagnostics: 115
   `visibility must be specified` and 40 `return type must be specified`. Every return-type diagnostic
   sits on a declaration that also lacks `public`, so 115 declarations are rejected.
2. A copy edited exactly as section 7 prescribes compiles with `-Xexplicit-api=strict` with no
   diagnostic, and **all 29 produced class files are byte-identical** to the ones compiled without the
   mode, Kotlin metadata included. The three `Segment` body properties of section 7.8 are the only
   edits not demanded by a diagnostic (118 = 115 + 3).
3. The same copy with `Result<R>` instead of `Result<T>` on `onException` produces a different
   `MiscKt.class` (section 7.2), and `public constructor` on the `TwoDoubles` secondary constructor is a
   compile error: `constructor must be private or protected in sealed class`.

**Where the type becomes visible in the dump.** The JVM dump records erased descriptors, so an explicit
type cannot change it. What the dump cannot see is the generic type argument of a declared return type
(`Result<T>` versus `Result<R>` on `onException`): it lives in the Kotlin metadata. The byte-identical
class files above cover it, and the Level 1 check below repeats the comparison under the real build.

## 3. Assumptions

| # | Assumption | If false |
|---|---|---|
| A1 | `kmp-build` moves the sources without editing them, so the line numbers in section 7 hold. | Anchor on the declaration names in the tables instead; the compiler's diagnostics list the same declarations by file and line. |
| A2 | Kotlin 2.4.21's explicit-API rules equal those of the 2.2.0 compiler used for verification (the 2.4.21 checker source was read and matches). | `compileKotlinJvm` reports a declaration not listed here; add the modifier the diagnostic names. The count in the commit message is then adjusted. |
| A3 | `kmp-build`'s `kotlin { }` block exists and holds the target declarations and the toolchain/pin settings. | Place `explicitApi()` in whatever block the build file uses for extension-level settings; it is a statement on the `kotlin {}` extension. |
| A4 | `api/GeneralTools.api` equals the tag `2.2.0` dump when this unit starts (`kmp-build`'s postcondition). | A non-empty dump diff after this unit is then not attributable to this unit; `kmp-build` must be corrected first. |

## 4. Non-goals

- No API change of any kind: no renamed, added, removed, narrowed or widened declaration. The one
  case that looks like a visibility change, the `TwoDoubles` secondary constructor written `protected`,
  restates the sealed-class default and compiles to identical bytecode (section 7.9).
- No KDoc, comment or formatting change beyond the inserted tokens. Existing documentation gaps are listed
  in Known gaps; they belong to the units that change those declarations.
- No change to `private` / `internal` declarations, to local functions, to tests, to resources, or to the
  CI workflow.
- No `@SupportedExtension`, `@RequiresOptIn` or other tier annotation; those arrive with `two-doubles`.
- No narrower visibility on any declaration: none is effectively internal.

## 5. Design

`explicitApi()` is the strict mode (`-Xexplicit-api=strict`): omissions are errors, not warnings. The mode
is switched on and the sources are brought into compliance in the same commit, because the first alone
breaks the build. The edit has four mechanical forms, and nothing else:

1. Prefix the declaration with `public`, after any annotation and before every other modifier
   (`@Throws(...) public fun`, `public inline fun`, `public infix fun`, `public operator fun`,
   `public const val`, `public data class`, `public companion object`).
2. Where a function body is a single expression, or a property has no declared type, insert `: <Type>`
   where the inferred type is the type in the tables of section 7.
3. A constructor property of a **non-data** class (`TwoDoubles.first` / `second`,
   `MessageBuilder.color`, `IJColor.colorCode`) gets `public`.
4. The `TwoDoubles` secondary constructor gets `protected`, the only legal explicit form in a sealed class.

Rationale for the rule "add `public` where the compiler demands it":

- Constructor properties of data classes, overrides, accessors and primary constructors stay bare because
  the compiler exempts them and the convention is not to write the modifier there.
- The three properties declared in the body of the data class `Segment` (`delta`, `length`,
  `lengthSquared`) are exempt only because the compiler's data-class rule is not limited to constructor
  properties. They get `public` anyway so the sibling members `pointAt` and `asRay` do not read
  differently from them.

Rationale for the explicit types: `Result<T>` rather than `Result<R>` on `onException` (section 7.2),
`Unit` on the `TwoDoubles` mutators and on `Logger.time` (section 7.2, 7.9), and `String` / `Boolean` /
`Double` / `File` for the rest. Each type is the compiler-inferred one.

Error handling, logging, concurrency: none of the 118 signatures changes its failure path, its
mutability or its thread-safety. `Result`-returning functions keep their `Result` types
(`distanceFrom: Result<Double>`, `normalized: Result<Point>`, `asRay` / `unit: Result<Ray>`, the three
intersection functions, `onException: Result<T>`). No `Result` is unwrapped anywhere new. No log event
is added, removed or re-levelled.

## 6. Staging

One commit (section 11). The mode and the edits cannot be separated: with the mode on and the edits
absent, `compileKotlinJvm` fails with 155 errors; with the edits present and the mode off, nothing
enforces them.

Suggested working order inside the commit, so the compiler is the oracle throughout:

1. Before touching anything, build once and snapshot the classes (section 9, Level 1 step 2).
2. Add `explicitApi()`; run `./gradlew compileKotlinJvm` and confirm the errors match the 115 in section 7.
3. Apply the edits file by file; re-run until the compile is clean.
4. Compare the class directory with the snapshot; run the dump check and the tests.

## 7. File-by-file changes

All paths are under `src/jvmMain/kotlin/`. "V" = add `public` (or `protected` where stated). "V+T" = add
`public` and the explicit type shown. Line numbers are the current ones; no edit adds, removes or splits a
line, so every line number cited by another plan stays valid after this unit, and only the columns of
edited lines shift. Files keep their CRLF line endings; an editor or tool that rewrites line endings must
not be used.

Count per file (declarations edited / of which with a new type):
`Color.kt` 30 / 14; `Misc.kt` 10 / 7; `UserActions.kt` 7 / 1; `AxisAlignedBox.kt` 6 / 0; `CenteredBox.kt` 5 / 0;
`Intersections.kt` 7 / 0; `Point.kt` 9 / 4; `Ray.kt` 3 / 0; `Segment.kt` 6 / 0; `Square.kt` 1 / 0;
`TwoDoubles.kt` 15 / 10; `Vectors.kt` 7 / 0; `logging/Logger.kt` 5 / 2; `logging/MessageBuilder.kt` 7 / 2.
**Total 118 declarations (106 outside `logging`), 40 with a new explicit type, in 14 files.**
Plus `build.gradle.kts` (one statement) and `README.md` (one paragraph).

### 7.0 `build.gradle.kts`

Add, inside the `kotlin { }` block and directly after the `jvmToolchain(17)` call (or, if that call is
placed elsewhere, before the target declarations):

```kotlin
    // Strict explicit API: a declaration is public only on purpose, because this module exports to
    // three targets. Test source sets are not subject to it.
    explicitApi()
```

Signature facts: `explicitApi()` is a member of the Kotlin Gradle plugin's `KotlinBaseExtension`; it sets
`ExplicitApiMode.Strict`. It returns `Unit`. A failure surfaces as a compile error at the next compile, not
as an exception from the DSL.

### 7.1 `generaltools/Color.kt`

| Line | Declaration | Edit | Resulting text of the changed part |
|---|---|---|---|
| 23 | `data class Color` | V | `public data class Color(val red: Int, val green: Int, val blue: Int, val alpha: Int = 255) {` (constructor properties stay bare: data class) |
| 30 | `normalized` | V | `public fun normalized(): FloatArray =` |
| 44 | `lightened` | V | `public fun lightened(fraction: Double = 0.35): Color {` |
| 61 | `darkened` | V | `public fun darkened(...)` |
| 76 | `withAlpha` | V | `public fun withAlpha(newAlpha: Int): Color = ...` |
| 83 | `inverted` | V | `public fun inverted(): Color = ...` |
| 91 | `grayscale` | V | `public fun grayscale(): Color {` |
| 106 | `lerp` | V | `public fun lerp(target: Color, fraction: Float): Color {` |
| 127 | `luminance` | V | `public fun luminance(): Float = ...` |
| 136 | `toPackedInt` | V | `public fun toPackedInt(): Int = ...` |
| 147 | `toHex` | V | `public fun toHex(includeAlpha: Boolean = false): String {` |
| 155 | `companion object` | V | `public companion object {` |
| 156-168, 170 | `WHITE`, `BLACK`, `RED`, `GREEN`, `BLUE`, `YELLOW`, `CYAN`, `MAGENTA`, `ORANGE`, `PURPLE`, `GRAY`, `LIGHT_GRAY`, `DARK_GRAY` (156-168), `TRANSPARENT` (170) | V+T | `public val WHITE: Color = Color(255, 255, 255)` and likewise for all 14 (line 169 is a KDoc line; do not touch) |
| 185 | `of` | V | `public fun of(red: Int, green: Int, blue: Int, alpha: Int = 255): Color =` |
| 206 | `fromNormalized` | V | `public fun fromNormalized(r: Float, g: Float, b: Float, a: Float = 1f): Color {` |
| 223 | `fromPackedInt` | V | `public fun fromPackedInt(argb: Int): Color {` |
| 243 | `fromHex` | V | `public fun fromHex(hex: String): Color {` |

Not edited: the local functions `towardWhite` (:48), `towardBlack` (:65), `interpolate` (:111) and
`channel` (:210); the compiler exempts local declarations. Failure paths unchanged: `fromHex` still throws
`IllegalArgumentException` (programmer-facing contract documented in its KDoc), everything else is total.

### 7.2 `generaltools/Misc.kt`

| Line | Declaration | Edit | Resulting text of the changed part |
|---|---|---|---|
| 27 | `String.capitalizeEveryWord` | V+T | `public fun String.capitalizeEveryWord(): String =` (the chain ends in `String.replace`) |
| 35 | `String.asURL` | V | `@Throws(MalformedURLException::class) public fun String.asURL(): URL {` |
| 43 | `read` | V | `public fun read(textFile:String): List<String> {` |
| 57 | `evaluateList` | V | `public fun <T>evaluateList(list:Iterable<T>, validator: Predicate<T>):Boolean{` (the `Predicate` parameter is the `generaltools-breaks` unit's to change) |
| 73 | `profile` | V+T | `public fun profile(nameOfAction:String, action:()->Unit): String = "..."` |
| 75 | `Logger.time` | V+T | `public fun Logger.time(actionName:String,action:()->Unit): Unit = info(profile(actionName,action))` (slf4j `Logger.info(String)` is a Java `void`, so `Unit`) |
| 84, 87 | `Result<T>.onException` | V+T | line 84: `public inline fun <R, T : R> Result<T>.onException(`; line 87: `): Result<T> = recoverCatching { ex ->` |
| 95 | `Collection<T>.forEveryIndexed` | V+T | `public infix fun <T> Collection<T>.forEveryIndexed(consume:(Int,T)->Unit): Unit =` |
| 102 | `Collection<T>.forEvery` | V+T | `public infix fun <T> Collection<T>.forEvery(consume:(T)->Unit): Unit = forEach { consume(it) }` |
| 108 | `String.trimIfLongerThan` | V+T | `public infix fun String.trimIfLongerThan(length:Int): String = if(this.length > length) substring(0,length) else this` |

**`onException` is the declaration to watch.** Its inferred return type is `Result<T>`, not `Result<R>`:
`recoverCatching`'s own result parameter is fixed by the lambda, which returns `T`, and `R` appears only as
an upper bound of `T`. Writing `Result<R>` changes the Kotlin metadata of `MiscKt` (verified: the class file
differs) and therefore the declared signature of a public inline function, even though the JVM descriptor
(`Object`) stays the same. Use `Result<T>`.

Not edited: the `private val log` at :22. Private; exempt.

### 7.3 `generaltools/UserActions.kt`

| Line | Declaration | Edit | Resulting text of the changed part |
|---|---|---|---|
| 22 | `cropImage` | V | `public fun cropImage(...): BufferedImage {` |
| 41 | `BufferedImage.to` | V+T | `public infix fun BufferedImage.to(fileName:String): File = saveImage(this, fileName)` (`saveImage` declares `File`) |
| 45 | `File.saveTo` | V | `public infix fun File.saveTo(fileName:String) {` (block body; `Unit`) |
| 53 | `screenshotBrowser` | V | `public fun screenshotBrowser(address:String): BufferedImage {` |
| 62 | `openInBrowser` | V | `public fun openInBrowser(address:String) {` |
| 70 | `screenshotArea` | V | `public fun screenshotArea(...): BufferedImage {` |
| 77 | `screenshotFull` | V | `public fun screenshotFull(): BufferedImage {` |

Not edited: `saveImage`, `screenshot`, `click`, `typeKey`, `shiftType`, `typeText`, `typeCharacters`,
`typeRandomKey`, `typeRandomKeys` and `log`, all `private`. `java.awt.Rectangle` stays an explicit import
(:6), so the future `com.spartanlabs.geometry.Rectangle` never collides with it.

### 7.4 `geometry/AxisAlignedBox.kt`

| Line | Declaration | Edit |
|---|---|---|
| 14 | `AxisAlignedBox` | V: `public interface AxisAlignedBox {` |
| 16 / 19 / 22 / 25 | `min` / `max` / `center` / `size` | V: `public val min: Point` (types already declared; interface members need `public` too) |
| 35 | `contains` | V: `public infix fun contains(p: Point): Boolean =` |

### 7.5 `geometry/CenteredBox.kt`

| Line | Declaration | Edit |
|---|---|---|
| 22 | `CenteredBox` | V: `public data class CenteredBox(` (`center` is an override, `halfExtents` a data-class constructor property: both stay bare) |
| 38 | `companion object` | V: `public companion object {` |
| 48 | `fromCorners` | V: `public fun fromCorners(c1: Point, c2: Point): CenteredBox {` |
| 67 | `Square.toCenteredBox` | V: `public fun Square.toCenteredBox(): CenteredBox =` |
| 79 | `AxisAlignedBox.toSquare` | V: `public fun AxisAlignedBox.toSquare(): Square =` |

The `override val min/max/size` members (:27, :31, :35) are overrides with declared types; untouched.

### 7.6 `geometry/Intersections.kt`

| Line | Declaration | Edit |
|---|---|---|
| 18 | `SegmentIntersection` | V: `public sealed interface SegmentIntersection {` |
| 20 | `None` | V: `public data object None : SegmentIntersection` |
| 28 | `Touching` | V: `public data class Touching(val point: Point) : SegmentIntersection` (`point` bare: data class) |
| 37 | `Overlapping` | V: `public data class Overlapping(val segment: Segment) : SegmentIntersection` |
| 60 | `segmentIntersectsSegment` | V |
| 131 | `segmentIntersectsBox` | V |
| 188 | `rayIntersectsBox` | V |

Not edited: `boxProblem` (:249, private) and `log` (:7). All three public functions already declare their
`Result<...>` return types; their failure paths (`Result.failure(IllegalArgumentException)`) and the WARN
logs are untouched.

### 7.7 `geometry/Point.kt`

| Line | Declaration | Edit | Resulting text of the changed part |
|---|---|---|---|
| 14 | `Point` | V | `public class Point(x:Double = 0.0,y:Double = 0.0) : TwoDoubles(x,y){` |
| 16 | secondary constructor | V | `public constructor(point: Point) : this(point.x, point.y)` |
| 18 | `Point.x` | V+T | `public var x: Double` (accessors on :19-20 unchanged; the getter returns `first: Double`) |
| 22 | `Point.y` | V+T | `public var y: Double` |
| 30 | `distanceFrom` | V | `public infix fun distanceFrom(other: Point): Result<Double> =` |
| 40 | `Dimensions` | V | `public class Dimensions(width:Double = 0.0,height: Double = 0.0) : TwoDoubles(width,height){` |
| 42 | secondary constructor | V | `public constructor(dimensions:Dimensions) : this(dimensions.width,dimensions.height)` |
| 44 | `Dimensions.width` | V+T | `public var width: Double` |
| 48 | `Dimensions.height` | V+T | `public var height: Double` |

### 7.8 `geometry/Ray.kt`, `Segment.kt`, `Square.kt`

| File | Line | Declaration | Edit |
|---|---|---|---|
| `Ray.kt` | 16 | `Ray` | V: `public data class Ray(val origin: Point, val direction: Point) {` |
| `Ray.kt` | 24 | `pointAt` | V |
| `Ray.kt` | 35 | `unit` | V (return type `Result<Ray>` already declared) |
| `Segment.kt` | 18 | `Segment` | V: `public data class Segment(val a: Point, val b: Point) {` |
| `Segment.kt` | 20 | `delta` | V (uniformity, not demanded): `public val delta: Point get() = ...` |
| `Segment.kt` | 23 | `length` | V (uniformity): `public val length: Double get() = delta.length` |
| `Segment.kt` | 26 | `lengthSquared` | V (uniformity): `public val lengthSquared: Double get() = ...` |
| `Segment.kt` | 35 | `pointAt` | V |
| `Segment.kt` | 45 | `asRay` | V |
| `Square.kt` | 16 | `Square` | V: `public data class Square (` (`location` and `dimensions` are data-class constructor properties; `min` / `max` / `center` / `size` are overrides) |

### 7.9 `geometry/TwoDoubles.kt`

| Line | Declaration | Edit | Resulting text of the changed part |
|---|---|---|---|
| 13 | class and its two constructor properties | V x3 | `public sealed class TwoDoubles(public var first: Double = 0.0, public var second: Double = 0.0){` |
| 15 | secondary constructor | `protected` | `protected constructor(doubles: TwoDoubles): this(doubles.first, doubles.second)` |
| 17 | `containsNaN` | V+T | `public val containsNaN: Boolean get() = first.isNaN() \|\| second.isNaN()` |
| 19 | `setTo(Double, Double)` | V+T | `public fun setTo(first: Double, second: Double): Unit = with(this){` |
| 24 | `modBy(Double, Double)` | V+T | `public fun modBy(first: Double, second: Double): Unit = with(this){` |
| 29 | `setTo(TwoDoubles)` | V+T | `public infix fun setTo(new: TwoDoubles): Unit =    setTo(new.first, new.second)` |
| 31 | `modBy(TwoDoubles)` | V+T | `public infix fun modBy(change: TwoDoubles): Unit = modBy(change.first, change.second)` |
| 33 | `amplifyBy` | V | `public infix fun amplifyBy(magnitude: Double) {` |
| 42 | `divideBy` | V+T | `public infix fun divideBy(magnitude: Double): Unit =` |
| 49 | `plusAssign` | V+T | `public operator fun plusAssign(doubles: TwoDoubles): Unit = modBy(doubles)` |
| 51 | `minusAssign` | V+T | `public operator fun minusAssign(doubles: TwoDoubles): Unit = modBy(-doubles.first, -doubles.second)` |
| 53 | `timesAssign` | V+T | `public operator fun timesAssign(magnitude: Double): Unit = amplifyBy(magnitude)` |
| 55 | `divAssign` | V+T | `public operator fun divAssign(magnitude: Double): Unit = divideBy(magnitude)` |

Why each type is `Unit`: the `with(this){ ... }` lambdas of `setTo` and `modBy` end in an assignment
statement; `modBy` / `amplifyBy` are `Unit`; `divideBy`'s `if` branch ends in `throw` (`Nothing`) and its
`else` is `amplifyBy(...)` (`Unit`), whose common supertype is `Unit`. The existing dump entries are
`(D)V` / `(DD)V` for all of them.

The secondary constructor is `protected`, not `public`: the primary constructor is exempt, but a secondary
constructor of a sealed class must be `private` or `protected`, and the default is `protected`. Writing it
out compiles to the same bytecode (verified). The `equals` (:61) and `hashCode` (:72) overrides and
`toString` (:75) already declare their types and are overrides; untouched. `private val log` (:6)
untouched.

### 7.10 `geometry/Vectors.kt`

| Line | Declaration | Edit |
|---|---|---|
| 27 | `EPSILON` | V: `public const val EPSILON: Double = 1e-10` |
| 37 | `Point.dot` | V |
| 49 | `Point.cross` | V |
| 56 | `Point.length` | V: `public val Point.length: Double get() = ...` |
| 64 | `Point.lengthSquared` | V |
| 74 | `Point.normalized` | V |
| 100 | `Point.projectedOnto` | V |

All types are already declared.

### 7.11 `logging/Logger.kt` and `logging/MessageBuilder.kt`

These files are deleted by the `logging` unit's last commit. They are edited now because they exist and
are public API at this point; the mode fails the build otherwise.

| File:line | Declaration | Edit | Resulting text of the changed part |
|---|---|---|---|
| `Logger.kt:3` | `main` | V | `public fun main(){` |
| `Logger.kt:6` | `Logger` | V | `public class Logger{` |
| `Logger.kt:7` | `printToConsole` | V+T | `public var printToConsole: Boolean = true` |
| `Logger.kt:8` | `info` (property) | V+T | `public val info: MessageBuilder = MessageBuilder(color = IJColor.INFO)` |
| `Logger.kt:9` | `info(String)` | V | `public infix fun info(message : String) {` |
| `MessageBuilder.kt:3` | `MessageBuilder` and its `color` property | V x2 | `public class MessageBuilder(showClass:Boolean = true, showTime:Boolean = false, showThread:Boolean = false, showStackTrace:Boolean = false, public val color:IJColor){` |
| `MessageBuilder.kt:4` | `modify` | V | `public infix fun modify(message:String): String {` |
| `MessageBuilder.kt:8` | `IJColor` and its `colorCode` property | V x2 | `public enum class IJColor(public val colorCode:String){` (entry `INFO` is exempt) |
| `MessageBuilder.kt:10` | `reset` | V+T | `public val reset: String = "\u001b[0m"` |
| `MessageBuilder.kt:11` | `plus` | V+T | `public operator fun plus(restOfString:String): String = colorCode + restOfString + reset` |

### 7.12 `README.md`

See section 8.

## 8. Documentation impact

- **Ring 1 (in-editor):** none; the tokens inserted are the navigation signal (`public` is now visible on
  every API line).
- **Ring 2 (component / KDoc):** no KDoc text changes. The explicit `public` and the explicit types are
  the in-source form of the API contract and make the declaration-site types readable in the editor
  without inference. Level-2 KDoc completeness is not this unit's concern (Known gaps).
- **Ring 3 (boundary / protocol):** none; the dump (`api/GeneralTools.api`) is unchanged by design.
- **Ring 4 (architecture):** `architecture.md` already states the mode (S2, row 2); no change.
- **README currency:** the change alters a build rule for contributors, so `README.md` gets one paragraph
  appended at the end of its "Build & test" section (which `kmp-build` has already rewritten):

  > The library compiles in Kotlin's strict explicit API mode: every declaration reachable from outside
  > the module states `public`, and every public function or property whose type would be inferred
  > states its type. Test source sets are not subject to it. A missing modifier or type fails
  > `compileKotlinJvm` (and the web compilations once they hold code).

  No coordinates, module list, dependency, command or protocol changes, so no other README section moves.
- **Migration notes:** none; no consumer-visible change.

## 9. Test plan by level

No test file is added, changed or moved. The unit changes no behaviour, and explicit API mode is itself
enforced by the compiler on every build; a unit test asserting "this declaration is public" would test
the language, not the library.

**Level 1, local gating (before the commit).** Run from the repository root with the working tree as it
will be committed:

1. Baseline snapshot, before any edit (after `kmp-build`'s state is checked out):
   `./gradlew clean compileKotlinJvm`, then copy the JVM main class directory aside, for example
   `cp -r build/classes/kotlin/jvm/main /tmp/classes-before` (confirm the directory with
   `find build/classes -name Color.class`).
2. After all edits: `./gradlew clean compileKotlinJvm`, then
   `diff -r /tmp/classes-before build/classes/kotlin/jvm/main` -> **no output** (all class files
   byte-identical, Kotlin metadata included). A difference points at an explicit type that is not the
   inferred one; `onException` (section 7.2) is the likeliest.
3. `./gradlew compileTestKotlinJvm` -> succeeds (tests compile unchanged and are not under the mode).
4. `./gradlew jvmTest` -> all 185 tests pass. Count with
   `grep -h -o 'tests="[0-9]*"' build/test-results/jvmTest/*.xml` summed, or read the Gradle summary.
5. `./gradlew checkKotlinAbi` -> passes **without** running `updateKotlinAbi`;
   `git status --short api/` -> empty.
6. `./gradlew compileKotlinJs compileKotlinWasmJs` -> succeed (empty source sets).
7. `./gradlew check` -> passes (this repeats 4 and 5 and runs the web test tasks, which have no tests).
8. Negative control, not committed: delete one `public` (for example on `Segment.pointAt`), run
   `./gradlew compileKotlinJvm`, expect `visibility must be specified in explicit API mode`, then restore
   the file with `git checkout -- <file>` only if the file has no other uncommitted edit (otherwise
   re-insert the token by hand).
9. `git diff --numstat` shows 16 files modified (14 Kotlin sources, `build.gradle.kts`, `README.md`) plus the
   new plan document. For each Kotlin file the added and removed line counts are equal (no line is added,
   removed or split); the 14 files together change 115 lines (`Color.kt` 30, `Misc.kt` 11, `UserActions.kt` 7,
   `AxisAlignedBox.kt` 6, `CenteredBox.kt` 5, `Intersections.kt` 7, `Point.kt` 9, `Ray.kt` 3, `Segment.kt` 6,
   `Square.kt` 1, `TwoDoubles.kt` 13, `Vectors.kt` 7, `Logger.kt` 5, `MessageBuilder.kt` 5); the 118
   declarations outnumber the lines because lines 13 of `TwoDoubles.kt`, 3 and 8 of `MessageBuilder.kt` hold
   a class and its constructor properties, and `Misc.kt` counts both lines of `onException`.

**Level 2, component.** No new tests. The existing component suites run unchanged under `jvmTest` in
step 4 and are the behavioural proof.

**Level 3, integration.** No new tests. The boundary gate is `checkKotlinAbi` (step 5): it compares the
compiled public API against the committed baseline and is the integration check for this unit.

**Level 4a, deterministic.** No new tests: no function was added or changed.

**Level 4b, end-to-end.** None. The consumer smoke matrix belongs to `release`.

**Level 4c, non-functional.** None. Compilation time and artefact size are unaffected: the class files are
byte-identical.

**Level 5, UAT.** None.

**Cannot be automated:** whether each explicit type is the type a human would have chosen is not checked;
only that it equals the inferred one (steps 2 and 5).

## 10. Risks & edge cases

| Risk | Handling |
|---|---|
| An explicit type differs from the inferred one and silently changes the API (the `Result<R>` trap on `onException`). | The class-file diff (step 2) catches it, including metadata the JVM dump cannot see. The table gives the verified type for every one of the 40. |
| `public constructor` on the sealed `TwoDoubles` is written by reflex. | It is a compile error; the table says `protected`. `two-doubles` later changes the class to `abstract` and keeps the `protected` constructor. |
| Line-ending damage: the working tree is CRLF. | Edit in place without a tool that normalises endings; the Level 1 hunk check (step 9) shows any whole-file rewrite at once. |
| `updateKotlinAbi` is run "to fix" a red `checkKotlinAbi`. | A red check here means an edit changed the API; fix the edit. The baseline must not move in this unit. |
| Kotlin 2.4.21 reports a declaration this plan did not list. | A2. Add the modifier the diagnostic names; the class-file diff still applies. |
| The `Segment` body properties get `public` although the compiler does not ask. | Harmless and covered by the byte-identical check; kept for readability. A reviewer who wants only compiler-demanded edits can drop lines 20, 23, 26 of `Segment.kt`; nothing else depends on them. |
| Later units add declarations without `public` or a type. | The build fails at once, which is the purpose of the mode. Every sibling plan writes new public declarations in this form. |
| JS and Wasm compilations. | Empty today; the mode applies to them when `commonize` moves code, and the same sources compile there under the same rules. |
| Cross-repository impact. | None. No downstream repository changes, and none is touched. |

## 11. Version control

- **Branch:** `feat/issue-7-kotlin-multiplatform`. The `manager` agent performs every git step; this plan
  describes the commit and runs nothing.
- **Precondition:** `master` (`c196d1a`) is merged into the branch and `kmp-build` has landed.
- **Commit (one):** `refactor: enable strict explicit API mode`.
  - Body: states that no API changes, that 118 declarations gain `public`, that 40 gain the type the
    compiler inferred, that the three `Segment` body properties were annotated for uniformity, that the
    `TwoDoubles` secondary constructor is written `protected`, and that the JVM dump is unchanged.
  - Contents, staged by path:
    - `build.gradle.kts`;
    - `src/jvmMain/kotlin/generaltools/Color.kt`, `Misc.kt`, `UserActions.kt`;
    - `src/jvmMain/kotlin/geometry/AxisAlignedBox.kt`, `CenteredBox.kt`, `Intersections.kt`, `Point.kt`,
      `Ray.kt`, `Segment.kt`, `Square.kt`, `TwoDoubles.kt`, `Vectors.kt`;
    - `src/jvmMain/kotlin/logging/Logger.kt`, `MessageBuilder.kt`;
    - `README.md`;
    - `docs/plans/7-kotlin-multiplatform/plan-explicit-api.md` (this document, in the same commit).
  - No attribution trailers.
- **Expected dump delta:** none. `api/GeneralTools.api` is not modified; no klib dump exists yet.
- **Proof:** `git diff --stat HEAD~1 HEAD -- api/` prints nothing; `./gradlew checkKotlinAbi` passes
  without `updateKotlinAbi`; the class-directory diff of Level 1 step 2 is empty.
- **Planning documents** ride with the work they plan: `architecture.md`, `impact.md` and the dated
  pointers on the two earlier plans are already in from the first `kmp-build` commit, and each other unit's
  `plan-<unit>.md` lands in that unit's first commit. This commit therefore carries only its own plan.
  Stage by explicit path, never with `git add -A`.

## 12. Interfaces with sibling units

**Expects from `kmp-build` (unit 1):**

- the multiplatform plugin applied; `kotlin { }` extension present with `jvm()`, `js { nodejs() }`,
  `wasmJs { nodejs() }`, `jvmToolchain(17)` and the `languageVersion` / `apiVersion` 2.2 pins;
- every source file under `src/jvmMain/kotlin/{generaltools,geometry,logging}/` and every test under
  `src/jvmTest/kotlin/...`, byte-for-byte as in `src/main` / `src/test`, so the line numbers of section 7
  hold;
- `abiValidation()` configured, `api/GeneralTools.api` equal to the tag `2.2.0` dump, `check` depending
  on `checkKotlinAbi`;
- the Gradle task names `compileKotlinJvm`, `compileTestKotlinJvm`, `jvmTest`, `checkKotlinAbi`,
  `compileKotlinJs`, `compileKotlinWasmJs`, `check`.

**Provides to `logging` (unit 3):**

- `logging/Logger.kt` and `MessageBuilder.kt` carry modifiers and types as in section 7.11; the unit
  deletes them in its last commit, so a delete of the already-annotated files is clean;
- all public declarations in `geometry/*` carry `public` and explicit types; its edits (the five logger
  declarations, the eleven failure sites) touch `private val log` and function bodies only, and its new
  `internal` / `private` declarations need no modifier;
- no line is added or removed by this unit, so its line citations into the geometry files stay valid.

**Provides to `commonize` (unit 4):**

- every declaration it moves, splits or rewrites already carries `public` and its explicit type
  (`Misc.kt`'s `profile`, `forEvery*`, `trimIfLongerThan` are `public ...: String` / `: Unit`); the split
  of `Misc.kt` into `Misc.kt` and `MiscJvm.kt` keeps those tokens on every moved declaration, and the new
  file declarations written there follow the same form;
- `Color.fromHex`, `Color.toHex` and the other `Color` members are in their final modifier form, so the
  `String.format` replacement touches bodies only.

**Provides to `two-doubles` (unit 5):**

- `TwoDoubles` is `public sealed class` with `public var first: Double` / `second: Double` and a
  `protected constructor(doubles: TwoDoubles)`; the unit turns `sealed` into `abstract`, adds the
  protected constructors required by its design, and keeps the explicit `protected` form on the existing
  secondary constructor (no bytecode change from that restatement);
- `divideBy` is `public infix fun divideBy(magnitude: Double): Unit`; the unit changes it in place to
  `Result<Unit>`, and `divAssign` is `public operator fun divAssign(magnitude: Double): Unit`, which
  becomes a block body that throws;
- `containsNaN` is `public val containsNaN: Boolean`; `Point.x` / `y` and `Dimensions.width` /
  `height` are `public var ...: Double`.

**Provides to `box-model` (unit 6):**

- `Square` is `public data class Square (` at `Square.kt:16`; `toSquare` and `toCenteredBox` are public with
  declared types at `CenteredBox.kt:67, 79`; `AxisAlignedBox` is `public interface` with `public val`
  members and `public infix fun contains`, which the unit makes an `operator`; new members the unit adds to
  these types are written `public` with an explicit return type (the mode requires it).

**Provides to `generaltools-breaks` (unit 7):**

- `Misc.kt` declarations carry `public` and explicit types: `capitalizeEveryWord(): String`,
  `trimIfLongerThan(...): String`, `evaluateList(...):Boolean` (parameter still `Predicate<T>`);
  `Color`'s constructor is `public data class Color(val red: Int, ...)` at `Color.kt:23` with the
  constructor properties bare (data class), which the unit's validation edit leaves as they are.

**Provides to `release` (unit 8):** a dump identical to the baseline after this unit, so every row of the
final diff is attributable to a later unit's commit.

## 13. Sequencing & follow-ups

- Lands directly after `kmp-build` and before `logging`, so the later break commits are not buried in
  modifier noise (architecture 11.2, row 2).
- Follow-up for the units that add public declarations: `public` plus an explicit type on every new
  declaration, `protected` or `internal` where the design says so.
- Follow-up for `commonize`: confirm the klib dump (`api/GeneralTools.klib.api`) lists the same public
  surface; no expectation is added here.
- No work is deferred out of the release.

## 14. Known gaps

- Gradle could not be run during planning. The completeness of section 7 and the byte-identity of the
  output were established with a standalone Kotlin 2.2.0 compiler at language and API version 2.2
  (155 diagnostics before, none after; 29 class files identical). The 2.4.21 checker source was read and
  gives the same rules, but the real `compileKotlinJvm` under KGP 2.4.21 has not been run on the edited
  tree; the Level 1 steps close that gap at implementation time.
- `api/GeneralTools.api` does not exist yet at planning time (`kmp-build` creates it), so the dump
  comparison is predicted from the byte-identical class files rather than executed.
- The JS and Wasm compilations contain no code at this point, so the mode is not exercised on them.
- Several public declarations lack Level-2 KDoc: the thirteen named `Color` constants other than
  `TRANSPARENT`, and the `Color.Companion` and `CenteredBox.Companion` objects. This unit adds none
  because it changes no declaration's contract; `generaltools-breaks` documents the `Color` ones and
  `box-model` the `CenteredBox` companion, in the commits that already rewrite those files' KDoc.

## 15. Considerations

None.
