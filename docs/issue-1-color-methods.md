# Plan: finish issue #1 — Color downstream methods (tests, fixes, release)

## Header / Association

- **Covers:** `SpartanLaboratories/GeneralTools#1` — "Color Methods used
  downstream". Downstream libraries in the SpartanLabsGaming org hand-roll
  helpers (e.g. `lighten()`, RGBA→`0f..1f`) on top of this library's `Color`
  data class; the issue asks for those to live on `Color`.
- **Current state:** PR #2 ("fix `spartanlabs.generaltools.Color` issue#1
  methods used downstream", squashed as `ac20d00`, merged via `d36ccc2`) landed
  the method bodies on `master` — **one file**
  (`src/main/kotlin/generaltools/Color.kt`), **no tests, no version bump, no
  README**. PR #2 carried no `Closes #1`, so **issue #1 is still OPEN**.
- **This run finishes the issue:** component tests for the whole new `Color`
  surface, all outstanding correctness fixes (non-finite guards, hex
  round-trip, rounding consistency), a clamping factory, a version bump, and a
  repo `README.md`. The manager posts the resolution comment and closes #1
  after merge.
- **Branch:** `issue-1-color-followup`
- **Commit:** TBD
- **PR:** TBD
- **Status:** planning only.
- **Target version:** `2.1.0` (minor — additive public API over the published
  `2.0.1`). Published to Maven Central as
  `io.github.spartanlaboratories:GeneralTools`.
- **Related:** PR #2, commit `ac20d00`.

---

## 1. Context

### What `ac20d00` put on `master`

`src/main/kotlin/generaltools/Color.kt` (package `com.spartanlabs.generaltools`,
`import kotlin.math.roundToInt`):

Instance members:

| Member | Behaviour | `fraction` clamp | Rounding |
|---|---|---|---|
| `normalized(): FloatArray` | `[r,g,b,a]` each `/255f` (from 2.0.1) | — | — |
| `lightened(fraction: Double = 0.35): Color` | each RGB → `c + (255-c)*f`, alpha kept | `coerceIn(0.0,1.0)` | **`.toInt()` (truncate)** |
| `darkened(fraction: Double = 0.35): Color` | each RGB → `c * (1-f)`, alpha kept | `coerceIn(0.0,1.0)` | **`.toInt()` (truncate)** |
| `withAlpha(newAlpha: Int): Color` | `copy(alpha = newAlpha.coerceIn(0,255))` | — | — |
| `inverted(): Color` | `255 - c` per RGB, alpha kept; no output clamp | — | — |
| `grayscale(): Color` | BT.601 luma, replicated to RGB | — | `roundToInt`, clamped |
| `lerp(target: Color, fraction: Float): Color` | per-channel incl. alpha `start+(end-start)*f` | `coerceIn(0f,1f)` | `roundToInt`, clamped |
| `luminance(): Float` | `(0.299r+0.587g+0.114b)/255f` (alpha ignored) | — | — |
| `toPackedInt(): Int` | `(a<<24) or (r<<16) or (g<<8) or b` | — | — |
| `toHex(includeAlpha: Boolean = false): String` | `#RRGGBB` or **`#RRGGBBAA`**, upper-case, zero-padded | — | — |

Companion members:

| Member | Behaviour |
|---|---|
| constants `WHITE`…`TRANSPARENT` | unchanged from 2.0.1 |
| `fromNormalized(r,g,b,a=1f): Color` | each `coerceIn(0f,1f)*255` → `roundToInt` |
| `fromPackedInt(argb: Int): Color` | extracts A,R,G,B bytes |
| `fromHex(hex: String): Color` | strips `#`/`0x`; len 3 → `RGB`, len 6 → `RRGGBB`, len 8 → **`AARRGGBB`**; else `throw IllegalArgumentException` |

Also in `ac20d00`: the class KDoc was trimmed to a bare line; the file has **no
trailing newline**.

### Defects and inconsistencies to resolve this run (full scope, per the user)

1. **Non-finite `fraction` silently produces black** in `lightened` / `darkened`.
   `Double.NaN.coerceIn(0.0, 1.0)` returns `NaN` (every `<`/`>` comparison is
   false for `NaN`); `(… * NaN).toInt()` is `0`; the outer `coerceIn(0, 255)`
   keeps `0`. So `Color.RED.lightened(Double.NaN)` → `Color(0,0,0,255)`. Same for
   `±Infinity`. **`lerp` and `fromNormalized` have the identical latent bug**
   (`Float.NaN.coerceIn` → `NaN` → `roundToInt` → `0`).

2. **`toHex` / `fromHex` do not round-trip with alpha.**
   `toHex(includeAlpha = true)` emits `#RRGGBBAA` (`… red, green, blue, alpha`),
   but `fromHex`'s 8-digit branch decodes `#AARRGGBB` (`a = substring(0,2)`), as
   do `toPackedInt` / `fromPackedInt` (ARGB). The `fromHex` KDoc also wrongly
   claims `#RRGGBBAA` support.

3. **Rounding is inconsistent.** `lightened` / `darkened` truncate (`.toInt()`);
   `lerp`, `grayscale`, `fromNormalized` round (`roundToInt`).

4. **`Color` constructor does not validate channels.** `Color(-10, 300, 0)` is
   constructible; `inverted()` / `toPackedInt()` / `toHex()` then propagate or
   mis-encode out-of-range values.

### Fixes chosen (all in this run)

- **#1 → guard everywhere.** `lightened`, `darkened`, `lerp`, `fromNormalized`:
  a non-finite input is treated as `0` (identity / zero channel). Total,
  side-effect-free, consistent with the class's clamp-and-continue style.
- **#2 → align `toHex` to ARGB.** `toHex(includeAlpha = true)` emits
  `#AARRGGBB`, matching `fromHex`, `toPackedInt`, `fromPackedInt`. Both KDocs
  corrected. Hex now round-trips with alpha; a test pins it.
- **#3 → unify on `roundToInt()`.** `lightened` / `darkened` switch from
  `.toInt()` to `.roundToInt()`. `lerp` / `grayscale` / `fromNormalized` already
  round. `toPackedInt` operates on `Int` channels — no rounding involved, no
  change.
- **#4 → additive `Color.of(...)` clamping factory.** The primary constructor is
  left untouched (a `require` would throw for existing callers; silent clamping
  in the constructor would change stored values). `Color.of(r, g, b, a = 255)`
  clamps each channel into `0..255` and is the documented safe entry point for
  untrusted input. See Open decisions.

### Acceptance criteria for this run

- Every new public instance method and companion factory has Level 2 component
  coverage in the 5-level hierarchy.
- `lightened` / `darkened` / `lerp` return an equal colour for `NaN` and
  `±Infinity` fractions; `fromNormalized` treats non-finite channels as `0f`.
  Tests lock this in.
- `Color.fromHex(c.toHex(includeAlpha = true)) == c`; a test pins it.
- `lightened` / `darkened` use round-half-up (`roundToInt`) semantics; §6 test
  values reflect that.
- `Color.of` clamps out-of-range channels; a test covers it.
- `build.gradle.kts` coordinates version is `2.1.0`.
- Repo has a `README.md` (modules, build/test commands, Maven coordinates at
  `2.1.0`, `Color` quick reference).

---

## 2. Design

### Fix 1 — non-finite guards

`lightened`:

```kotlin
/**
 * This colour moved [fraction] of the way toward white, [alpha] untouched.
 *
 * @param fraction how far toward white to move, clamped to `0.0..1.0`
 *   (default `0.35`); a non-finite value (`NaN`, `±Infinity`) is treated as
 *   `0.0` — this colour unchanged.
 * @return a new [Color]; the receiver is not modified.
 */
fun lightened(fraction: Double = 0.35): Color {
    // A non-finite fraction would propagate through the channel maths and,
    // after coerceIn(0, 255), collapse every channel to 0 (black).
    val f = fraction.takeIf { it.isFinite() }?.coerceIn(0.0, 1.0) ?: 0.0
    fun towardWhite(channel: Int) =
        (channel + (255 - channel) * f).roundToInt().coerceIn(0, 255)
    return Color(towardWhite(red), towardWhite(green), towardWhite(blue), alpha)
}
```

`darkened`: identical guard; `fun towardBlack(channel: Int) = (channel * (1.0 - f)).roundToInt().coerceIn(0, 255)`.

`lerp`:

```kotlin
fun lerp(target: Color, fraction: Float): Color {
    val f = fraction.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
    fun interpolate(start: Int, end: Int) =
        (start + (end - start) * f).roundToInt().coerceIn(0, 255)
    return Color(
        interpolate(red, target.red),
        interpolate(green, target.green),
        interpolate(blue, target.blue),
        interpolate(alpha, target.alpha),
    )
}
```

Note: `+Infinity` is treated as `0f` (identity), **not** clamped to `1f`, because
`isFinite()` rejects it before `coerceIn`. This is the deliberate, documented
contract — a non-finite blend factor is meaningless, so "do nothing" is safer
than "jump to target".

`fromNormalized`:

```kotlin
fun fromNormalized(r: Float, g: Float, b: Float, a: Float = 1f): Color {
    fun channel(value: Float) =
        ((value.takeIf { it.isFinite() } ?: 0f).coerceIn(0f, 1f) * 255).roundToInt()
    return Color(channel(r), channel(g), channel(b), channel(a))
}
```

### Fix 2 — `toHex` ARGB alignment

```kotlin
/**
 * Upper-case, `#`-prefixed hex.
 *
 * @param includeAlpha when true, emits `#AARRGGBB` (alpha first — the same
 *   ARGB byte order as [toPackedInt] and [fromHex], so
 *   `Color.fromHex(toHex(includeAlpha = true))` round-trips); otherwise
 *   `#RRGGBB`.
 */
fun toHex(includeAlpha: Boolean = false): String =
    if (includeAlpha) String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    else String.format("#%02X%02X%02X", red, green, blue)
```

`fromHex` KDoc (parse unchanged — the 8-digit branch already reads `#AARRGGBB`):

```kotlin
/**
 * Parses a hex colour: `#RGB`, `#RRGGBB`, or `#AARRGGBB` (alpha first). The
 * leading `#` is optional; a `0x` prefix is also accepted.
 *
 * @param hex the string to parse.
 * @return the decoded [Color].
 * @throws IllegalArgumentException if [hex] is not one of the accepted lengths
 *   or contains non-hex digits (`NumberFormatException`, an
 *   `IllegalArgumentException`).
 */
```

### Fix 3 — rounding

Covered by the `roundToInt()` already shown in the `lightened` / `darkened` /
`lerp` bodies above. `Double.roundToInt()` / `Float.roundToInt()` round to the
nearest integer, ties toward positive infinity (round-half-up). `toPackedInt`
takes `Int` channels and does bit shifts — nothing to round, left as-is.

### Fix 4 — `Color.of` clamping factory (companion)

```kotlin
/**
 * Builds a [Color] with every channel clamped into `0..255`. Prefer this over
 * the primary constructor for values that may be out of range (parsed input,
 * arithmetic results) — the constructor performs no validation.
 */
fun of(red: Int, green: Int, blue: Int, alpha: Int = 255): Color =
    Color(
        red.coerceIn(0, 255),
        green.coerceIn(0, 255),
        blue.coerceIn(0, 255),
        alpha.coerceIn(0, 255),
    )
```

Purely additive; the primary constructor and its callers are untouched.

### Class KDoc (restore — `ac20d00` trimmed it to one line)

```kotlin
/**
 * A Red-Green-Blue-Alpha colour, each channel `0..255`.
 *
 * Immutable value type. Channels are a documented `0..255` contract — the
 * primary constructor does not validate them; use [Color.of] to clamp
 * untrusted input. Transform methods ([lightened], [darkened], [lerp],
 * [inverted], [grayscale], [withAlpha]) return a new instance.
 *
 * Hex and packed-int forms use ARGB byte order: [toHex] with alpha emits
 * `#AARRGGBB` and round-trips through [fromHex]; [toPackedInt] /
 * [fromPackedInt] likewise.
 *
 * @property red   `0..255`, not validated by the constructor.
 * @property green `0..255`, not validated by the constructor.
 * @property blue  `0..255`, not validated by the constructor.
 * @property alpha `0..255`, not validated by the constructor; defaults to `255` (opaque).
 */
```

### Trailing newline

Add the missing EOF newline while editing.

### Alternatives considered

| Alternative | Rejected because |
|---|---|
| Throw on non-finite `fraction` | Inconsistent with the clamp-and-continue style of `withAlpha` / `lerp` / `fromNormalized`; burdens render-path callers with `try`/`catch`. |
| Make `fromHex` accept `#RRGGBBAA` too | 8 hex digits can't disambiguate `AARRGGBB` from `RRGGBBAA`; one documented order (ARGB, matching the rest of the class) beats a heuristic. |
| `require(channel in 0..255)` in the primary constructor | Breaking — throws for existing 2.0.1 callers that pass unclamped values. |
| Silent clamping in the primary constructor | Changes stored values without the caller knowing; `Color(300,0,0).red` would no longer be `300`. Surprising for a `data class`. |
| Keep `lightened`/`darkened` truncating | Leaves the class internally inconsistent; round-half-up matches every other channel computation and the user asked to unify. |
| One big `fix:` commit for all code | The `toHex` output change is the only observable-behaviour change to a string consumers might snapshot; isolating it keeps it independently revertable (see §8). |

### Staging

Single PR. Commit split in §8 — the `toHex` alignment and the `Color.of`
addition are separated from the transform-hardening so each is a coherent,
revertable unit.

---

## 3. File-by-file changes

### `src/main/kotlin/generaltools/Color.kt` (edit)

- `lightened`: `isFinite()` guard; `.toInt()` → `.roundToInt()`; Level-1 comment;
  KDoc `@param`/`@return` per §2.
- `darkened`: same.
- `lerp`: `isFinite()` guard on `fraction`; KDoc note that non-finite → identity.
- `fromNormalized`: per-channel `isFinite()` guard via a local `channel(...)`
  helper.
- `toHex`: swap byte order for the `includeAlpha` branch to `alpha, red, green,
  blue`; rewrite KDoc to state `#AARRGGBB` + the round-trip guarantee.
- `fromHex`: rewrite KDoc (parse unchanged); `@throws`.
- Companion: add `of(red, green, blue, alpha = 255): Color`.
- Class KDoc: restore the accurate block from §2.
- Add trailing newline at EOF.
- Removals: none.

### `build.gradle.kts` (edit)

- Line 32: `coordinates("io.github.spartanlaboratories", "GeneralTools", "2.0.1")`
  → `"2.1.0"`. Nothing else. `tasks.test { useJUnitPlatform() }` already runs
  JUnit 5 (`MiscKtTest` imports `org.junit.jupiter.api.*` today), so `@Tag` works.

### `src/test/kotlin/com/spartanlabs/testing/component/generaltools/ColorTest.kt` (new)

- Level 2 (Isolated Component Behaviour). Package
  `com.spartanlabs.testing.component.generaltools` (mirrors production package
  `com.spartanlabs.generaltools` under a `testing.component` root). Directory
  `src/test/kotlin/com/spartanlabs/testing/component/generaltools/`.
- Existing tests sit flat under `src/test/kotlin/generaltools/` and
  `.../geometry/` with a package-vs-directory mismatch; that is **pre-existing
  and out of scope** — the new file uses the hierarchy-correct path and package.
- One class, `ColorTest`, annotated `@org.junit.jupiter.api.Tag("component")`.
- House style (`PointTest`, `SquareTest`): `private val log =
  LoggerFactory.getLogger(ColorTest::class.java)` and a `log.info("Running …")`
  at the top of each test.
- `kotlin.test` assertions; `assertContentEquals` for `FloatArray`; delta for
  individual floats.
- Behaviours in §6.

### `README.md` (new — repo root)

Short, proportional:

- Title + one-line description.
- **Maven coordinates:** `io.github.spartanlaboratories:GeneralTools:2.1.0`
  (Gradle + Maven snippets).
- **Modules:**
  - `com.spartanlabs.generaltools` — `Misc` (string/URL/file/list/timing),
    `UserActions` (AWT screenshot / input automation), `Color` (immutable RGBA
    value type: transforms + hex/packed-int codecs).
  - `com.spartanlabs.geometry` — `Point`/`Dimensions`, `Square`, `TwoDoubles`.
  - `com.spartanlabs.logging` — `Logger`, `MessageBuilder`.
- **Build & test:** `./gradlew build`, `./gradlew test`.
- **`Color` quick reference:** constants; `normalized`, `lightened`, `darkened`,
  `withAlpha`, `inverted`, `grayscale`, `lerp`, `luminance`, `toPackedInt`,
  `toHex`; factories `of`, `fromNormalized`, `fromPackedInt`, `fromHex`. One
  code example, including `Color.of` for clamping and the `#AARRGGBB` hex form.
- Required by the "Repository READMEs — keep current" rule: new public API on a
  Maven Central library, and no README exists.

### `docs/issue-1-color-methods.md` (this file)

- Committed in the **same commit as the first code change** so `git log
  --follow` binds plan to implementation.

---

## 4. Documentation impact

| Ring | Touched? | Action |
|---|---|---|
| 1 — In-editor | Yes (minor) | Level-1 comment above each `isFinite()` guard. |
| 2 — Component / KDoc | **Yes** | `lightened` / `darkened` / `lerp` / `fromNormalized` guard docs; `toHex` + `fromHex` rewritten for ARGB and round-trip; new `Color.of` KDoc; restored class KDoc; verify `@param`/`@return`/`@throws` completeness on `withAlpha`, `grayscale`, `luminance`, `toPackedInt`, `fromPackedInt` while in the file. |
| 3 — Boundary / protocol | No | `Color` is not serialized by this library; `toHex` / `toPackedInt` are local conversions. The ARGB convention is captured in KDoc. |
| 4 — Architecture | No | No topology change. |

Plus **README** (`README.md`, new) — §3.

---

## 5. (reserved)

---

## 6. Test plan (5-level hierarchy)

### Level 2 — Isolated Component Behaviour  *(the whole of this run's coverage)*

- **Package:** `com.spartanlabs.testing.component.generaltools`
- **Path:** `src/test/kotlin/com/spartanlabs/testing/component/generaltools/ColorTest.kt`
- **Class:** `ColorTest`, `@Tag("component")`
- **Mocks:** none — `Color` is pure.

All expected values below are computed for the **post-fix** implementation
(`roundToInt`, ARGB hex, guards). Rows tagged **(round)** change vs the shipped
truncating code; **(guard)** rows are new behaviour from Fix 1; **(ARGB)** rows
are new from Fix 2.

#### `normalized()`
| Test | Assert |
|---|---|
| maps channels `0..255` → `0f..1f` | `Color(255,0,0,255).normalized()` content-equals `[1f,0f,0f,1f]` |
| converts a partial channel | `Color(0,0,0,128).normalized()[3]` ≈ `0.50196f` (δ 1e-4) |

#### `lightened(fraction = 0.35)`
| Test | Assert |
|---|---|
| default fraction toward white | `Color(0,0,0).lightened()` == `Color(89,89,89)` (`round(255*0.35)=round(89.25)=89`) |
| **(round)** half rounds up | `Color(0,0,0).lightened(0.5)` == `Color(128,128,128)` (`round(127.5)=128`; truncation gave `127`) |
| alpha preserved | `Color(0,0,0,10).lightened().alpha` == `10` |
| `fraction = 1.0` → white RGB, original alpha | `Color(1,2,3,50).lightened(1.0)` == `Color(255,255,255,50)` |
| `fraction = 0.0` → equal | `Color(10,20,30,40).lightened(0.0)` == `Color(10,20,30,40)` |
| clamps `fraction > 1` | `Color(10,20,30).lightened(5.0)` == `Color(10,20,30).lightened(1.0)` |
| clamps `fraction < 0` | `Color(10,20,30).lightened(-1.0)` == `Color(10,20,30)` |
| **(guard)** `NaN` → identity | `Color(10,20,30,40).lightened(Double.NaN)` == `Color(10,20,30,40)` |
| **(guard)** `±Infinity` → identity | `lightened(Double.POSITIVE_INFINITY)` and `lightened(Double.NEGATIVE_INFINITY)` both == `Color(10,20,30,40)` |
| output channels in `0..255` | `Color(250,5,128).lightened(1.0)` — every channel in `0..255` |
| receiver not mutated | `val c = Color(0,0,0); c.lightened(); assertEquals(Color(0,0,0), c)` |

#### `darkened(fraction = 0.35)`
| Test | Assert |
|---|---|
| default fraction toward black | `Color(200,200,200).darkened()` == `Color(130,130,130)` (`round(200*0.65)=130`) |
| **(round)** half rounds up | `Color(255,255,255).darkened(0.5)` == `Color(128,128,128)` (`round(255*0.5)=round(127.5)=128`; truncation gave `127`) |
| alpha preserved | `Color(255,255,255,10).darkened().alpha` == `10` |
| `fraction = 1.0` → black RGB, original alpha | `Color(1,2,3,50).darkened(1.0)` == `Color(0,0,0,50)` |
| `fraction = 0.0` → equal | `Color(10,20,30).darkened(0.0)` == `Color(10,20,30)` |
| clamps out-of-range fraction | `darkened(5.0)` == `darkened(1.0)`; `darkened(-1.0)` == identity |
| **(guard)** `NaN` / `±Infinity` → identity | `Color(10,20,30,40).darkened(Double.NaN)` == `Color(10,20,30,40)` (and both infinities) |

#### `withAlpha(newAlpha)`
| Test | Assert |
|---|---|
| replaces alpha, keeps RGB | `Color(1,2,3,4).withAlpha(200)` == `Color(1,2,3,200)` |
| clamps above 255 | `Color(1,2,3).withAlpha(999).alpha` == `255` |
| clamps below 0 | `Color(1,2,3).withAlpha(-5).alpha` == `0` |

#### `inverted()`
| Test | Assert |
|---|---|
| inverts RGB, keeps alpha | `Color(0,0,0,10).inverted()` == `Color(255,255,255,10)` |
| involution for in-range colours | `Color(12,34,56,78).inverted().inverted()` == `Color(12,34,56,78)` |

#### `grayscale()`
| Test | Assert |
|---|---|
| BT.601 weight on pure red | `Color(255,0,0).grayscale()` == `Color(76,76,76)` (`round(0.299*255)=round(76.245)=76`) |
| BT.601 weight on pure green | `Color(0,255,0).grayscale()` == `Color(150,150,150)` (`round(0.587*255)=round(149.685)=150`) |
| white unchanged, alpha preserved | `Color(255,255,255,42).grayscale()` == `Color(255,255,255,42)` |

#### `lerp(target, fraction)`
| Test | Assert |
|---|---|
| `fraction = 0f` → this | `a.lerp(b, 0f)` == `a` |
| `fraction = 1f` → target | `a.lerp(b, 1f)` == `b` |
| midpoint black→white | `Color(0,0,0).lerp(Color(255,255,255), 0.5f)` == `Color(128,128,128,255)` (`round(127.5)=128`) |
| interpolates alpha too | `Color(0,0,0,0).lerp(Color(0,0,0,255), 0.5f).alpha` == `128` |
| clamps fraction | `a.lerp(b, 2f)` == `a.lerp(b, 1f)`; `a.lerp(b, -1f)` == `a` |
| **(guard)** `NaN` → identity | `Color(1,2,3,4).lerp(Color(5,6,7,8), Float.NaN)` == `Color(1,2,3,4)` |
| **(guard)** `±Infinity` → identity (documented: not clamped to target) | `Color(1,2,3,4).lerp(Color(5,6,7,8), Float.POSITIVE_INFINITY)` == `Color(1,2,3,4)` |

#### `luminance()`
| Test | Assert |
|---|---|
| white → `1.0f` | `Color.WHITE.luminance()` ≈ `1.0f` (δ 1e-4) |
| black → `0.0f` | `Color.BLACK.luminance()` == `0.0f` |
| pure red weight | `Color(255,0,0).luminance()` ≈ `0.299f` (δ 1e-4) |
| ignores alpha | `Color(255,0,0,0).luminance()` == `Color(255,0,0,255).luminance()` |

#### `toPackedInt()` / `Color.fromPackedInt(argb)`
| Test | Assert |
|---|---|
| transparent black → `0` | `Color(0,0,0,0).toPackedInt()` == `0` |
| opaque white → `0xFFFFFFFF` | `Color(255,255,255,255).toPackedInt()` == `-1` |
| channel placement | `Color(1,2,3,4).toPackedInt()` == `(4 shl 24) or (1 shl 16) or (2 shl 8) or 3` |
| `fromPackedInt` inverts `toPackedInt` | `Color.fromPackedInt(Color(1,2,3,4).toPackedInt())` == `Color(1,2,3,4)` |
| `fromPackedInt(-1)` | == `Color(255,255,255,255)` |

#### `toHex(includeAlpha)`  **(ARGB)**
| Test | Assert |
|---|---|
| RGB form, upper-case | `Color(255,87,51).toHex()` == `"#FF5733"` |
| zero-padded | `Color(1,2,3).toHex()` == `"#010203"` |
| alpha form is `#AARRGGBB` | `Color(255,87,51,128).toHex(includeAlpha = true)` == `"#80FF5733"` (was `"#FF573380"`) |
| **round-trips with alpha** | `Color.fromHex(Color(255,87,51,128).toHex(includeAlpha = true))` == `Color(255,87,51,128)` |
| round-trips without alpha | `Color.fromHex(Color(255,87,51).toHex())` == `Color(255,87,51)` (alpha defaults `255`) |

#### Companion constants
| Test | Assert |
|---|---|
| representative constants | `Color.WHITE` == `Color(255,255,255,255)`; `Color.TRANSPARENT` == `Color(0,0,0,0)`; `Color.RED` == `Color(255,0,0,255)` |

#### `Color.of(red, green, blue, alpha = 255)`  **(new)**
| Test | Assert |
|---|---|
| in-range passes through | `Color.of(1,2,3,4)` == `Color(1,2,3,4)` |
| default alpha opaque | `Color.of(1,2,3)` == `Color(1,2,3,255)` |
| clamps high and low | `Color.of(300,-5,128,999)` == `Color(255,0,128,255)` |

#### `Color.fromNormalized(r,g,b,a = 1f)`
| Test | Assert |
|---|---|
| default alpha opaque | `Color.fromNormalized(1f,0f,0f)` == `Color(255,0,0,255)` |
| all-zero incl. explicit alpha | `Color.fromNormalized(0f,0f,0f,0f)` == `Color(0,0,0,0)` |
| clamps out-of-range floats | `Color.fromNormalized(2f,-1f,0.5f)` == `Color(255,0,128,255)` (`round(0.5*255)=round(127.5)=128`) |
| **(guard)** non-finite channel → `0f` | `Color.fromNormalized(Float.NaN, 0f, 0f)` == `Color(0,0,0,255)`; `Color.fromNormalized(Float.POSITIVE_INFINITY, 0f, 0f)` == `Color(0,0,0,255)` |

#### `Color.fromHex(hex)`
| Test | Assert |
|---|---|
| `#RRGGBB` | `Color.fromHex("#FF5733")` == `Color(255,87,51)` |
| `#RGB` shorthand expands | `Color.fromHex("F53")` == `Color(255,85,51)` |
| `#AARRGGBB` (8-digit, alpha first) | `Color.fromHex("#80FF5733")` == `Color(255,87,51,128)` |
| `0x` prefix accepted | `Color.fromHex("0xFF5733")` == `Color(255,87,51)` |
| no prefix accepted | `Color.fromHex("FF5733")` == `Color(255,87,51)` |
| invalid length throws | `assertFailsWith<IllegalArgumentException> { Color.fromHex("#FFFF") }` |
| non-hex digits throw | `assertFailsWith<IllegalArgumentException> { Color.fromHex("#GGGGGG") }` |

### Levels 1, 3, 4, 5

- **Level 1 (gating):** no separate files; executor runs `./gradlew test` (or the
  `ColorTest` class) before committing — `ColorTest` *is* the WIP gate.
- **Level 3 (integration):** N/A — no external interface.
- **Level 4a (deterministic):** the tests above are pure input→output maps and
  would qualify, but one Level 2 `ColorTest` is proportional for a single value
  class with no external boundary. Do not create an empty
  `testing.deterministic` package.
- **Level 4b / 4c:** N/A — no E2E flow, no perf/security surface.
- **Level 5 (UAT):** whether `lightened` / `grayscale` / `lerp` *look* right is a
  perceptual judgement — a visual check in a downstream renderer
  (`SpartanLabsGaming/GameGraphics`) on adoption. Not automatable, not a blocker;
  noted for the downstream follow-up.

### What cannot be tested automatically

Perceptual colour correctness (Level 5, downstream). Everything else here is
deterministic and covered.

---

## 7. Risks & edge cases

- **Nothing here is a breaking change against a published release.** Maven
  Central's latest is **`2.0.1`**, which contains only
  `Color(red, green, blue, alpha = 255)`, `normalized()`, and the colour
  constants. Every method touched this run — `lightened`, `darkened`, `withAlpha`,
  `inverted`, `grayscale`, `lerp`, `luminance`, `toPackedInt`, `toHex`,
  `fromNormalized`, `fromPackedInt`, `fromHex` — was added by the unreleased PR #2
  (`ac20d00`) and has **never shipped**. So changing `toHex`'s output byte order,
  switching `lightened`/`darkened` to `roundToInt`, and adding the non-finite
  guards are all changes to code no consumer has ever depended on. Against `2.0.1`
  this run is **purely additive** (new methods + `Color.of`); nothing removed,
  no 2.0.1 signature altered. Hence `2.1.0` (minor) is correct.
- **`normalized()` already shipped (2.0.1).** Do not re-declare it; add tests
  only.
- **`FloatArray` has no structural `equals`.** Tests use `assertContentEquals` /
  delta. Pre-existing, flagged for the executor.
- **`toHex` output string changed** (`#RRGGBBAA` → `#AARRGGBB` for the alpha
  form). Only matters to code that snapshot-tested PR #2's unreleased output —
  none exists. The change makes `toHex`/`fromHex`/`toPackedInt` mutually
  consistent.
- **`lerp` / `lightened` / `darkened` non-finite `fraction` now returns identity,
  including for `+Infinity`.** Documented contract, not "clamp to max". Tests pin
  it so a future reader does not "fix" it back.
- **`Color.of` vs primary constructor.** Two entry points now. README and KDoc
  steer untrusted input to `Color.of`; the constructor stays a documented
  `0..255` contract. `inverted()` / `toPackedInt()` / `toHex()` still assume
  in-range channels — unchanged behaviour, now clearly a caller responsibility.
- **`roundToInt()` ties round toward `+∞`.** `Double`/`Float` `.roundToInt()`
  from `kotlin.math` (already imported). Test values in §6 assume this.
- **`@Tag("component")` is advisory** — no Gradle task filters by tag yet, so the
  test runs in the single `test` task. Per-level task wiring is a follow-up.
- **Local `master`** now contains `d36ccc2` / `ac20d00`; branch off updated
  `master`.
- **Pre-existing working-tree noise:** `.idea/misc.xml`, `.idea/vcs.xml`,
  `.idea/workspace.xml` are modified — keep them **out** of this branch's
  commits (stash/discard, or commit separately). Follow-up: add `.idea/` to
  `.gitignore`.
- **Cross-repo.** After `2.1.0` releases, `SpartanLabsGaming/GameGraphics` (and
  possibly `MyGameTools` / `MyGameServer`) should drop hand-rolled colour helpers
  for `Color.*`. Separate issues in those repos; confirm current owners with
  `gh repo view <repo>`.
- **Concurrency / performance.** None — pure functions, no shared state.

---

## 8. Version control

- **Branch:** `issue-1-color-followup` off updated `master`.
- **Commits (conventional-commit prefixes):**

  1. **`fix: guard Color transforms against non-finite input and round to nearest`**
     - `Color.kt`: `isFinite()` guards in `lightened`, `darkened`, `lerp`,
       `fromNormalized`; `.toInt()` → `.roundToInt()` in `lightened` / `darkened`;
       restored class KDoc; trailing newline
     - `docs/issue-1-color-methods.md` (this plan — same commit, per convention)
  2. **`fix: align Color.toHex(includeAlpha) to ARGB so hex round-trips`**
     - `Color.kt`: `toHex` byte order + KDoc; `fromHex` KDoc
  3. **`feat: add Color.of clamping factory`**
     - `Color.kt`: companion `of(...)`
  4. **`test: add Color component test suite`**
     - `src/test/kotlin/com/spartanlabs/testing/component/generaltools/ColorTest.kt`
  5. **`docs: add repository README`**
     - `README.md`
  6. **`build: bump version to 2.1.0`**
     - `build.gradle.kts`

  Rationale for the split: commit 1 is transform-hardening (behaviour only
  observable to code that fed in bad input); commit 2 is the one change to an
  observable output *string*; commit 3 is additive API. Each is independently
  revertable and reviewable. If the team prefers fewer commits, 1–3 can be
  squashed into a single `fix:` — but keep 4/5/6 separate.

- **Trailer:** the repo has no strict convention (PR #2 merged with a default
  message). Add `Refs: SpartanLaboratories/GeneralTools#1` to each commit body;
  do **not** add `Closes #1` (the manager closes the issue with a resolution
  comment after merge). Include the tooling's `Co-Authored-By` /
  `Generated-with` trailers if that is your standard.
- **After merge:** fill `Commit:` / `PR:` in the header with the real squash SHA
  and PR number.
- **Post-merge (manager):** comment on issue #1 linking PR #2 and this PR, close
  #1, cut the `2.1.0` GitHub release (the `Publish` workflow fires on release
  creation).

---

## 9. Open decisions

**None block execution.** The scope items are resolved as follows:

| Item | Resolution |
|---|---|
| Version number | `2.1.0` (user-confirmed). Minor: purely additive over the published `2.0.1`. |
| #1 non-finite guard scope | All of `lightened`, `darkened`, `lerp`, `fromNormalized`. Non-finite → `0` / identity (documented, incl. `+Infinity`). |
| #2 hex round-trip | Align `toHex(includeAlpha=true)` to `#AARRGGBB` (ARGB, matching `toPackedInt` / `fromHex`). `fromHex` parse unchanged; both KDocs corrected. |
| #3 rounding | Unify on `roundToInt()`; `lightened` / `darkened` stop truncating. `toPackedInt` unaffected (integer bit ops). |
| #4 constructor validation | **Option (b): add `Color.of(...)` clamping factory**; leave the primary constructor as a documented `0..255` contract. A `require` in the constructor is breaking; silent constructor clamping hides data loss. `Color.of` is additive and the recommended entry point for untrusted input. |
| Commit split | Recommended 6 commits (§8); squashing 1–3 into one `fix:` is acceptable if the team prefers. Not a blocking decision. |

If the reviewer wants `fromHex` to *also* accept the web-standard `#RRGGBBAA`
form, that is a genuine product call — but it is **not** required for the
round-trip guarantee (which ARGB alignment already delivers) and 8-digit input
is ambiguous. Recommend deferring to a separate `fromHexRgba` helper if ever
wanted; not in scope here.

---

## 10. Sequencing & follow-ups

1. Branch off updated `master`; land the six commits in §8 as one PR.
2. Manager: comment on and close issue #1; cut the `2.1.0` release.
3. **Follow-up (downstream):** issues on `SpartanLabsGaming/GameGraphics` (and
   any of `MyGameTools` / `MyGameServer` carrying colour maths) to adopt
   `Color.*` and delete duplicates, once `2.1.0` is on Maven Central.
4. **Follow-up (infra):** migrate the existing flat tests under
   `src/test/kotlin/generaltools/` and `.../geometry/` into the 5-level
   `testing.*` layout, fix the package-vs-directory mismatch, and bind per-level
   Gradle test tasks / JUnit tags. Add `.idea/` to `.gitignore`.
5. **Follow-up (optional):** `fromHexRgba` web-convention helper; making the
   primary constructor `internal` in a future major so `Color.of` /
   `fromNormalized` / `fromHex` are the only entry points.
