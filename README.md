# GeneralTools

A small Kotlin/JVM library of generic helpers — string/file/list utilities,
AWT-based user-action automation, an immutable RGBA `Color` value type, simple
2D geometry, and a lightweight logging wrapper.

## Maven coordinates

Published to Maven Central as `io.github.spartanlaboratories:GeneralTools:2.1.0`.

Gradle (Kotlin DSL):

```kotlin
dependencies {
    implementation("io.github.spartanlaboratories:GeneralTools:2.1.0")
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.spartanlaboratories</groupId>
    <artifactId>GeneralTools</artifactId>
    <version>2.1.0</version>
</dependency>
```

## Modules

- **`com.spartanlabs.generaltools`**
  - `Misc` — string, URL, file, list, and timing helpers.
  - `UserActions` — AWT screenshot capture and input automation.
  - `Color` — immutable RGBA value type: transforms plus hex / packed-int codecs.
- **`com.spartanlabs.geometry`**
  - `Point` / `Dimensions`, `Square`, `TwoDoubles`.
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
