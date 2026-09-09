# GeneralTools

A small Kotlin/JVM library of generic helpers — string/file/list utilities,
AWT-based user-action automation, an immutable RGBA `Color` value type, simple
2D geometry, and a lightweight logging wrapper.

## Maven coordinates

Published to Maven Central as `io.github.spartanlaboratories:GeneralTools:2.2.0`.

Gradle (Kotlin DSL):

```kotlin
dependencies {
    implementation("io.github.spartanlaboratories:GeneralTools:2.2.0")
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.spartanlaboratories</groupId>
    <artifactId>GeneralTools</artifactId>
    <version>2.2.0</version>
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

```sh
./gradlew build
./gradlew test
```

Tests run on the JUnit 5 platform with `kotlin.test`.

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
