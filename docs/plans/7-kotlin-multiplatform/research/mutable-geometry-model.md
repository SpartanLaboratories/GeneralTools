# The mutable geometry model for GeneralTools 3.0.0

- **Issues:** [#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7) (Kotlin Multiplatform) and
  [#8](https://github.com/SpartanLaboratories/GeneralTools/issues/8) (read-only views for `Point` and `Square`).
- **Date read:** 2026-10-18. Repo baseline: branch `feat/issue-7-kotlin-multiplatform` @ `7913fc1`; `src/main/kotlin/geometry/*` and the geometry tests as on `master` @ `17d30bc`.
- **Pinned outside sources:** Kool `v0.19.0`; libGDX `1.14.2`; androidx `62841bbfbf6d1f0a79ca386cd8ce71182868ef28` (Compose `ui-geometry`); korlibs `94e645c59f3c54a268cba01d566e0a0b4654207a`; OPENRNDR `v0.5.0`; openjdk/jfx `cb56d4c90fe7a43079688b92c54c7d2b364c7232`; AOSP `android-15.0.0_r1`; Java SE 21 API; Kotlin documentation pages as read on 2026-10-18. Read-only reads of MyGameTools @ `dc5214f`, GameGraphics @ `8b428a4`, MyGameServer and SpartanGraphics @ `50b61e2` supply the usage counts.
- **Method limits:** nothing was compiled or run. Language behaviour comes from the Kotlin documentation and specification; library behaviour from source at the pinned tags. Everything that needs a compiler is listed in *Gaps* with the smallest check.
- **Related findings (reused, not repeated):** [`immutable-geometry-model.md`](immutable-geometry-model.md) (equality, the opt-in tier, augmented-assignment rule, comparable libraries), [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) (mangling of members that return `Result`, the marker shape, ABI dumps), [`kotlin-logging-adoption.md`](kotlin-logging-adoption.md) (the failure helper), [`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md) (`jvm`/`js`/`wasmJs`).

## 1. Question

With `Point` and `Dimensions` mutable, what shape do `TwoDoubles.divideBy`, the in-place operators, the renamed `Square` and its immutable copy take in GeneralTools 3.0.0?

An answer must meet these requirements, all plain facts about the work:

- `Point` and `Dimensions` stay mutable as in 2.x, with every in-place member: `var x`, `y`, `width`, `height`, `setTo`, `modBy`, `amplifyBy`, `plusAssign`, `minusAssign`, `timesAssign`, `divAssign`.
- `TwoDoubles` is no longer `sealed`; subclassing it is Experimental, gated by `@SubclassOptInRequired(ExperimentalForInheritanceGeneralToolsApi::class)` (an ERROR-level `@RequiresOptIn` marker with `@Target()`).
- `divideBy` returns a `Result` and logs at WARN through `internal fun <T> KLogger.failure(message: String): Result<T>` instead of throwing.
- Equality: same concrete class; IEEE-equal components except that NaN equals NaN; `-0.0` hashes like `0.0`, every NaN alike; pairs equal in 2.x stay equal. `toString` keeps each platform's own number formatting.
- `Square` gets a new name; its `location` and `dimensions` references become `val`; the `Point` and `Dimensions` it holds stay mutable. The renamed type provides a method returning an immutable copy as a new immutable type with a stability tier of its own.
- The 3.0.0 breaks are exactly: the multiplatform move; `TwoDoubles` unsealed; `divideBy` returns a `Result`; `evaluateList` takes `(T) -> Boolean`; `com.spartanlabs.logging` removed; `Square` renamed with `val` references; the equality contract change; `Color`'s constructor validates; `trimIfLongerThan` throws `IndexOutOfBoundsException`; the `capitalizeEveryWord` comma fix. Clean breaks, no deprecation cycle. Anything else is source-compatible; an option that needs another break says so. Additive members and types are not breaks.
- Library-design rules: mechanism in the library, policy with the consumer; domain types extend by inheritance with invariant-keeping members `final`/`internal`; every public seam sits in one tier (Stable Core, Supported Extension with `@SupportedExtension`, Experimental behind `@RequiresOptIn`).
- Common code for `jvm`, `js`, `wasmJs` at `languageVersion`/`apiVersion` 2.2; geometry sits on game hot paths.

## 2. Bottom line

- **`divideBy` divides in place and returns `Result<Unit>`**, as a `final` member of the abstract `TwoDoubles`. It is the same verb family as `setTo`/`modBy`/`amplifyBy` (mutate, return nothing of the value), a failure leaves both components unchanged and logs WARN, and a discarded result is harmless: the success path has already happened and the failure path has already been logged. It needs no factory hook, no covariant `Result` override (never compiled in any comparable code found) and no change to the name.
- **`/=` on zero is a call.** Throwing (as 2.x does) is the only option that leaves `/=` exactly as it is today, stays inside the listed break set and matches Kotlin's own operator-throws/named-method-returns-failure pairing; logging and leaving the value unchanged gives one zero policy for the type; IEEE division is the libGDX/Kool/kotlin-math norm but silently produces Infinity/NaN positions. Recommended: throw, without logging, and say so in the KDoc. No downstream repository calls `/=` or `divideBy`.
- **No non-assigning operators (`plus`, `minus`, `times`, `div`) on `Point`/`Dimensions` in 3.0.0.** Kotlin reports `a += b` as ambiguous when `plus` returns a subtype of the type of a `var` `a`, so a `Point`-returning `plus` breaks every `var` holder that uses `+=` and cannot be added later in a minor. No `var` holder with compound assignment exists in GeneralTools, MyGameTools, GameGraphics or MyGameServer, but the library's external consumers are unknown. `unaryMinus` has no assign counterpart and can be added at any time.
- **The immutable copy is a final class of four `Double`s (`x`, `y`, `width`, `height`) that implements `AxisAlignedBox`**, with hand-written `equals`/`hashCode`/`toString` that follow the equality contract (a generated data class would split `0.0` from `-0.0`). Origin-plus-size storage keeps the round trip exact; JavaFX `Rectangle2D`, korlibs `Rectangle` and OPENRNDR `Rectangle` store the same way. `AxisAlignedBox` gains defaulted primitive accessors (`minX`, `minY`, `maxX`, `maxY`, `width`, `height`) so the intersection functions and the immutable box stop allocating per read.
- **Names:** `Rectangle` / `ImmutableRectangle`, conversion `toImmutable()`; runner-up `TopLeftBox` / `ImmutableTopLeftBox`. SpartanGraphics already plans `Rect`, `Rectangle` and `Box` of its own, so every plain geometric noun is taken there; the choice is a call.
- **Tier of the immutable type: Stable Core, medium confidence.** The need is evidenced (issue #8 and three defensive-copy sites in MyGameTools); the shape follows four independent libraries; an Experimental gate would force the opt-in onto the consumers who are meant to hold the type in public signatures.
- **Read-only views:** `AxisAlignedBox` plus the immutable copy answers issue #8 without new interfaces. `Square.min` and `Square.size` return the live parts while `max` and `center` return copies; making them copies is the only way to make an `AxisAlignedBox`-typed handle sound, and is a named further behavioural change that no known caller depends on. `ReadOnlyPoint`/`ReadOnlySquare` interfaces are not recommended.

## 3. Findings

Evidence tiers: **consensus** (language documentation/specification, or several independent mature sources), **practice** (what mature libraries visibly do), **opinion** (one source), **source-read** (read at the pinned version, not executed), **reasoned** (my inference from the repo's code), **computed** (arithmetic run here).

### 3.1 `divideBy` on a mutable type

**What the repo has.** `setTo`, `modBy` and `amplifyBy` mutate and return `Unit`; `divideBy` is `amplifyBy(1/magnitude)` behind a zero check that logs ERROR and throws `IllegalArgumentException`; `divAssign` delegates to it ([`TwoDoubles`](../../../../src/main/kotlin/geometry/TwoDoubles.kt)). The repo's seven other `Result`-returning functions (`distanceFrom`, `normalized()`, `asRay()`, `unit()`, `segmentIntersectsSegment`, `segmentIntersectsBox`, `rayIntersectsBox`) all keep plain names ([`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) F2.1).

| shape | signature | in-place verb family | discarded result | cost |
|---|---|---|---|---|
| **(a)** in place, `Result<Unit>` | `final infix fun divideBy(magnitude: Double): Result<Unit>` | consistent (mutates) | success already applied; failure already logged | none beyond the `Result` |
| **(b)** in place, receiver inside the `Result` | `open … : Result<TwoDoubles>` on the base, `override … : Result<Point>` / `Result<Dimensions>` | consistent (mutates) | same as (a) | a covariant override of a `Result` return type that no comparable code was found to use; Kotlin's guidelines call a narrowed return type source-compatible but binary-incompatible when it later changes ([claim 13](#7-load-bearing-claims)); chaining value is small because no other verb in the repo chains |
| **(c)** receiver untouched, new instance | `fun divideBy(magnitude: Double): Result<TwoDoubles>` plus a factory hook | breaks the family: `p divideBy 2.0` as a statement compiles and does nothing | silent no-op | one allocation per call; an abstract `create` on an open class that every consumer subclass must implement; `/=` still needs an in-place path |

- **(a) is the consistent shape** (reasoned). Kotlin's own naming convention separates the in-place verb from the one that returns a new value (`sorted…` returns "a new collection", in-place sorting is a separate function; [claim 17](#7-load-bearing-claims)); the repo's verbs are all in-place. A new-instance form would want its own name (`dividedBy`) in a minor, additively.
- **A discarded `Result` is benign in (a) and (b), harmful in (c)** (reasoned, with consensus on the tooling gap). No mechanism usable at `languageVersion`/`apiVersion` 2.2 warns about a dropped `Result` ([`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) F2.2). With (a), `p divideBy 2.0` written as a statement keeps its 2.x success behaviour; only a zero divisor differs (no exception; a WARN line; the value is unchanged). The hazard recorded for the immutable model ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.4, "compiles and does nothing") does not arise.
- **JVM name mangling (source-read).** A member returning an inline class such as `Result` is name-mangled on the JVM (`divideBy-<hash>` with descriptor `(D)Ljava/lang/Object;`, as `distanceFrom` already is; [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) F2.1, [claim 18](#7-load-bearing-claims)). Kotlin callers are unaffected; Java callers see the mangled name; the dump line for `divideBy (D)V` is replaced by the mangled line. `Result<Unit>` allocates nothing on success (`Unit` is a singleton); a failure allocates the exception.
- **Naming (reasoned).** The Kotlin guideline's `Catching` suffix names a *pair of overloads*, one throwing and one wrapping (`run`/`runCatching`); see [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) F2.1. If `/=` keeps throwing, a throwing sibling exists, but it is spelled as an operator, not as a same-named function; `get(i)` throwing next to `getOrNull(i)` is the stdlib shape of "operator throws, named function reports". Renaming the `Result`-returning function `divideByCatching` would be a rename beyond the listed break. Keep `divideBy`.
- **Arithmetic (computed + reasoned).** Today's body multiplies by the reciprocal. `49.0 * (1.0 / 49.0)` is `0.9999999999999999` (IEEE double; Python 3 shows it, and the JVM uses the same arithmetic), whereas `49.0 / 49.0` is `1.0`; 8 of the integers 1..199 misbehave this way (49, 98, 103, 107, 161, 187, 196, 197). A subnormal divisor `5e-324` has reciprocal `Infinity`, so the reciprocal form turns a legitimate division into `Infinity` while true division gives `1.0`. Kool's `divAssign` also multiplies by the reciprocal (`mul(1.0 / div)`; [claim 4](#7-load-bearing-claims)). True division costs two divides instead of two multiplies, negligible against everything else on a frame. No known caller depends on the last-ulp results (no downstream code calls `divideBy` or `/=`).
- **What counts as zero.** `magnitude == 0.0` is IEEE, so `-0.0` fails and `NaN` passes (a NaN divisor yields NaN components). Widening the failure set to non-finite divisors would be a further behavioural change; not recommended.

### 3.2 `/=` (`divAssign`) when the divisor is zero

`divAssign` must return `Unit`: "Make sure its return type is `Unit`, and report an error otherwise" ([claim 1](#7-load-bearing-claims)). It therefore cannot return the `Result`.

| option | `p /= 0.0` | relative to 2.x | single zero policy with `divideBy` | notes |
|---|---|---|---|---|
| **throw** (`IllegalArgumentException("Cannot divide by zero")`) | exception, value unchanged | **identical** to 2.x (apart from the dropped ERROR log) | no: two spellings, two policies | the existing `divAssign throws when dividing by zero` test stays green; matches Kotlin `Int / 0` and the stdlib "operator throws" shape |
| **log WARN, value unchanged** (`operator fun divAssign(m: Double) { divideBy(m) }`) | no exception, WARN, unchanged | behaviour change (unlisted unless read as part of the `divideBy` break) | yes | libGDX `Vector2.nor()` also leaves a zero-length vector unchanged ([claim 5](#7-load-bearing-claims)); a failing call builds an exception and, on the JVM, captures a stack trace, so a loop that divides by zero repeatedly is slow and floods the log |
| **IEEE, no check** (`amplifyBy(1/m)` semantics) | components become `±Infinity`/`NaN` | behaviour change | no | libGDX has no `div` (callers use `scl(1/x)`), Kool `divAssign` is `mul(1.0 / div)`, kotlin-math `div` is plain division ([claims 4, 5](#7-load-bearing-claims), [`immutable-geometry-model.md`](immutable-geometry-model.md) §3.4); the library's own `normalized()` and `distanceFrom` treat degenerate input as an expected failure, so a silent `NaN` position contradicts them |
| **drop the operator** | does not compile | **further source break** | n/a | not recommended; `divAssign` has no known callers, but it is part of the listed in-place surface that must stay |

- **What a consumer expects (practice).** Game-library users coming from libGDX/Kool expect IEEE; users coming from Kotlin integers or from 2.x expect an exception. No comparable type returns a `Result` from an operator.
- **No downstream call exists.** `grep` for `divideBy` and `divAssign` over MyGameTools, GameGraphics, MyGameServer and WebTools finds none; the only uses of `/=` on a `Point` are in GeneralTools' own `PointTest`.
- **Log-and-throw** (what 2.x does: log ERROR, then throw) reports the same fault twice; if `/=` throws, the exception is the report (reasoned).

### 3.3 Non-assigning operators (`plus`, `minus`, `times`, `div`, `unaryMinus`)

**The rule (consensus).** Kotlin documents the compiler's steps for `a += b`: if `plusAssign` is available and "the corresponding binary function … is available too, `a` is a mutable variable, and the return type of `plus` is a subtype of the type of `a`, report an error (ambiguity)"; otherwise it generates `a = a + b` "(this includes a type check …)" ([claim 1](#7-load-bearing-claims)). The specification states it as two expansions: "If both expansion variants result in correctly resolved and inferred code, this should be reported as an operator overloading ambiguity. If only one of the expansion variants can be resolved correctly, this variant is picked" ([claim 2](#7-load-bearing-claims)). Consequences, derived (reasoned from those two texts):

| holder of the left operand | `plus` returns `Point` | `plus` returns a non-subtype of `Point` | no `plus` |
|---|---|---|---|
| `val p: Point` | `plusAssign` (`p = …` is not assignable, so only one expansion resolves) | `plusAssign` | `plusAssign` |
| `var p: Point` | **ambiguity error** | `plusAssign` (`p = p + q` fails the type check) | `plusAssign` |
| `var p: TwoDoubles` | **ambiguity error** (`Point` is a subtype) | `plusAssign` | `plusAssign` |

- **How Kool avoids it (source-read).** `plus`, `minus`, `times`, `div` are declared on the read-only base `Vec2d` and return `Vec2d`; `MutableVec2d : Vec2d` adds `plusAssign` and friends ([claim 4](#7-load-bearing-claims)). For `var m: MutableVec2d`, `m + x` is a `Vec2d`, not a `MutableVec2d`, so only `plusAssign` resolves. The price: `m = m + x` does not compile for a `MutableVec2d`. GeneralTools has no read-only supertype of `Point` to copy this arrangement (see §3.6), and an immutable point type would add a new type pair.
- **Count of affected holders (repo facts, grep).** `var` holders of `Point`/`Dimensions` that use `+=`, `-=`, `*=` or `/=`: **0** in GeneralTools (`PointTest` uses `val` locals), MyGameTools (`GameObject.location` is a `val`; the two sites in `Actor` and two in tests go through it; the only `var` holders are `Actor.destination` and `Alive.respawn`, neither used with a compound operator), GameGraphics (no compound operator touches a `Point`) and MyGameServer (none). No downstream file declares its own `operator fun Point.plus`. So a `Point`-returning `plus` would break no known source, but would break any external `var` holder, and the break could not be undone or deferred: adding it in a minor later is the same break.
- **Demand (reasoned from grep).** Two downstream main-source sites build a `Point` from component arithmetic (`Alive.healthBar`, `ZoneGrid.zones`); both add scalar offsets, not two points. Nothing downstream needs `Point + Point`.
- **`div` returning `Result<Point>`** coexists with `divAssign` (not a subtype, so `p /= k` picks `divAssign`), but an operator whose result must be unwrapped does not compose in expressions; not recommended.
- **`unaryMinus` is safe** (reasoned): there is no assign counterpart, so no ambiguity; additive at any time.
- **Opt-in route, if arithmetic is wanted without a break (reasoned, unverified by a compiler).** Declare extensions `operator fun Point.plus(other: Point): Point` in a separate package such as `com.spartanlabs.geometry.operators`. Extensions "outside [their] declaring package" must be imported ([claim 3](#7-load-bearing-claims)), so only files that import them can meet the ambiguity. Caveats: a star import brings them in; a later *member* `plus` would shadow them silently ("the member function takes precedence", [claim 3](#7-load-bearing-claims)). It would be a Supported Extension (a likely, non-core need) if built; there is no evidence yet that it is wanted.

### 3.4 The immutable copy type

**Parts: four `Double`s (practice).** Origin-plus-size storage is what JavaFX `Rectangle2D` (`minX`, `minY`, `width`, `height`), korlibs `Rectangle` (`x`, `y`, `width`, `height`) and OPENRNDR `Rectangle` (`corner`, `width`, `height`) use ([claims 7–9](#7-load-bearing-claims)); Compose `Rect` stores edges (`left`, `top`, `right`, `bottom`) ([claim 6](#7-load-bearing-claims)). For this repo origin-plus-size is the better fit (reasoned): `Square.max` is `location + dimensions`, and with stored edges `width = right - left` need not equal the original width (`(x + w) - x != w` in floating point), whereas storing `x, y, width, height` makes `toImmutable().width` equal `dimensions.width` exactly and its `maxX` bit-identical to `Square.max.x`. korlibs has the same pair with the same four doubles, and has since deprecated the mutable twin with "Use Rectangle" ([claim 7](#7-load-bearing-claims)). Immutable point and size parts would add two more public types, each with its own equality and tier, for no consumer need.

**Implements `AxisAlignedBox`: yes (practice + reasoned).** The intersection functions and `contains` take `AxisAlignedBox`; `CenteredBox` already implements it. JavaFX has the same split (`Bounds` read-only abstraction with `BoundingBox` immutable implementation, [claim 8](#7-load-bearing-claims) for `Rectangle2D`; `BoundingBox extends Bounds`). An immutable implementation must return a fresh `Point`/`Dimensions` per `min`/`max`/`center`/`size` read, which is safe (mutating the copy cannot reach the box) but allocates.

**Allocation on the hot path (reasoned from the code; not measured).** Reads of `box.min`, `box.max`, `box.size` in the shared code:

| function | reads per call | allocations for `Square` (`min`/`size` live) | allocations for `CenteredBox` and for an immutable box |
|---|---|---|---|
| `rayIntersectsBox` | `size` once, `min` ×4, `max` ×4 (finite-corner check, then slab) | 4 (`max`) | 9 |
| `segmentIntersectsBox` | `size` once, `contains` up to 2 × (`min` ×2 + `max` ×2), slab `min` ×2 + `max` ×2 | up to 6 | up to 13 |

Whether HotSpot removes them depends on inlining the interface call and escape analysis, which fail for megamorphic receivers and non-inlined calls ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.9, Shipilev, [claim 15](#7-load-bearing-claims)); JS/Wasm are not established. So an immutable box that allocates on every read would make its own best use case (a cached, handed-out region queried every tick) the slow path.

**Primitive accessors on `AxisAlignedBox` (additive; JVM and klib).**

- Add `val minX: Double get() = min.x`, `minY`, `maxX`, `maxY`, `width: Double get() = size.width`, `height`, all with default bodies; override them allocation-free in `Square`, `CenteredBox` and the immutable class (`CenteredBox`: `center.x - halfExtents.width` etc., the same expressions as its `min`/`max`, so results are bit-identical), and have `contains`, `boxProblem`, `rayIntersectsBox` and `segmentIntersectsBox` read the primitives. Names follow JavaFX (`minX`, `maxX`, `width`) and the interface's own `min`/`max`/`size`.
- **JVM:** Kotlin 2.2 compiles interface members with bodies as JVM default methods by default (`-jvm-default=enable`: "Generates default implementations in interfaces and includes compatibility bridges and `DefaultImpls` classes", [claim 14](#7-load-bearing-claims); the default follows the language version, [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) claim 23). A class compiled against the interface without the new members still links, because the default method is inherited. Within this module and the known consumers (no downstream class implements `AxisAlignedBox`; only `CenteredBox` and `Square` do) nothing is affected.
- **klib targets (`js`, `wasmJs`):** interface members with bodies are ordinary Kotlin semantics; there is no `DefaultImpls` concern. klib binary-compatibility guarantees are not finalized (README, "Why exactly 2.2" / [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md)), which affects every addition equally.
- **Source risk (reasoned):** an external Kotlin class that implements `AxisAlignedBox` and already declares an incompatible `width`/`minX` would fail to compile. None exists in the four repositories; it is the only residual risk and is why the accessors belong in a major.

**Equality, hash and `toString` (consensus + reasoned).**

- A Kotlin `data class` of four `Double`s would use `Double.equals` semantics (total order: `0.0 != -0.0`, `NaN == NaN`; [`immutable-geometry-model.md`](immutable-geometry-model.md) §3.5, claim 3 there) and bit-based hashing. That would make `ImmutableRectangle(0.0, …) != ImmutableRectangle(-0.0, …)` while `Point(0.0, 0.0) == Point(-0.0, 0.0)`, contradicting the equality contract's "pairs equal in 2.x stay equal" in spirit and making the two box types disagree on the same coordinates. Hand-write `equals` (same concrete class; `sameValueAs` per component) and `hashCode` (class folded in; `-0.0` → 0; every NaN alike) with the same `internal` helpers as `TwoDoubles`. `toString` keeps the platform's own number formatting, in the layout of `Square`'s generated one (`Name(x=…, y=…, width=…, height=…)`) or any layout the maintainer prefers; tests check layout only ([`cross-platform-behaviour-parity.md`](cross-platform-behaviour-parity.md)).
- **Cross-type equality is not provided** (same concrete class): `Rectangle(...)` is never equal to `ImmutableRectangle(...)`. Kool's `Vec2d.equals` accepts any `Vec2d`, so a mutable and an immutable vector can be equal ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.2, claim 7 there). Compose's `MutableRect` defines no `equals`/`hashCode` at all (identity), so it cannot be misused as a hash key ([claim 6](#7-load-bearing-claims)).
- No `data` modifier: Kotlin's guidelines advise against data classes in a library API because the generated constructor and `copy` freeze ([claim 13](#7-load-bearing-claims)); a hand-written `copy(x = …, y = …, width = …, height = …)` is additive and can evolve.

**Relation to `CenteredBox` (reasoned).** `CenteredBox` is a data class of two mutable parts documented "treat as immutable". Changing its parts is a break; leave it. `AxisAlignedBox.toSquare()` already builds an independent mutable copy of any box with fresh `Point`/`Dimensions`, so it serves as the "deep mutable copy" under the new name; no `CenteredBox` twin is needed.

**Conversion naming (practice).** Compose has `MutableRect.toRect()`, an extension named after the target type ([claim 6](#7-load-bearing-claims)); korlibs has `MMatrix.immutable`; kotlinx.collections.immutable has `toImmutableList()`: "Converts a read-only or mutable collection to an immutable one" ([claim 16](#7-load-bearing-claims)). `toImmutable()` as a member of the mutable type is stable under any choice of type name, reads like the stdlib's `to…` conversions, and avoids two alternatives: `snapshot()` (MyGameTools uses "Snapshot" for serializable network DTOs, e.g. `PointSnapshot`, `GameObjectSnapshot`, an "immutable, serializable copy") and `frozen()` (suggests the receiver itself is frozen).

### 3.5 Read-only views under the mutable model

**What is true in the code (repo facts).** `Square.min` returns `location` itself and `Square.size` returns `dimensions` itself (documented as such in the KDoc: "i.e. [location] itself"); `max` and `center` return fresh `Point`s. So `square.min.x = 5.0` moves the square, while `square.max.x = 5.0` changes nothing. Typing a property as `AxisAlignedBox` does not stop the former. With `Point`/`Dimensions` mutable, `AxisAlignedBox` is a read-only *interface* (it has no setters) but not a read-only *view*.

| option | what it gives issue #8 | further break? |
|---|---|---|
| **(a)** `AxisAlignedBox` documented as a shape contract; the immutable copy and the primitive accessors are the protection | A library hands out `ImmutableRectangle` (copied once, at construction, not per access) when callers must not mutate; hands out `AxisAlignedBox` for allocation-free live reads when it trusts them. Does not stop mutation through `min`/`size` of a live `Rectangle`. | none |
| **(b)** `Rectangle.min` and `Rectangle.size` return copies | Every implementor then returns fresh values, so an `AxisAlignedBox`-typed handle to a live box is sound at runtime ("hand out the live object without copying" for reads, via the primitive accessors; mutation through the returned parts cannot reach the box). Also removes the min/max inconsistency. | **yes, a behavioural change** from the documented identity of `min`/`size`; the type is renamed in the same release, and no caller in MyGameTools, GameGraphics, MyGameServer or WebTools reads `Square.min`/`.size` (grep) |
| **(c)** `ReadOnlyPoint { val x; val y }`, `ReadOnlyDimensions`, `ReadOnlySquare` as in issue #8 | A compile-time read-only live view. Additive if only implemented by `Point`/`Dimensions`/`Rectangle`; it only helps if library signatures use them, and changing `Point` parameters to `ReadOnlyPoint` is an API-dump break. | additive if only implemented; a break if signatures change |

- **(c) in detail (reasoned).** (1) Viral: `distanceFrom(other: Point)`, `contains(p: Point)`, `Segment(a: Point, b: Point)`, `Ray` all take `Point`; a `ReadOnlyPoint` cannot be passed to them without overloads or signature changes. Kotlin's `List` works because the whole stdlib takes `List`/`Iterable`. (2) An interface cannot carry `equals`/`hashCode` ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.3), so a `ReadOnlyPoint` implemented by a consumer's class is never equal to a `Point` under "same concrete class". (3) A read-only view is not an immutable value: "Functions in this interface support only read-only access to the list; read/write access is supported through the MutableList interface" ([`immutable-geometry-model.md`](immutable-geometry-model.md) claim 17); a holder cannot know whether someone else mutates, and a downcast defeats it. (4) Each new interface is a seam with no consumer evidence yet, so it would be Experimental, and an ERROR-level opt-in on *use* is exactly the friction a "hand out geometry" type must not have ([claim 15](#7-load-bearing-claims): `@OptIn` is not propagated; propagating passes the requirement to every caller).
- **The cost issue #8 names, under (a).** `ZoneGrid` copies `Point(space.bounds.location)` and reads two doubles: with the immutable copy it becomes one `space.bounds.toImmutable()` taken at construction; `UnzonedPointException` still needs the point copy it already makes (a `Point` is the thing the caller handed in, and `Point` has no immutable twin); `Zone.bounds` can be typed `ImmutableRectangle` (copied once per zone at construction) so callers cannot mutate it, replacing the prose "do not mutate". The one residual copy-on-hand-out is the point itself.
- **Mutable Point/Dimensions have no read-only supertype**, and no new type for them is proposed: the immutable copy covers the box, the case issue #8 actually shows. A twin for `Point` can be added later without a break if a consumer asks.

### 3.6 `Rectangle` (renamed) as a `data class` with `val` references to mutable parts

- **`copy()` is shallow; this is not new.** `Square` is a `data class` with `var` references today, so `copy()` already shares the `Point` and `Dimensions` instances; after the change it still does. The KDoc must say so, and point to `toImmutable()` (independent immutable) and `toSquare()`'s renamed successor (independent mutable) for real copies.
- **`componentN`/`copy` stay**: dropping `data` removes `copy`, `component1`/`component2`, and the generated `equals`/`hashCode`/`toString` shape — a further break beyond "renamed with `val` references". The Kotlin guideline against data classes in APIs concerns *adding* properties later ([claim 13](#7-load-bearing-claims)); this type's two properties are its identity, so the freeze costs nothing. Keep `data`.
- **Hash-key hazard (consensus on the rule, reasoned for the type).** `equals`/`hashCode` follow the mutable parts. Java's `Map` documentation: "great care must be exercised if mutable objects are used as map keys. The behavior of a map is not specified if the value of an object is changed in a manner that affects `equals` comparisons while the object is a key in the map" ([claim 12](#7-load-bearing-claims)). The hazard exists today (`Square`'s `var` references make it larger; `val` references remove only the reassign half). MyGameTools' `Zone` documents the same and excludes `bounds` from its equality for that reason. The immutable copy is the safe key.
- **Because the references are `val`**, the only way to move or resize a rectangle is through its parts (`location.setTo(…)`, `dimensions.setTo(…)`). The one downstream assignment (`VisibleObject.dimensions`'s setter, `area.dimensions = value`) becomes `area.dimensions.setTo(value)`. An additive `Rectangle.setTo(other)` mirroring `TwoDoubles.setTo` is possible but not needed.

### 3.7 Mutable values as hash keys: how comparable types document it

- **Practice: they mostly do not.** The general warning lives on `Map` (above). libGDX `Vector2` and `Rectangle` define value-based `equals`/`hashCode` (bit-based; [claim 5](#7-load-bearing-claims)) with no key warning in the class, and AOSP's `Rect` is a `final` class with public mutable fields and value-based `equals`/`hashCode` ([claim 10](#7-load-bearing-claims)). Compose avoids the hazard for `MutableRect` by defining no equality, and gives the immutable `Rect` a data class ([claim 6](#7-load-bearing-claims)). korlibs' deprecated `MRectangle` and `MPoint` are data classes over `var`s ([claim 7](#7-load-bearing-claims)).
- **What the KDoc should say (reasoned).** On `TwoDoubles`, `Point`, `Dimensions` and `Rectangle`: "Equality and hash code follow the current values. Do not mutate an instance while it is a key in a hash-based collection or an element of a hash-based set; use [`toImmutable`] (for `Rectangle`) or a copy as the key." One sentence in `TwoDoubles.equals`/`hashCode`, repeated on `Rectangle`; the equality contract itself (same concrete class; NaN equals NaN; `-0.0` hashes as `0.0`) stays as in [`immutable-geometry-model.md`](immutable-geometry-model.md) §3.5.

### 3.8 `TwoDoubles` as an open base with mutable state

- **Kotlin members are final unless marked `open`; `override` members are open unless marked `final`** (language rule; reasoned from the 2.x declarations). So in the unsealed class, `first`, `second`, `setTo`, `modBy`, `amplifyBy`, `divideBy`, the four assign operators and `containsNaN` are already final without any annotation, but `equals` and `hashCode`, being overrides, would be overridable unless declared `final override`. Make `equals` and `hashCode` `final` (they are the invariant: same concrete class and values, hash folds the class). Leave `toString` open (a consumer's type may want its own text; the contract does not depend on it). Opening a `final` member later is source- and binary-compatible; closing it is not.
- **`first`/`second` setters stay final.** `Point.x = v` writes `first` today; the in-place verbs write through the same properties. An overridable setter would let a subclass intercept some mutations but not others; if a consumer proves it needs validation hooks, an `open` member can be added later.
- **No factory hook.** The immutable-model design needed `protected abstract fun create(first, second)` because `divideBy` and operators built new instances. In this model no member returns a new `TwoDoubles` (shape (a) of §3.1; no `plus`), so `TwoDoubles` has no abstract members. Consumers' subclasses implement nothing. Adding an abstract member to an open class later would be a break; this avoids ever needing one unless a new-instance member is added, which can be done with a defaulted `open` function instead.
- **`abstract class`, not `open class`.** The `sealed` base cannot be instantiated; `abstract` keeps that, whereas `open` would make `TwoDoubles(1.0, 2.0)` constructible and unequal to `Point(1.0, 2.0)` under the same-class rule.
- **`Point` and `Dimensions` stay final classes** with their copy constructors ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.7).

### 3.9 Stability tiers

| item | tier | confidence | why |
|---|---|---|---|
| subclassing `TwoDoubles` | Experimental (`@SubclassOptInRequired`) | as set | no consumer has subclassed it; carried over |
| `divideBy: Result<Unit>`, in-place operators | Stable Core | high | the type's core function; shape is the 2.x shape |
| `AxisAlignedBox` primitive accessors | Stable Core | medium | names conventional (JavaFX); defaulted, additive; needed by hot paths and the immutable type |
| `ImmutableRectangle` and `Rectangle.toImmutable()` | **Stable Core** | **medium** | need evidenced (issue #8, three defensive-copy sites); shape matches four libraries; see below |
| extension operators in a separate package (if built) | Supported Extension | low | likely but non-core; no demand shown |

- **Why Stable Core for the immutable type, not Experimental (reasoned).** Experimental is for "a seam whose shape has not been proven by a real consumer yet". `ImmutableRectangle` is a final value type, not an extension seam. Gating it behind `@RequiresOptIn(ERROR)` would require every consumer that names it, and every library that exposes it (the stated purpose: `Zone.bounds`), either to `@OptIn` (not propagated, so downstream users use the experimental API unknowingly) or to propagate the marker to its own callers ([claim 15](#7-load-bearing-claims)). Promoting Experimental to Stable later costs nothing and the reverse costs a major, so the risk of Stable is real; it is bounded by keeping the surface small: four accessors, the `AxisAlignedBox` members, `copy`, `equals`/`hashCode`/`toString`, and conversion back via the existing extension. The residual uncertainty is the name and the accessor set, both settled by the maintainer before release; if the maintainer is not ready to freeze them, the alternative is Experimental with the friction above.

## 4. How comparable projects do it

| project (pin) | mutable rectangle | immutable rectangle | conversion | equality of the mutable type | in-place ops |
|---|---|---|---|---|---|
| Compose `ui-geometry` (androidx `62841bb`) | `class MutableRect(var left, top, right, bottom: Float)` | `@Immutable data class Rect(left, top, right, bottom)` | extension `MutableRect.toRect(): Rect` | none (identity) | `translate`, `inflate`, `set`, `intersect` |
| korlibs (`94e645c`) | `@Deprecated("Use Rectangle") data class MRectangle(var x, y, width, height: Double)` | `data class Rectangle(val x, y, width, height: Double)` | property `MMatrix.immutable`; `MRectangle.mutable` | data class over `var`s | mutators |
| JavaFX (jfx `cb56d4c`) | none | `Rectangle2D(minX, minY, width, height)`; `BoundingBox extends Bounds` | n/a | n/a | n/a |
| OPENRNDR (`v0.5.0`) | none | `data class Rectangle(val corner: Vector2, val width: Double, val height: Double)` | n/a | n/a | n/a |
| libGDX (`1.14.2`) | `class Rectangle` with public `float x, y, width, height`; `static public final Rectangle tmp` | none | n/a | `floatToRawIntBits` per field | `set(...)`, fluent |
| AOSP (`android-15.0.0_r1`) | `public final class Rect` with public fields | none | n/a | value-based | `set`, `offset`, ... |
| AWT / Swing (Java 21) | `java.awt.Rectangle` | none | `getBounds()` hands out fresh copies; `JComponent.getBounds(Rectangle rv)` avoids allocation | value-based | `setBounds`, ... |
| Kotlin collections | `MutableList` | `List` (read-only view); kotlinx.collections.immutable `ImmutableList` | `toImmutableList()` | n/a | n/a |

| project | vector type | non-assigning ops next to assigning ones | `/=` or `div` by zero |
|---|---|---|---|
| Kool `v0.19.0` | `Vec2d` / `MutableVec2d : Vec2d` | `plus`, `minus`, `times`, `div` on the base `Vec2d` (return `Vec2d`); `plusAssign`... on `MutableVec2d`. `set`/`add` return `this` | `divAssign(div: Double) { mul(1.0 / div) }`, no check |
| libGDX `1.14.2` | `Vector2` | none; `add`, `scl`, `nor` mutate and return `this` | no `div`; `nor()` leaves a zero vector unchanged |
| kotlin-math `v1.8.0` | `data class Float2(var x, var y)` | `plus`, `div`, no assign operators ([`immutable-geometry-model.md`](immutable-geometry-model.md) claim 12) | IEEE |
| GeneralTools 2.x | `Point`, `Dimensions` | assign operators only | throws |

Signatures worth copying: Compose `public fun MutableRect.toRect(): Rect = Rect(left, top, right, bottom)`; Kool `operator fun plus(that: Double) = Vec2d(x + that, y + that)` on the read-only base; libGDX `public Vector2 nor ()` (`if (len != 0) { x /= len; y /= len; } return this;`).

## 5. Pitfalls & regrets

- **A mutable twin kept beside an immutable rectangle is a recurring regret.** korlibs deprecated `MRectangle`/`MPoint` ("Use Rectangle", "Use Point instead") in favour of immutable data classes ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.2). GeneralTools cannot drop its mutable types in 3.0.0; the lesson for this design is to keep the twin small (no mutable-twin growth: no `ReadOnly*` interface family) and make the immutable type the one to hand out.
- **Defensive copying as the default hand-out.** AWT's getters allocate a fresh mutable value on every call and Swing added out-parameter overloads for callers "who want to avoid allocating a new `Rectangle` object on the heap" ([claim 11](#7-load-bearing-claims)). The primitive accessors are the same escape hatch without an out-parameter.
- **Public mutable shared instances.** libGDX's `Rectangle.tmp` and `tmp2` are `static public final` mutable scratch objects with the comment "Use with care! Use only when sure other code will not also use this." ([claim 5](#7-load-bearing-claims)); the repo's `Point` has no shared constants and should not gain one while mutable.
- **`Square.min`/`size` alias live state while `max`/`center` copy.** An asymmetry that makes `square.min.x = 5.0` move the square (§3.5).
- **A `Point`-returning `plus` next to `plusAssign`** turns `+=` on every `var` holder into an ambiguity error and cannot be introduced compatibly later (§3.3).
- **Generated `data class` equality on four `Double`s** disagrees with `TwoDoubles` equality on `-0.0` (§3.4).
- **Failure construction in a loop.** `divideBy(0.0)` (and `/=` if it delegates) builds an exception and logs at WARN each call; on the JVM that includes capturing a stack trace ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.9). Divisors that can be zero must be tested first in hot loops.
- **A shared `Point` is part of the contract downstream.** `VisibleObject(area) : GameObject(area.location)` shares one `Point`, and `Alive.updateHealthBar` mutates it in place with `healthBar.location.setTo(…)`; the mutable model keeps this working, and the rename must not copy `location` anywhere. `Rectangle.min` as a copy (option (b)) must not change `location` itself.
- **Name collisions with a near-term consumer.** SpartanGraphics already plans `Color, Point, Size, Rect` in `com.spartanlabs.graphics` and `Rectangle, Text, OutlinedRectangle` in `.shape`, plus `Box`, `Region` and `Frame` ([claim 19](#7-load-bearing-claims)); its adapters over GeneralTools types will alias `Color` and `Point` regardless.

## 6. Recommendation for this repo

### 6.1 Public signatures (sketch; KDoc at the repo's Level-2 standard on every member)

```kotlin
// common
@SubclassOptInRequired(ExperimentalForInheritanceGeneralToolsApi::class)
abstract class TwoDoubles(var first: Double = 0.0, var second: Double = 0.0) {   // no abstract members
    constructor(doubles: TwoDoubles)
    val containsNaN: Boolean
    fun setTo(first: Double, second: Double)                 // Unit, unchanged
    fun modBy(first: Double, second: Double)                 // Unit, unchanged
    infix fun setTo(new: TwoDoubles)
    infix fun modBy(change: TwoDoubles)
    infix fun amplifyBy(magnitude: Double)
    infix fun divideBy(magnitude: Double): Result<Unit>      // in place; 0.0 and -0.0: value unchanged, WARN via KLogger.failure
    operator fun plusAssign(doubles: TwoDoubles)
    operator fun minusAssign(doubles: TwoDoubles)
    operator fun timesAssign(magnitude: Double)
    operator fun divAssign(magnitude: Double)                // see call 2
    final override fun equals(other: Any?): Boolean          // same class; IEEE per component; NaN == NaN
    final override fun hashCode(): Int                       // class folded in; -0.0 as 0.0; every NaN alike
    override fun toString(): String
}
// Point and Dimensions: final, unchanged signatures.

interface AxisAlignedBox {
    val min: Point; val max: Point; val center: Point; val size: Dimensions           // unchanged
    val minX: Double get() = min.x                                                    // new, defaulted
    val minY: Double get() = min.y
    val maxX: Double get() = max.x
    val maxY: Double get() = max.y
    val width: Double get() = size.width
    val height: Double get() = size.height
    infix fun contains(p: Point): Boolean                                              // default now reads the primitives
}

data class Rectangle(val location: Point = Point(), val dimensions: Dimensions = Dimensions()) : AxisAlignedBox {
    fun toImmutable(): ImmutableRectangle
    // overrides minX..height from location/dimensions with no allocation
}

class ImmutableRectangle(val x: Double, val y: Double, override val width: Double, override val height: Double) : AxisAlignedBox {
    fun copy(x: Double = this.x, y: Double = this.y, width: Double = this.width, height: Double = this.height): ImmutableRectangle
    // min/max/center/size return a new Point/Dimensions per read; equals/hashCode/toString hand-written
}

fun AxisAlignedBox.toRectangle(): Rectangle      // the renamed toSquare(); independent mutable copy
fun Rectangle.toCenteredBox(): CenteredBox        // receiver renamed
```

### 6.2 Trade-offs that need a call

**Call 1: the name pair.** Ranked:

| rank | pair (mutable / immutable) | for | against | reads at the 14 MyGameTools sites |
|---|---|---|---|---|
| 1 | `Rectangle` / `ImmutableRectangle` | self-describing; the KDoc already calls the type "square (or rectangle)"; plain name = mutable, as `Point` and `Dimensions` are; `Immutable*` is the Guava / kotlinx.collections.immutable convention for the immutable twin | `java.awt.Rectangle` (imported by GeneralTools' own `UserActions.kt`), libGDX and JavaFX `Rectangle`; SpartanGraphics' planned `.shape.Rectangle` | `area: Rectangle`, `bounds: Rectangle`; `Zone.bounds: ImmutableRectangle` |
| 2 | `TopLeftBox` / `ImmutableTopLeftBox` | same "Box" vocabulary as `AxisAlignedBox` and `CenteredBox`; names the origin convention that distinguishes it from `CenteredBox`; no collision found | unfamiliar; "top-left" bakes in a y-down reading (the KDoc already says "top-left (minimum-x, minimum-y)"); long | `area: TopLeftBox` |
| 3 | `MutableRectangle` / `Rectangle` | Kotlin `Mutable*` prefix; short name goes to the type handed out | in one package `Point` would be mutable but `Rectangle` immutable; same awt/libGDX/SpartanGraphics collisions | `area: MutableRectangle` |
| 4 | `Bounds` / `ImmutableBounds` | reads well for `Space.bounds`, `Zone.bounds` | `val bounds: Bounds` stutter; `javafx.geometry.Bounds`; a "bounds" is an attribute more than a located box | `bounds: Bounds` |
| 5 | `MutableRect` / `Rect` | Compose's exact convention ([claim 6](#7-load-bearing-claims)) | collides with both Compose names and with SpartanGraphics' planned `Rect` | `area: MutableRect` |

Recommendation: rank 1. Conversion `toImmutable()` whatever the pair. The `toSquare` extension is renamed with the type (part of the rename break). This is the maintainer's call; the real constraint is that every plain geometric noun is in use in some neighbouring library.

**Call 2: `/=` on zero.** Options in §3.2. Recommendation: **throw** `IllegalArgumentException("Cannot divide by zero")` directly (no log), keeping `divAssign` exactly as in 2.x and the existing test green; document in the KDoc that `divideBy` is the non-throwing form. Alternative if the maintainer wants one zero policy for the type: delegate to `divideBy` and leave the value unchanged with a WARN; that is a behavioural change to `/=` beyond the listed `divideBy` break unless the maintainer reads that break as covering the operator, and the migration notes would say so. IEEE division and dropping the operator are not recommended.

**Call 3: `divideBy`'s return shape.** Recommendation: (a) `Result<Unit>`, in place, name unchanged, `final`. (b) is acceptable only if chaining is wanted and the covariant-override spike passes; (c) is rejected.

**Call 4: arithmetic of `divideBy`.** Keep the reciprocal (identical results to 2.x, matches Kool) or divide each component (accurate, ≤ 1 ulp different on some inputs, finite on subnormal divisors). Recommendation: divide each component; no known caller depends on the last bit, and the change is inside the method that already changes. It is a numerical behaviour change and goes in the migration notes.

**Call 5: non-assigning operators.** Recommendation: none on `Point`/`Dimensions` in 3.0.0; a `Point`-returning `plus`/`minus`/`times`/`div` is a further source break (`+=` on a `var` holder becomes ambiguous) that cannot be deferred to a minor. `unaryMinus` may be added now or later. If arithmetic is wanted, use the import-gated extension route in §3.3 and have a compile check confirm it first.

**Call 6: `min`/`size` aliasing (read-only views).** Recommendation: (a) now, and (b) if the maintainer accepts one named behavioural change: `Rectangle.min` and `Rectangle.size` return copies, matching `max`/`center`, making `AxisAlignedBox` a sound live read-only view for reads (with the primitive accessors the hot paths do not allocate). It is the only moment it can be done without a silent behaviour change in a minor, and no known caller reads them. `ReadOnlyPoint`/`ReadOnlyDimensions`/`ReadOnlySquare` are not recommended; they can be added later without a break if a consumer demonstrates the need.

**Call 7: tier of `ImmutableRectangle`.** Stable Core, medium confidence (§3.9). If the maintainer would rather keep the name or accessor set open, Experimental with the opt-in cost described.

**Call 8: primitive accessors on `AxisAlignedBox`.** Add now (Stable Core). Waiting is also additive, but the new immutable type would ship with an allocating hot path.

### 6.3 Settled by evidence

- `Rectangle` stays a `data class`; `copy`/`componentN` stay; KDoc states `copy()` is shallow and that equality follows the mutable parts.
- `ImmutableRectangle` stores `x, y, width, height`; equality and hash are hand-written with the shared `internal` helpers; no cross-type equality.
- `TwoDoubles` is an `abstract` class with no abstract members; `equals`/`hashCode` `final`; setters and verbs final; no factory hook.
- `CenteredBox`, `Segment`, `Ray` are unchanged ("treat as immutable" stays accurate).

### 6.4 Options that would add a break beyond the listed set (named)

- A `Point`/`Dimensions`-returning `plus`/`minus`/`times`/`div`: source break for `var` holders using `+=`, `-=`, `*=`, `/=`.
- `Rectangle.min`/`size` as copies: behavioural change from the documented identity (call 6).
- `/=` that no longer throws, or IEEE `/=`: behavioural change; dropping `divAssign`: source break.
- `divideByCatching` or any rename of `divideBy`: source break beyond the listed one.
- Dropping `data` from `Rectangle`: loses `copy`/`componentN`.
- Changing `Segment.a`, `Ray.origin`, `CenteredBox.center` or `AxisAlignedBox.min` to a read-only interface type.
- Reciprocal to true division in `divideBy`: last-ulp behavioural change in a method that is already changing.

## 7. Load-bearing claims

1. **Augmented assignment and the ambiguity rule.** "If the corresponding binary function (that means `plus()` for `plusAssign()`) is available too, `a` is a mutable variable, and the return type of `plus` is a subtype of the type of `a`, report an error (ambiguity)." "Make sure its return type is `Unit`, and report an error otherwise." "Otherwise, try to generate code for `a = a + b` (this includes a type check: the type of `a + b` must be a subtype of `a`)." Source: <https://kotlinlang.org/docs/operator-overloading.html>, read 2026-10-18. (The second and third sentences were extracted by the page fetcher; the first matches [`immutable-geometry-model.md`](immutable-geometry-model.md) claim 1.)
2. **The specification's two expansions.** "If both expansion variants result in correctly resolved and inferred code, this should be reported as an operator overloading ambiguity. If only one of the expansion variants can be resolved correctly, this variant is picked as the correct one." Source: <https://kotlinlang.org/spec/statements.html> ("Operator assignments"), read 2026-10-18 (fetcher-extracted).
3. **Extensions: member wins; import across packages.** "If a class has a member function and there's an extension function with the same receiver type, the same name, and compatible arguments, the member function takes precedence." "To use an extension outside its declaring package, import it at the call site." Source: <https://kotlinlang.org/docs/extensions.html>, read 2026-10-18 (fetcher-extracted).
4. **Kool's split of read-only operators and mutable assign operators.** `open class Vec2d(open val x: Double, open val y: Double) {`; `operator fun plus(that: Double) = Vec2d(x + that, y + that)`; `open class MutableVec2d(override var x: Double, override var y: Double) : Vec2d(x, y) {`; `operator fun plusAssign(that: Double) { add(that) }`; `operator fun divAssign(div: Double) { mul(1.0 / div) }`; `open fun set(x: Double, y: Double): MutableVec2d {`. Source: <https://github.com/kool-engine/kool/blob/v0.19.0/kool-core/src/commonMain/kotlin/de/fabmax/kool/math/Vec2.kt>, tag `v0.19.0`.
5. **libGDX: fluent in-place ops, `nor()` leaves a zero vector alone, public mutable statics, bit-based equality.** `public Vector2 nor () {` … `if (len != 0) {` `x /= len;` `y /= len;` … `return this;`; `public Vector2 set (float x, float y) {` … `return this;`; Rectangle: `/** Static temporary rectangle. Use with care! Use only when sure other code will not also use this. */` `static public final Rectangle tmp = new Rectangle();`, `public float x, y;`, `public float width, height;`, `NumberUtils.floatToRawIntBits(width)` in `hashCode`. Source: <https://github.com/libgdx/libgdx/blob/1.14.2/gdx/src/com/badlogic/gdx/math/Vector2.java> and `…/Rectangle.java`, tag `1.14.2`.
6. **Compose pairs `MutableRect` with `Rect` through a type-named extension; the mutable type has no equality.** `public fun MutableRect.toRect(): Rect = Rect(left, top, right, bottom)`; `public class MutableRect(` with `public var left: Float,` …; `@Immutable` … `public data class Rect(`; the only `override` in `MutableRect.kt` is `public override fun toString(): String =` (no `equals`/`hashCode`). Source: <https://github.com/androidx/androidx/blob/62841bbfbf6d1f0a79ca386cd8ce71182868ef28/compose/ui/ui-geometry/src/commonMain/kotlin/androidx/compose/ui/geometry/MutableRect.kt> and `…/Rect.kt`, commit `62841bb`.
7. **korlibs stores origin plus size and deprecated the mutable twin.** `data class Rectangle(val x: Double, val y: Double, val width: Double, val height: Double) : SimpleShape2D, IsAlmostEquals<Rectangle> {`; `@KormaMutableApi` `@Deprecated("Use Rectangle")` `data class MRectangle(` `var x: Double, var y: Double,` `var width: Double, var height: Double`; `val immutable: Matrix get() = Matrix(a, b, c, d, tx, ty)` on `MMatrix`; `@Deprecated("Use Point instead")` `data class MPoint(`. Source: <https://github.com/korlibs/korlibs/blob/94e645c59f3c54a268cba01d566e0a0b4654207a/korlibs-math-vector/src/commonMain/kotlin/korlibs/math/geom/Rectangle.kt> and `…/korlibs-math/src/commonMain/kotlin/korlibs/math/geom/_MathGeomMutable.kt`, commit `94e645c`.
8. **JavaFX `Rectangle2D` is immutable, origin plus size, IEEE `equals`, bit-based hash.** `public class Rectangle2D {`; `public double getMinX() { return minX; }`; `public double getWidth() { return width; }`; `return minX == other.minX && minY == other.minY && width == other.width && height == other.height;`; `bits = 31L * bits + Double.doubleToLongBits(minX);`; `public class BoundingBox extends Bounds {`. Source: <https://github.com/openjdk/jfx/blob/cb56d4c90fe7a43079688b92c54c7d2b364c7232/modules/javafx.graphics/src/main/java/javafx/geometry/Rectangle2D.java> (and `BoundingBox.java`, `Bounds.java` in the same directory), commit `cb56d4c`. (The equality returns are laid out over several lines in the source; the quote joins them.)
9. **OPENRNDR's rectangle.** `data class Rectangle(val corner: Vector2, val width: Double, val height: Double = width) : Movable, Scalable2D,`. Source: <https://github.com/openrndr/openrndr/blob/v0.5.0/openrndr-shape/src/commonMain/kotlin/org/openrndr/shape/Rectangle.kt>, tag `v0.5.0`.
10. **AOSP `Rect` is a final class with public mutable fields and value `equals`.** "Rect holds four integer coordinates for a rectangle." "These fields can be accessed directly." `public final class Rect implements Parcelable {`; `public boolean equals(Object o) {`, `public int hashCode() {`. Source: <https://github.com/aosp-mirror/platform_frameworks_base/blob/android-15.0.0_r1/graphics/java/android/graphics/Rect.java>, tag `android-15.0.0_r1`.
11. **Swing's allocation-avoiding overload.** "Stores the bounds of this component into "return value" `rv` and returns `rv`. If `rv` is `null` a new `Rectangle` is allocated. This version of `getBounds` is useful if the caller wants to avoid allocating a new `Rectangle` object on the heap." Source: <https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/JComponent.html> (`getBounds(Rectangle rv)`), Java SE 21 (fetcher-extracted).
12. **Mutable map keys.** "Note: great care must be exercised if mutable objects are used as map keys. The behavior of a map is not specified if the value of an object is changed in a manner that affects `equals` comparisons while the object is a key in the map." Source: <https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html>, Java SE 21.
13. **Kotlin API guidelines on data classes and return types.** Section "Avoid using data classes in your API": "In regular development, the strength of data classes is the extra functions that are generated for you. In API design, this strength becomes a weakness." Section "Avoid widening or narrowing return types": "You might think that narrowing a return type, for example from `Collection` to `List` would preserve compatibility. Unfortunately, while source compatibility is preserved, binary compatibility is broken." Source: <https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html>, read 2026-10-18 (fetcher-extracted).
14. **JVM default methods under `-jvm-default=enable`.** "Default behavior. Generates default implementations in interfaces and includes compatibility bridges and `DefaultImpls` classes. This mode maintains compatibility with older compiled Kotlin code." Source: <https://kotlinlang.org/docs/java-to-kotlin-interop.html> ("Compatibility modes for default methods"), read 2026-10-18 (fetcher-extracted). That the default follows the 2.2 language version is [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) claim 23.
15. **Opt-in propagation.** "The opt-in requirement is not propagated, which means others might unknowingly use experimental APIs. To avoid this, it is safer to propagate the opt-in requirements." "The opt-in propagates the opt-in requirement to users of the `getDate()` function." Source: <https://kotlinlang.org/docs/opt-in-requirements.html>, read 2026-10-18 (fetcher-extracted). Escape analysis limits: [`immutable-geometry-model.md`](immutable-geometry-model.md) claim 18.
16. **kotlinx.collections.immutable conversion naming.** `toImmutableList()`: "Converts a read-only or mutable collection to an immutable one." Source: <https://github.com/Kotlin/kotlinx.collections.immutable> README, read 2026-10-18 (fetcher-extracted).
17. **In-place versus returns-new naming in the Kotlin stdlib.** "These functions return their result as a new collection containing the elements of the original collection in the requested order. To learn about functions for sorting mutable collections in place, see the List-specific operations page." Source: <https://kotlinlang.org/docs/collection-ordering.html>, read 2026-10-18 (fetcher-extracted).
18. **Return-type mangling of members returning an inline class.** `val IrFunction.hasMangledReturnType: Boolean get() = returnType.isInlineClassType() && parentClassOrNull?.isFileClass != true`. Source: <https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/ir/backend.jvm/src/org/jetbrains/kotlin/backend/jvm/InlineClassAbi.kt>, `v2.4.21`, as recorded in [`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) claim 15.
19. **SpartanGraphics' planned names.** `com.spartanlabs.graphics       Color, Point, Size, Rect` and `.shape                       Rectangle, Text, OutlinedRectangle, Outline, layers, tags`; and "`Box` is a rectangle with a colour." Source: `docs/plans/1-phase-1-spartangraphics/architecture.md` §3.1 and `docs/plans/spartan-libraries-split.md` in the SpartanGraphics working tree @ `50b61e2` (planning documents, not released API; they can change).

## 8. Gaps

- **Nothing was compiled.** The smallest checks, in order:
  1. `abstract class TwoDoubles` with `@SubclassOptInRequired`, `final override equals/hashCode`, and `infix fun divideBy(Double): Result<Unit>` compiles in common code at `languageVersion` 2.2 on `jvm`, `js`, `wasmJs`; `Point`/`Dimensions` need only `@OptIn` and do not leak a requirement to code that merely uses them; a throwaway subclass without opt-in fails.
  2. The resolution table in §3.3 (`val`/`var` holders against a `plus` returning `Point`, a non-subtype, and no `plus`), and the import-gated extension route, which rests on the documentation sentences and my reading of the specification, not on a compile.
  3. Adding defaulted `minX`...`height` to `AxisAlignedBox` leaves `Square`/`CenteredBox` and a class compiled against the old interface linking on the JVM (`DefaultImpls` bridges), and the klib dumps change by the expected lines only.
  4. Under `-jvm-default=enable` a Kotlin class that implements the interface and declares a conflicting `width` fails as stated (accidental override); the claim that no such class exists is a grep over four repositories, not over all consumers.
- **Allocation counts are my reading of the code**, not measured; JIT inlining and escape analysis may remove many on the JVM; JS and Wasm behaviour is not established ([`immutable-geometry-model.md`](immutable-geometry-model.md) §3.9).
- **The `49 * (1/49)` figures are computed in Python 3 (IEEE-754 double)**; the JVM and the JS/Wasm engines use the same double arithmetic, but it was not run on them.
- **Kotlin-documentation quotes marked fetcher-extracted** (claims 1–3, 13–17) came through a page fetcher, which returned quotation blocks; verbatim wording should be confirmed when the claims are re-opened. The extension-operator route additionally rests on the unstated assumption that an unimported extension is not a candidate for the `+=` expansion.
- **The downstream counts are greps**, not compiles: `var` holders with compound operators (0), `Square.min`/`size` readers (0), `divideBy`/`divAssign` callers (0), implementors of `AxisAlignedBox` (0), `toSquare`/`toCenteredBox` callers (0) in MyGameTools @ `dc5214f`, GameGraphics @ `8b428a4`, MyGameServer and WebTools. A type that reaches a `Point` through a function return (so a `var x = someCall()` with no explicit type) would escape the grep for `var` holders. Other consumers of the published artifact are unknown. The full downstream sweep is a separate question.
- **Whether the covariant `Result<Point>` override works** (shape (b)) is still unspiked; recommendation (a) does not depend on it.
- **Tier placement is a judgement** (§3.9): the evidence is medium for Stable Core on `ImmutableRectangle`; the name pair and the exact accessor set are not settled by evidence.
- **SpartanGraphics' names are from planning documents** that are not released API; if they change, the collision analysis in call 1 changes with them.
- **IDE inspections for an ignored `Result`** were not looked at ([`idiomatic-kotlin-api-mechanics.md`](idiomatic-kotlin-api-mechanics.md) Gaps); shape (a) does not rely on one.
- **Not examined:** whether `ImmutableRectangle` should also serialize (MyGameTools' snapshot DTOs live in its own `serializations` package and are separate types); a `Point` twin; `Segment`/`Ray`/`CenteredBox` immutable variants.
