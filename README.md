# GeneralTools

A small Kotlin Multiplatform library of generic helpers: an immutable RGBA `Color` value type, simple 2D
geometry and string, list and timing utilities, usable from common code on the JVM, JS and Wasm/JS; and,
on the JVM only, file and URL helpers and AWT-based user-action automation.

| Target | Artifact | You need |
|---|---|---|
| JVM | `GeneralTools-jvm` | Java 17 or newer; build with Kotlin 2.1 or newer |
| JS (tested on Node) | `GeneralTools-js` | Kotlin 2.4 or newer |
| Wasm/JS | `GeneralTools-wasm-js` | Kotlin 2.4 or newer; Kotlin/Wasm is Beta, so its klib compatibility guarantees are not final |

`read`, `asURL`, the slf4j `Logger.time` extension and `UserActions` exist on the JVM only. Upgrading from 2.x:
see the [3.0.0 migration notes](#300-migration-notes).

## Maven coordinates

Published to Maven Central as `io.github.spartanlaboratories:GeneralTools:3.0.0`. The coordinate `GeneralTools`
is the Gradle module-metadata root; each target has its own artifact:

| Artifact | Contents |
|---|---|
| `GeneralTools` | Gradle module metadata that selects the variant of your target; contains no classes |
| `GeneralTools-jvm` | the JVM jar |
| `GeneralTools-js` | the JS klib |
| `GeneralTools-wasm-js` | the Wasm/JS klib |

Gradle (Kotlin DSL), in a JVM or a Multiplatform project; Gradle selects the variant:

```kotlin
dependencies {
    implementation("io.github.spartanlaboratories:GeneralTools:3.0.0")   // in commonMain.dependencies of a multiplatform project
}
```

Maven does not read Gradle module metadata, so depend on the JVM artifact:

```xml
<dependency>
    <groupId>io.github.spartanlaboratories</groupId>
    <artifactId>GeneralTools-jvm</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Modules

Everything below is common code, available on the `jvm`, `js` and `wasmJs` targets, except the items marked
**JVM only**, which exist only in the `jvm` variant (`GeneralTools-jvm`).

- **`com.spartanlabs.generaltools`**
  - `Misc` — string, list and timing helpers: `profile`, `onException`, `forEvery`, `forEveryIndexed` and
    `trimIfLongerThan` are common; **JVM only:** `asURL`, `read`, the slf4j `Logger.time` extension, `evaluateList`
    and `capitalizeEveryWord`.
  - `UserActions` — AWT screenshot capture and input automation. **JVM only.**
  - `Color` — immutable RGBA value type: transforms plus hex / packed-int codecs.
  - `ExperimentalForInheritanceGeneralToolsApi` — opt-in marker for subclassing `TwoDoubles`.
  - `SupportedExtension` — marks a Supported Extension surface; documentation only, not an opt-in.
- **`com.spartanlabs.geometry`**
  - `Point` / `Dimensions` — 2D coordinate / size pairs.
  - `TwoDoubles` — abstract base; subclassing is Experimental.
  - `AxisAlignedBox` — shared box contract, implemented by `Rectangle`
    (top-left origin) and `CenteredBox` (centre origin).
  - `Segment`, `Ray` — line primitives.
  - Vector algebra on `Point`: `dot`, `cross`, `length`, `lengthSquared`,
    `normalized()`, `projectedOnto`.
  - `segmentIntersectsSegment`, `segmentIntersectsBox`, `rayIntersectsBox`
    intersection tests.
- **`com.spartanlabs.geometry.arithmetic`**
  - `plus`/`minus`/`times`/`div` for `Point` and `Dimensions` — import to use; Supported Extension.

## Build & test

Gradle 9 runs on a JDK 17 to 24. The build compiles and tests with a JDK 17 toolchain, which Gradle
downloads when it is missing; the first JS or Wasm test run also downloads Node.js and Yarn.

```sh
./gradlew build                 # compile and test every target, and check the public API dump
./gradlew jvmTest               # JVM tests (JUnit 5 platform)
./gradlew jsNodeTest            # JS tests on Node
./gradlew wasmJsNodeTest        # Wasm/JS tests on Node
./gradlew allTests              # all three targets, one report
./gradlew checkKotlinAbi        # compare the public API with the dumps in api/ (part of check)
./gradlew updateKotlinAbi       # rewrite the dumps after an intended API change; commit the diff
```

Tests live in `com.spartanlabs.testing.<level>.<package>`, one folder per level: `gating`, `component`,
`integration`, `deterministic`, `e2e`, `nonfunctional`, `uat`. `-PtestLevel=<level>[,<level>]` runs only the
named levels on every target; an unknown level fails the build, and left unset every level runs.

```sh
./gradlew check -PtestLevel=gating,component
./gradlew jvmTest -PtestLevel=integration     # a JVM-only level: skip the web test tasks, which still start Node
```

After a Kotlin Gradle plugin bump, run `./gradlew kotlinUpgradeYarnLock` and commit `kotlin-js-store/`.

The library compiles in Kotlin's strict explicit API mode: every declaration reachable from outside the module
states `public`, and every public function or property whose type would be inferred states its type. Test source
sets are not subject to it. A missing modifier or type fails `compileKotlinJvm` (and the web compilations once they
hold code).

## Releasing

Releases are published to Maven Central **manually** by a maintainer; there is no CI publish workflow, and
nothing is uploaded or published until the maintainer explicitly starts the release. A Central release cannot
be undone, so the upload and the release are separate steps.

Put the Central Portal user token and a signing key-ring file in `~/.gradle/gradle.properties`:
`mavenCentralUsername`, `mavenCentralPassword`, `signing.keyId`, `signing.password`, `signing.secretKeyRingFile`.

1. Set `version` in `build.gradle.kts`, merge to `master`, and run `./gradlew check` (every level, every target).
2. `./gradlew publishToMavenCentral` uploads one bundle to the Central Portal and stops; it logs the deployment id.
   Do not use `publishAndReleaseToMavenCentral`: it publishes as soon as validation passes.
3. In the Portal, wait for the deployment to be `VALIDATED`, then build a throwaway consumer against the staged
   bundle through the Portal's download endpoint (Sonatype, "Manually Testing a Deployment Bundle").
4. Press Publish in the Portal (or drop a bad bundle: `./gradlew dropMavenCentralDeployment --deployment-id=<id>`).
   The artifacts appear on Maven Central 10 to 30 minutes later; resolve them once from plain `mavenCentral()`.
5. Tag the merge commit on `master` `X.Y.Z` and create a matching GitHub release; its notes are the migration notes below.

## `Color` quick reference

Constants: `WHITE`, `BLACK`, `RED`, `GREEN`, `BLUE`, `YELLOW`, `CYAN`,
`MAGENTA`, `ORANGE`, `PURPLE`, `GRAY`, `LIGHT_GRAY`, `DARK_GRAY`, `TRANSPARENT`.

Instance methods: `normalized()`, `lightened(fraction = 0.35)`,
`darkened(fraction = 0.35)`, `withAlpha(newAlpha)`, `inverted()`, `grayscale()`,
`lerp(target, fraction)`, `luminance()`, `toPackedInt()`,
`toHex(includeAlpha = false)`.

Factories: `Color.of(red, green, blue, alpha = 255)` (clamps each channel into
`0..255` — use it for untrusted input; the primary constructor does not
validate), `Color.fromNormalized(r, g, b, a = 1f)`, `Color.fromPackedInt(argb)`,
`Color.fromHex(hex)`.

Hex and packed-int forms use ARGB byte order: `toHex(includeAlpha = true)` emits
`#AARRGGBB` and round-trips through `fromHex`.

```kotlin
val clamped = Color.of(300, 87, -5)               // Color(255, 87, 0) - out-of-range channels clamped
val brand = Color.of(255, 87, 51)
val faded = brand.withAlpha(128)
val hex = faded.toHex(includeAlpha = true)         // "#80FF5733"
val parsed = Color.fromHex(hex)                    // Color(255, 87, 51, 128)
val mixed = Color.BLACK.lerp(Color.WHITE, 0.5f)    // Color(128, 128, 128, 255)
```

## Geometry quick reference

Primitives (all value types; `Point` / `Dimensions` are mutable, so treat the
composites as immutable and do not mutate shared components):

- `Point(x, y)`, `Dimensions(width, height)`.
- `Segment(a, b)` — `delta`, `length`, `lengthSquared`, `pointAt(t)`,
  `asRay(): Result<Ray>`.
- `Ray(origin, direction)` — `direction` need **not** be unit length;
  `pointAt(t)`, `unit(): Result<Ray>`.
- `Rectangle(location, dimensions)` — top-left origin; `location` and `dimensions` are `val` references to mutable
  parts, changed in place (`rect.location.setTo(...)`); `min`/`size` return those live parts, `max`/`center` return
  new points; `copy()` shares the parts; a `Rectangle` or `Point` used as a hash key must not change while in the
  collection.
- `CenteredBox(center, halfExtents)` — centre origin; `CenteredBox.fromCorners(c1, c2)`.

`TwoDoubles` is abstract. Subclassing it needs `@OptIn(ExperimentalForInheritanceGeneralToolsApi::class)` and may
change in a minor release; using `Point`, `Dimensions` or a `TwoDoubles` parameter needs no opt-in.

`divideBy(k)` divides in place and returns `Result<Unit>`; a zero divisor leaves the value unchanged and logs one WARN
(logger `com.spartanlabs.geometry.TwoDoubles`). `p /= k` still throws `IllegalArgumentException("Cannot divide by
zero")` and logs nothing.

Equality: same concrete class, components equal per IEEE except that NaN equals NaN; `0.0` equals `-0.0` and they
hash alike. `Point` and `Dimensions` are mutable: do not change one while it is a hash key; use
`Rectangle.toImmutable()` to key by a box.

Destructuring: `val (x, y) = point`, `val (w, h) = dimensions`. Negation: `-point` (a new `Point`; `Dimensions` has
none).

`AxisAlignedBox` is the shared read-only contract (`min`, `max`, `center`,
`size`, `contains(p)`); both `Rectangle` and `CenteredBox` implement it, and the
box-intersection functions accept either. Convert with `Rectangle.toCenteredBox()`
/ `AxisAlignedBox.toRectangle()` (an independent mutable copy).

Vector algebra (extension functions on `Point`, in `Vectors.kt`):
`a dot b`, `a cross b` (2D perp-dot), `p.length`, `p.lengthSquared`,
`p.normalized(): Result<Point>` (fails on a zero-ish or NaN vector),
`v projectedOnto axis` (zero vector when `axis` is zero-ish).

Intersection functions (all return `Result`; failure on degenerate input —
zero-length segment, zero-direction ray, non-finite coordinates, negative /
non-finite `box.size`; each failure also logs one WARN line, see Logging):

| Function | Success shape |
|---|---|
| `segmentIntersectsSegment(s1, s2)` | `SegmentIntersection` — `None`, `Touching(point)`, or `Overlapping(segment)` (collinear overlap) |
| `segmentIntersectsBox(s, box)` | `Boolean` |
| `rayIntersectsBox(r, box)` | `Double?` — `null` on a miss / box behind the origin, `0.0` when the origin is inside, else the entry `t` (in units of `r.direction.length`) |

`EPSILON` (`1e-10`) is the shared absolute tolerance for parallel / collinear /
on-boundary tests; translate geometry toward the origin before testing at very
large coordinate magnitudes. Parametric `t` values from `rayIntersectsBox` scale
with `|direction|` — call `Ray.unit()` first, or `Ray.pointAt(t)` after, to work
in world distances.

```kotlin
// Occlusion raycast against a tile's box.
val tile = CenteredBox(Point(4.0, 3.0), Dimensions(0.5, 0.5))
val ray = Ray(Point(0.0, 0.0), Point(1.0, 0.75)).unit().getOrThrow()
val hit: Double? = rayIntersectsBox(ray, tile).getOrThrow()   // world distance, or null

// Segment vs segment, keeping the collinear-overlap case.
when (val r = segmentIntersectsSegment(
    Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
    Segment(Point(1.0, 0.0), Point(3.0, 0.0)),
).getOrThrow()) {
    is SegmentIntersection.None -> Unit
    is SegmentIntersection.Touching -> r.point
    is SegmentIntersection.Overlapping -> r.segment          // Segment((1,0), (2,0))
}
```

### Arithmetic operators (opt-in per file)

Value-returning `+`, `-`, `*` and `/` for `Point` and `Dimensions` live in their own package and work only in a
file that imports them: `import com.spartanlabs.geometry.arithmetic.*`, or one operator such as
`import com.spartanlabs.geometry.arithmetic.plus`, which imports all its overloads. `import com.spartanlabs.geometry.*`
does not reach the package.

```kotlin
val corner = location + dimensions
val mid = (a + b) / 2.0
val doubled = 2.0 * offset
```

Each operator returns a new value and leaves its operands unchanged. `div` by `0.0` or `-0.0` throws
`IllegalArgumentException("Cannot divide by zero")` and logs nothing, like `/=`. Each operator allocates its result;
the in-place members (`+=`, `-=`, `*=`, `/=`, `setTo`, `modBy`, `amplifyBy`, `divideBy`) are the allocation-free
alternative.

Importing the package has three costs, confined to the importing file:
- `+=`, `-=`, `*=` and `/=` on a `var` `Point` or `Dimensions` holder are ambiguous and stop compiling; a `val`
  holder is unaffected;
- a wildcard import brings every operator in, and an IDE may add the import when `p + q` does not resolve;
- a member operator added to `Point` or `Dimensions` in a later release would shadow the extension.

## Logging

GeneralTools logs through [kotlin-logging](https://github.com/oshai/kotlin-logging) 8.0.4. On the JVM
it delegates to slf4j, so your existing logback / log4j configuration applies; on JS and Wasm output
goes to the console at a single global level (INFO by default, no per-logger control - to mute
GeneralTools there, wrap kotlin-logging's `Appender` and drop events whose `loggerName` starts with
`com.spartanlabs`).

- **Logger names** are `com.spartanlabs.<package>.<File>`, unchanged from 2.x:
  `com.spartanlabs.geometry.TwoDoubles`, `.Point`, `.Segment`, `.Vectors`, `.Intersections`, and
  `com.spartanlabs.generaltools.Misc` and `com.spartanlabs.generaltools.UserActions`. The JVM-only
  helpers (`read`, `asURL`, `Logger.time`, `UserActions`) log through slf4j directly.
- **Every expected failure logs one WARN line** on its file's logger: `Point.distanceFrom`,
  `Segment.asRay`, `Point.normalized` (and so `Ray.unit`), `segmentIntersectsSegment`,
  `segmentIntersectsBox`, `rayIntersectsBox`, `TwoDoubles.divideBy`. The line is `<exception message>:
  <function>: <offending values>`. The returned `Result` holds an `IllegalArgumentException` with the
  message only; no stack trace is logged.
- A loop that probes degenerate input logs one line per call. On the JVM set
  `com.spartanlabs.geometry` to ERROR in your backend; test cheap preconditions first in hot loops.
- kotlin-logging prints one startup line to stdout on first use. Opt out with
  `-Dkotlin-logging.logStartupMessage=false` or the environment variable
  `KOTLIN_LOGGING_STARTUP_MESSAGE=false`; GeneralTools does not set it for you.

## 3.0.0 migration notes

3.0.0 is a major release. The library is now Kotlin Multiplatform, and the breaking changes listed here are clean
breaks: there is no deprecation cycle. Find your symptom in the first column.

| Symptom | Cause | Fix |
|---|---|---|
| A Maven build resolves `GeneralTools:3.0.0` but every `com.spartanlabs` import is unresolved | `GeneralTools` is now the multiplatform root: it holds Gradle module metadata and no classes, and Maven does not read it | Depend on `io.github.spartanlaboratories:GeneralTools-jvm:3.0.0`. Gradle builds need no change. |
| A Kotlin/JS or Kotlin/Wasm build reports an incompatible klib ABI version for GeneralTools | The `js` and `wasm-js` klibs are built by Kotlin 2.4 and carry its ABI version; `languageVersion` cannot lower it | Build with Kotlin 2.4 or newer. Kotlin/Wasm is Beta, so its klib compatibility guarantees are not final. |
| A JVM build reports that GeneralTools `was compiled with an incompatible version of Kotlin` (actual metadata version 2.2.0) | The JVM variant is compiled with language and API version 2.2 and requires `kotlin-stdlib` 2.2.0 | Build with Kotlin 2.1 or newer. |
| `kotlin-logging: initializing... active logger factory: Slf4jLoggerFactory` printed once on stdout | kotlin-logging 8 prints a startup line the first time any GeneralTools code that holds a logger runs | Start the JVM with `-Dkotlin-logging.logStartupMessage=false`, or set `KOTLIN_LOGGING_STARTUP_MESSAGE=false` (JS: the same name in `process.env`). |
| New WARN lines when a geometry call returns a failed `Result`; alert or log-scrape rules written for the 2.x WARN text stop matching | Each expected failure now logs one WARN line through a shared helper: `<message>: <function>: <values>`, under the unchanged logger names. | Match on logger name and level rather than text; silence `com.spartanlabs.geometry` in your backend if the volume is a problem. Exception messages are byte-identical to 2.x. |
| `Unresolved reference: Logger` / `MessageBuilder` / `IJColor` from `com.spartanlabs.logging`; `NoClassDefFoundError` at runtime | The package is removed; it printed one green ANSI line and nothing else used it | Use slf4j or kotlin-logging, or `println("\u001b[92m$message\u001b[0m")` for the same output. |
| `when (x) { is Point -> ...; is Dimensions -> ... }` over a `TwoDoubles` with no `else` reports a non-exhaustive `when` | `TwoDoubles` is no longer `sealed` | Add an `else` branch. |
| A `catch (e: IllegalArgumentException)` around `divideBy` never runs and a zero divisor leaves the value unchanged | `divideBy` returns `Result<Unit>` instead of throwing | Check `isFailure`/`getOrElse`, test the divisor first, or use `/=`; a statement `p divideBy k` still divides. |
| `NoSuchMethodError: TwoDoubles.divideBy(D)V` in code compiled against 2.x (Java callers see `divideBy-<hash>`) | The return type is `Result` (a Kotlin member returning `Result` is name-mangled on the JVM) | Recompile Kotlin callers; Java callers use `divAssign(double)` or call through Kotlin. |
| The log line `ERROR Attempted to divide a Point by zero` is gone, a zero `divideBy` logs `WARN Cannot divide by zero: Point.divideBy: (4.0, 6.0) by 0.0`, and `/=` by zero logs nothing | The failure is a returned `Result` reported at WARN, and the exception `/=` throws is its report | Adjust log filters and alerts. |
| Results of `divideBy` and `/=` can differ from 2.x in the last digit (`49.0` divided by `49.0` is exactly `1.0`) | Each component is divided directly instead of multiplied by the reciprocal | Compare with a tolerance. |
| `Point(Double.NaN, 0.0) == Point(Double.NaN, 0.0)` is now `true` (2.x `false`), and `Point(0.0, 0.0)` / `Point(-0.0, 0.0)` now hash alike (2.x equal, different hashes) | `TwoDoubles` equality is now lawful | Only NaN-versus-NaN pairs change, from unequal to equal; no pair equal in 2.x becomes unequal; code that used `p != p` to find NaN should use `containsNaN`. |
| Your own `operator fun Point.component1()`, `component2()` or `unaryMinus()` extension reports "extension is shadowed by a member" | `TwoDoubles` and `Point` now declare these operators | Delete the extension; the member returns the same values. |
| After importing the package (or `arithmetic.*`), `p += q`, `-=`, `*=` or `/=` on a `var` `Point`/`Dimensions` reports an assignment-operator ambiguity; an IDE may add the import when `p + q` does not resolve; a member `plus` added in a later release would shadow the extension | The import adds a value-returning `plus`/`minus`/`times`/`div` next to the member `plusAssign`... which makes a `var` holder ambiguous in that file, a wildcard import brings every operator in, and a later member would win over the extension | These costs apply only to files that import the package: keep the holder a `val`, write `p = p + q`, or drop the import; `import com.spartanlabs.geometry.*` does not reach the package. |
| `Unresolved reference: Square` (or `toSquare`) | `Square` is renamed `Rectangle`, and `AxisAlignedBox.toSquare()` is `toRectangle()`. No deprecated alias exists. | Rename imports and uses; `Rectangle(location, dimensions)` has the same constructor and members. |
| `Val cannot be reassigned` on `r.location = p` or `r.dimensions = d` | `Rectangle.location` and `.dimensions` are `val`. | `r.location.setTo(p)` / `r.dimensions.setTo(d)`. This copies the values into the existing part instead of adopting the instance you pass: a later change to `p` or `d` does not reach `r`, and every other holder of the old part sees the new values. |
| `Rectangle` names the wrong class, or is reported as ambiguous, in a file that also uses `java.awt.Rectangle` or another `Rectangle` | The new name is shared with those types. | Import one under an alias: `import com.spartanlabs.geometry.Rectangle as GeoRectangle`. |
| A string or log assertion on `toString()` fails: `Rectangle(location=..., dimensions=...)` where 2.x printed `Square(...)` | The class is renamed. | Update the expected text. |
