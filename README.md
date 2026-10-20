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

- **`com.spartanlabs.generaltools`**
  - `Misc` — string, URL, file, list, and timing helpers.
  - `UserActions` — AWT screenshot capture and input automation.
  - `Color` — immutable RGBA value type: transforms plus hex / packed-int codecs.
- **`com.spartanlabs.geometry`**
  - `Point` / `Dimensions`, `TwoDoubles` — 2D coordinate / size pairs.
  - `AxisAlignedBox` — shared read-only box contract, implemented by `Square`
    (top-left origin) and `CenteredBox` (centre origin).
  - `Segment`, `Ray` — line primitives.
  - Vector algebra on `Point`: `dot`, `cross`, `length`, `lengthSquared`,
    `normalized()`, `projectedOnto`.
  - `segmentIntersectsSegment`, `segmentIntersectsBox`, `rayIntersectsBox`
    intersection tests.
- **`com.spartanlabs.logging`**
  - `Logger`, `MessageBuilder`.

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
- `Square(location, dimensions)` — top-left origin.
- `CenteredBox(center, halfExtents)` — centre origin; `CenteredBox.fromCorners(c1, c2)`.

`AxisAlignedBox` is the shared read-only contract (`min`, `max`, `center`,
`size`, `contains(p)`); both `Square` and `CenteredBox` implement it, and the
box-intersection functions accept either. Convert with `Square.toCenteredBox()`
/ `AxisAlignedBox.toSquare()`.

Vector algebra (extension functions on `Point`, in `Vectors.kt`):
`a dot b`, `a cross b` (2D perp-dot), `p.length`, `p.lengthSquared`,
`p.normalized(): Result<Point>` (fails on a zero-ish or NaN vector),
`v projectedOnto axis` (zero vector when `axis` is zero-ish).

Intersection functions (all return `Result`; failure on degenerate input —
zero-length segment, zero-direction ray, non-finite coordinates, negative /
non-finite `box.size`):

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

## 3.0.0 migration notes

3.0.0 is a major release. The library is now Kotlin Multiplatform, and the breaking changes listed here are clean
breaks: there is no deprecation cycle. Find your symptom in the first column.

| Symptom | Cause | Fix |
|---|---|---|
| A Maven build resolves `GeneralTools:3.0.0` but every `com.spartanlabs` import is unresolved | `GeneralTools` is now the multiplatform root: it holds Gradle module metadata and no classes, and Maven does not read it | Depend on `io.github.spartanlaboratories:GeneralTools-jvm:3.0.0`. Gradle builds need no change. |
| A Kotlin/JS or Kotlin/Wasm build reports an incompatible klib ABI version for GeneralTools | The `js` and `wasm-js` klibs are built by Kotlin 2.4 and carry its ABI version; `languageVersion` cannot lower it | Build with Kotlin 2.4 or newer. Kotlin/Wasm is Beta, so its klib compatibility guarantees are not final. |
| A JVM build reports that GeneralTools `was compiled with an incompatible version of Kotlin` (actual metadata version 2.2.0) | The JVM variant is compiled with language and API version 2.2 and requires `kotlin-stdlib` 2.2.0 | Build with Kotlin 2.1 or newer. |
