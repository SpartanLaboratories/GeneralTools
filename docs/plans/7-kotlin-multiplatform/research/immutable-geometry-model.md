# The immutable geometry model for GeneralTools 3.0.0

- **Issues:** [#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7) (Kotlin Multiplatform) and
  [#8](https://github.com/SpartanLaboratories/GeneralTools/issues/8) (read-only views for `Point` and `Square`).
- **Date read:** 2026-10-17. Repo baseline: branch `feat/issue-7-kotlin-multiplatform`, `src/main/kotlin/geometry/*` and the geometry tests as of `master` @ `17d30bc`.
- **Pinned outside sources:** Kotlin `v2.4.21`; androidx `62841bbfbf6d1f0a79ca386cd8ce71182868ef28` (Compose `ui-geometry`); openjdk/jfx `cb56d4c90fe7a43079688b92c54c7d2b364c7232`; libGDX `1.14.2`; OPENRNDR `v0.5.0`; Kool `v0.19.0`; korlibs `94e645c59f3c54a268cba01d566e0a0b4654207a`; kotlin-math `v1.8.0`; Binaryen `version_133`.
- **Method limits:** no Kotlin was compiled or run. Language behaviour comes from the Kotlin documentation, the compiler's own test data and stdlib source at `v2.4.21`. Anything that needs a compiler to settle is listed in *Gaps* with the smallest check.

## 1. Question

What public shape should the immutable geometry model take in GeneralTools 3.0.0 (`TwoDoubles` as an open seam, `Point`, `Dimensions`, `Square`, the replacements for the in-place operators, the `Result` signature of `divideBy`, the equality contract), and is a read-only view type still needed once `Point` and `Dimensions` are immutable?

An answer must meet these requirements, all plain facts about the work:

- `Point` and `Dimensions` are immutable. `TwoDoubles` is no longer `sealed`; consumers can define their own two-double types.
- `TwoDoubles.divideBy` returns a `Result` and logs at WARN through the internal failure helper (`internal fun <T> KLogger.failure(message: String): Result<T>`, see [kotlin-logging-adoption.md](kotlin-logging-adoption.md)) instead of throwing.
- A library can hand out geometry that its callers cannot mutate, without copying it.
- The concrete types keep their `equals`/`hashCode` semantics: same concrete class and same values; the hash folds in the class.
- All of it is common code for `jvm`, `js` and `wasmJs`, compiled at `languageVersion`/`apiVersion` 2.2 ([kmp-build-and-kotlin-compat.md](kmp-build-and-kotlin-compat.md)).
- The 3.0.0 breaks are exactly: `Color`'s primary constructor becomes `internal`; `TwoDoubles` is no longer sealed; `Point` and `Dimensions` become immutable; `divideBy` returns a `Result`; `evaluateList` takes `(T) -> Boolean`; the `com.spartanlabs.logging` package is removed. Anything else that an option needs is named as a further break.
- Geometry sits on game hot paths, so allocation cost matters on the JVM, JS and Wasm.

## 2. Bottom line

- **Live state is a `var` holding an immutable value; do not add a mutable twin.** `location += delta` on a `var` compiles to `location = location + delta` once `plusAssign` is gone, `copy(x = ...)` replaces single-component writes, and a read-only/mutable pair would double the surface, split equality, and repeat what korlibs has since deprecated (`MPoint`, "Use Point instead"). The cost is one 32-byte allocation per update on the JVM, which is a rounding error at game scale and avoidable with scalar-first code where measured.
- **`TwoDoubles` stays a non-generic abstract class** holding the data and the invariant-keeping members (`final equals`/`hashCode`, `containsNaN`), with a `protected abstract fun create(first, second)` factory. Arithmetic lives per concrete type (`Point`, `Dimensions`, a consumer's own type). A recursive-generic `TwoDoubles<T : TwoDoubles<T>>` is rejected because every existing `TwoDoubles` reference without a type argument stops compiling: a break outside the set. An interface is rejected because it cannot carry `equals`/`hashCode`.
- **`divideBy` is `open infix fun divideBy(magnitude: Double): Result<TwoDoubles>` on the base**, narrowed covariantly to `Result<Point>` and `Result<Dimensions>` on the concrete types. No `div` operator in 3.0.0: adding one later is free, removing one is a break, and leaving it out makes `p /= 2.0` a loud compile error that points at `divideBy` rather than a silent move from "throws on zero" to "returns Infinity". The covariant `Result` override has not been compiled; section 8 gives the spike and the fallback.
- **`Square` stays a mutable holder** (`var location`, `var dimensions` of immutable types, still a `data class`): source-compatible, no further break. **No read-only view type is added.** `Point` and `Dimensions` are their own read-only view; the existing `AxisAlignedBox` is the read-only view of a `Square` (and `CenteredBox` is already an immutable box). Making `Square` immutable would be a further break and is not recommended.
- **Equality: fix it monotonically.** Keep IEEE `==` for the zeros (so `0.0` still equals `-0.0`, which `Actor.isAtDestination = location == destination` in MyGameTools relies on), make `NaN` equal `NaN` (so equality is reflexive), and hash `-0.0` like `0.0` and every `NaN` alike. Every pair that is equal today stays equal; only `NaN`-versus-`NaN` pairs change from unequal to equal. This is a contract change and is stated as one. Total-order equality (what Kotlin data classes and Java records give) is the alternative and would split `0.0`/`-0.0`.
- **Tier: Experimental for subclassing `TwoDoubles`** (`@SubclassOptInRequired` with a `@RequiresOptIn` marker); everything else in the model is Stable Core. Confidence is medium: the evidence says "unproven", not "likely and core".

## 3. Findings

Evidence tiers: **consensus** (multiple independent sources, or the language's own documentation/tests), **practice** (what mature libraries visibly do), **opinion** (one source), **source-read** (read from source at the pinned version, not executed).

### 3.1 Live mutable state: a `var` of an immutable value is the idiom (practice + consensus)

- Kotlin's augmented-assignment rule makes this work with no extra machinery: for `a += b`, if `plusAssign` is absent the compiler generates `a = a + b`, with a type check; if both `plus` and `plusAssign` exist and `a` is a `var`, it is an ambiguity error ([operator overloading](https://kotlinlang.org/docs/operator-overloading.html), read 2026-10-17; claim 1). Two consequences for 3.0.0:
  - The assign operators must go, not merely be supplemented. Keeping `plusAssign` next to a new `plus` would make `+=` on a `var Point` a compile error.
  - A holder that today declares `val location: Point` and writes `location += delta` must become `var`.
- Every comparable that has operators returns new instances from them: Compose `Offset.plus/minus/times/div` ([Offset.kt @ 62841bb](https://github.com/androidx/androidx/blob/62841bbfbf6d1f0a79ca386cd8ce71182868ef28/compose/ui/ui-geometry/src/commonMain/kotlin/androidx/compose/ui/geometry/Offset.kt)), OPENRNDR `Vector2.plus`, Kool `Vec2d.plus`, kotlin-math `Float2.plus`, korlibs `Vector2D.plus`. Single-component updates use `copy(x = ..., y = ...)` (Compose `Offset.copy`, `Size.copy`; data-class `copy` in OPENRNDR and korlibs).
- Replacement mapping for the removed mutators:

  | removed | replacement |
  |---|---|
  | `p setTo q` / `p.setTo(a, b)` | `p = q` / `p = Point(a, b)` |
  | `p modBy q` / `p += q` | `p += q` (on a `var`; compiles through `plus`) |
  | `p -= q` | `p -= q` (through `minus`) |
  | `p amplifyBy k` / `p *= k` | `p *= k` (through `times`) |
  | `p /= k` | `p = (p divideBy k).getOrElse { p }`, or `p *= 1 / k` when `k` is known non-zero |
  | `p.x = v` | `p = p.copy(x = v)` |
  | `d.width = v` | `d = d.copy(width = v)` |

### 3.2 A read-only/mutable pair is a poor fit here (practice)

- **korlibs** documented exactly this convention (`interface IType { val ... }`, `class MType : IType(override var ...)`, `fun doSomethingWith(a: IType, out: MType = MType()): MType`) and later marked its mutable point `@Deprecated("Use Point instead")` and `@KormaMutableApi`; its immutable `Vector2D`/`Size2D` are data classes ([_MathGeomMutable.kt](https://github.com/korlibs/korlibs/blob/94e645c59f3c54a268cba01d566e0a0b4654207a/korlibs-math/src/commonMain/kotlin/korlibs/math/geom/_MathGeomMutable.kt), [_Math_annotations.kt](https://github.com/korlibs/korlibs/blob/94e645c59f3c54a268cba01d566e0a0b4654207a/korlibs-math-core/src/commonMain/kotlin/korlibs/math/annotations/_Math_annotations.kt), [VectorsDouble.kt](https://github.com/korlibs/korlibs/blob/94e645c59f3c54a268cba01d566e0a0b4654207a/korlibs-math-vector/src/commonMain/kotlin/korlibs/math/geom/VectorsDouble.kt); claim 8).
- **Kool** keeps the pair: `open class Vec2d(open val x, open val y)` with `MutableVec2d(override var x, override var y) : Vec2d`. Its `Vec2d.equals` is `other !is Vec2d` then `x == other.x && y == other.y`, so a mutable and an immutable vector can be equal, and a `MutableVec2d` can be passed where an immutable value is assumed and changed later ([Vec2.kt @ v0.19.0](https://github.com/kool-engine/kool/blob/v0.19.0/kool-core/src/commonMain/kotlin/de/fabmax/kool/math/Vec2.kt); claim 7).
- **Kotlin's own pair**: `List`/`MutableList` are a read-only view, not an immutable value: "Functions in this interface support only read-only access to the list; read/write access is supported through the MutableList interface." ([List KDoc](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/); claim 17). A holder of a `List` cannot tell whether someone else changes it.
- For this repo the pair has four concrete costs:
  1. Equality: the contract is "same concrete class, same values". A `MutablePoint` that is not equal to the `Point` with the same values is surprising; one that is equal breaks the contract.
  2. Retrofitting a shared read-only interface as the type of existing members (`Segment.a`, `Ray.origin`, `CenteredBox.center`, `AxisAlignedBox.min`) is a further source break.
  3. Hash keys: a mutable point inside a `Segment` or `Zone`-style key is the hazard that immutability removes.
  4. It adds a seam with no consumer evidence, so it would need its own tier (Experimental).
- What the pair buys, allocation-free in-place update, is available more cheaply (section 3.9).

### 3.3 `TwoDoubles` as an open seam: base class, not interface, not generic (practice + reasoning)

- **Interface (option b) cannot carry the invariants.** Kotlin does not allow an interface to provide `equals`/`hashCode` defaults, so "same concrete class, same values, hash folds the class" could not be inherited by a consumer's type; each implementor would rewrite it. That is the opposite of the requirement that consumers' types get the contract for free. The repo's own rule also puts value/domain types on inheritance and reserves interface-plus-default for systems.
- **No shared base (option c)** removes `TwoDoubles` altogether, breaking `TwoDoublesSnapshot.from(twoDoubles: TwoDoubles)` in MyGameTools and the stated purpose of the seam.
- **A recursive generic is what comparable code uses, but only on an interface and only for arithmetic.** OPENRNDR: `interface LinearType<T : LinearType<T>> { operator fun plus(right: T): T; ... }` ("define custom mathematical types") and `data class Vector2(val x: Double, val y: Double) : LinearType<Vector2>, EuclideanVector<Vector2>, GeometricPrimitive2D` ([Vector2.kt](https://github.com/openrndr/openrndr/blob/v0.5.0/openrndr-math/src/commonMain/kotlin/org/openrndr/math/Vector2.kt), [LinearType.kt](https://github.com/openrndr/openrndr/blob/v0.5.0/openrndr-math/src/commonMain/kotlin/org/openrndr/math/LinearType.kt); claim 9). libGDX: `public interface Vector<T extends Vector<T>>` with `Vector2 implements Vector<Vector2>` ([Vector.java @ 1.14.2](https://github.com/libgdx/libgdx/blob/1.14.2/gdx/src/com/badlogic/gdx/math/Vector.java); claim 10). In both, the data classes do not share a state-bearing base.
- **Putting the type parameter on `TwoDoubles` itself is a source break.** `TwoDoubles<T : TwoDoubles<T>>` makes every mention of plain `TwoDoubles` an error ("one type argument expected"). MyGameTools has one such mention (`TwoDoublesSnapshot.from(twoDoubles: TwoDoubles)`); the point of unsealing is that consumers will write more. Binary compatibility is unaffected (erasure), source compatibility is not. Not in the break set, so rejected.
- **An additive generic layer between `TwoDoubles` and `Point`** (for example `abstract class Vector2Like<S : Vector2Like<S>> : TwoDoubles()`) avoids that break and lets `plus/minus/times` be written once. Rejected for 3.0.0: it makes `Point : Vector2Like<Point>` part of `Point`'s public supertype list (not removable without a break), no comparable library exposes a state-bearing recursive base, and nothing yet shows consumers want the shared operators. It can be added later as an experimental layer if a consumer writes the same five operators a second time.
- **Per-type operators are what the evidence supports.** Kool's `Vec2f`/`Vec2d`, OPENRNDR, kotlin-math and Compose all hand-write the operators per type.
- **The factory.** `divideBy` must build an instance of the receiver's concrete type, which the base cannot do without a hook. A `protected abstract fun create(first: Double, second: Double): TwoDoubles` gives the base that hook. `TwoDoubles` was sealed, so no outside subclass exists and adding an abstract member breaks nobody.
- **Members that stay on the base.** `first`, `second` (`val`, not `open`), the copy constructor, `containsNaN`, `final equals`, `final hashCode`, an overridable `toString`, and `divideBy`. `equals`/`hashCode` are `final` because they are the invariant (same concrete class and values; hash folds the class); a type with extra state composes a `TwoDoubles` instead of extending it. Opening a `final` member later is source- and binary-compatible; closing it is not, so `final` is the reversible choice.

### 3.4 `divideBy` and the `div` operator (consensus on `div`, reasoning on the rest)

- Every comparable with a scalar `div` makes it total, IEEE: Compose `Offset.div(operand: Float)` is `Offset(x / operand, y / operand)`; OPENRNDR `Vector2.div(scale: Double)`; Kool `Vec2d.div(that: Double)`; kotlin-math `Float2.div(v: Float)`; korlibs `Vector2D.div(scale: Double)`. None returns a `Result`. OPENRNDR's `EuclideanVector.normalized` returns `zero` for a zero-length vector rather than failing. GeneralTools differs deliberately: `normalized()` and `divideBy` report degenerate input as an expected failure.
- An operator that returns `Result<Point>` is not usable with augmented assignment: `p /= 2.0` becomes `p = p / 2.0` and fails the type check, because the result is not a `Point` (claim 1). So the choice is between a total `div` and no `div`.
- **Recommendation: no `div` in 3.0.0.** Reasons: (1) the old `p /= 0.0` threw; a total `div` would silently turn it into an `Infinity`/`NaN` point, the one case that `divideBy` reports as an expected failure; (2) two division spellings with different zero behaviour invite confusion; (3) adding `div` later is a minor-version addition, removing it is a break; (4) without `div`, `p /= k` is a compile error that names the missing operator, which is a good migration signal. The cost is `p * (1 / k)` or `divideBy` at call sites that divide by a known non-zero literal. If the maintainer prefers parity with the other libraries, ship a total IEEE `div` and document that `divideBy` is the guarded form.
- **The result type.** `Result<out T>` is covariant, so `Result<Point>` is a subtype of `Result<TwoDoubles>` and a narrowing override is type-correct ([Result.kt @ v2.2.0](https://github.com/JetBrains/kotlin/blob/v2.2.0/libraries/stdlib/src/kotlin/util/Result.kt), cited in [kotlin-logging-adoption.md](kotlin-logging-adoption.md)). Whether the compiler and the JVM backend accept the override cleanly for an inline-class return type was not established (Gaps).
- **Silent hazard.** `p divideBy 2.0` written as a statement still compiles after 3.0.0 and now does nothing, because the result is discarded. The other removed mutators fail to compile; this one does not. Kotlin has an Experimental unused-return-value checker (`-Xreturn-value-checker`, default `disable`) but it is opt-in, flags only functions marked `@MustUseReturnValues`, and was not checked against `apiVersion` 2.2 ([unused return value checker](https://kotlinlang.org/docs/unused-return-value-checker.html); claim 16), so it cannot be the mitigation. The migration notes must name `divideBy`.
- **Arithmetic.** The old `divideBy` multiplied by the reciprocal (`amplifyBy(1/magnitude)`); Kool still does (`mul(1f / div)`). Dividing each component is more accurate and differs from the reciprocal product by at most an ulp on most inputs, and it differs on subnormal divisors (the reciprocal overflows to `Infinity`). Preserving or changing it is the maintainer's call; the signature does not depend on it.

### 3.5 Equality: what comparable types do, and the pre-existing defect (source-read; consensus for the Kotlin/Java default)

`TwoDoubles.equals` compares with `==` on statically typed `Double`, which is IEEE; `hashCode` uses `Double.hashCode`, which is bit-based on the JVM and Wasm ([cross-platform-behaviour-parity.md](cross-platform-behaviour-parity.md) section 5). Kotlin's documentation states both semantics: statically typed operands follow IEEE 754, and "in generic use cases (such as `Any`, `Comparable<...>`, or `Collection<T>`)" the `equals()`/`compareTo()` implementations apply, so `NaN` equals itself and `-0.0` is less than `0.0` ([Numbers](https://kotlinlang.org/docs/numbers.html); claim 2).

| type | `equals` on the doubles | `hashCode` | consistent? |
|---|---|---|---|
| Kotlin `data class` with a `Double` property | total order: `A(0.0) != A(-0.0)`, `A(NaN) == A(NaN)` (compiler test `dataClasses/doubleParam.kt`, run on JVM; `IGNORE_BACKEND: JS_IR`) | differs for `0.0`/`-0.0`, equal for `NaN` | yes (claim 3) |
| Java `record` | `Double.compare(...) == 0` | bit-based | yes (claim 11) |
| Compose `Rect` (data class); `Offset`/`Size` (value class over a packed `Long`) | total order / bitwise on the packed `Long` | same | yes (claim 6) |
| OPENRNDR `Vector2` (data class); korlibs `Vector2D`, `Size2D`; kotlin-math `Float2` (data class, mutable) | total order | same | yes (claims 9, 8, 12) |
| libGDX `Vector2` | `floatToIntBits` on both components, `getClass()` must match; separate `epsilonEquals` | `floatToIntBits` | yes (claim 10) |
| JavaFX `Point2D`, `Dimension2D` | IEEE `getX() == other.getX()` | `Double.doubleToLongBits` | **no**, the same defect as GeneralTools (claim 5) |
| Kool `Vec2f`/`Vec2d` | IEEE `x == other.x`, and cross-class (`other !is Vec2d`) | `Float.hashCode()`/`Double.hashCode()` | **no**, the same defect (claim 7) |
| GeneralTools `TwoDoubles` today | IEEE, same concrete class | `Double.hashCode()` folded with the class hash | **no** |

- The pinned JavaFX source contradicts the idea that `Point2D` compares by `doubleToLongBits`: only the hash uses it.
- Target behaviour of the pieces an implementation would use (all `v2.4.21`, source-read): `Double.toBits()` canonicalises `NaN` on JS (`doubleToRawBits(if (this.isNaN()) Double.NaN else this)`) and Wasm; Wasm `Double.equals` is `other is Double && this.toBits() == other.toBits()`; the JS runtime `equals(obj1, obj2)` treats `NaN` as equal to itself and separates the zeros through `1 / obj1 === 1 / obj2`; JS `getNumberHashCode` hashes `-0.0` to `0` and allocates nothing (claim 4). `Double.toBits()` returns a `Long`, which is an emulated object on Kotlin/JS, so an equality built on `toBits()` would allocate on the JS hot path; an explicit comparison does not.
- **What the choice means for the data classes holding `Point`s.** `Segment`, `Ray`, `CenteredBox` and `Square` are data classes whose generated `equals` starts with an identity check and then calls `Point.equals`/`Dimensions.equals` per property (compiler `DataClassMembersGenerator.generateEqualsMethodBody` at `v2.4.21`), so they inherit whatever `TwoDoubles` decides. Today a `Segment` with a `NaN` endpoint is not equal to its own `copy()` (the copy shares the same `Point` instances, and `Point.equals(self)` is false for `NaN`), and `listOf(nanPoint).contains(nanPoint)` is false. A `Segment` with a `-0.0` endpoint is equal to one with `0.0` but hashes differently on the JVM and Wasm, so `HashMap` lookups fail.
- **Downstream reliance on the IEEE zero rule:** MyGameTools `Actor.isAtDestination` is `location == destination`. Under total-order equality, a destination reached as `-0.0` would never compare equal to `0.0`.

Options:

| option | `0.0` vs `-0.0` | `NaN` vs `NaN` | `equals` outcomes that change | hash |
|---|---|---|---|---|
| A. Fix only the hash (normalise `-0.0` to `0.0`) | equal | unequal, not reflexive | none | consistent |
| B. A, plus an identity shortcut in `equals` | equal | an object equals itself; two `NaN` objects stay unequal | `x.equals(x)` for `NaN` only | consistent |
| **C. Monotone: IEEE, plus `NaN` equals `NaN`** | equal | equal | `NaN`-versus-`NaN` pairs go from unequal to equal | `-0.0` like `0.0`, every `NaN` alike |
| D. Total order (`Double.equals`-style) | **unequal** | equal | every `0.0`/`-0.0` pair goes from equal to unequal | bit-based (`Double.hashCode()` as is) |
| E. Leave as is | equal | unequal | none | **inconsistent** on JVM/Wasm |

- Recommendation: **C**. It is the smallest change that gives a lawful equivalence relation (reflexive, symmetric, transitive) and a hash that agrees with it, and it is monotone: it never turns an equal pair into an unequal one, so no existing `==` comparison of positions can start failing (D can). It meets the `Object` contract ("`x.equals(x)` should return `true`"; equal objects "must produce the same integer result" from `hashCode`; claim 14). It is a change of contract in one place only (`NaN` coordinates) and is stated as such in the migration notes. **A** is the fallback if the maintainer wants no `equals` outcome to change at all; it fixes the hash defect but leaves `NaN` non-reflexive.
- Approximate equality is not an `equals` job. libGDX ships `epsilonEquals`; korlibs ships `isAlmostEquals` as a separate interface. GeneralTools already has the `EPSILON` constant; an approximate comparison would be an additive extension, outside this question.

### 3.6 What is left of read-only views (reasoning; the API facts are in the repo)

- **`Point` and `Dimensions`** are immutable, so a library hands them out directly with no copy and no view. This is issue #8's first bullet and its `ZoneGrid`/`UnzonedPointException` defensive copies (`Point(space.bounds.location)`, `Point(point)`) become unnecessary.
- **`Square`** stays a live holder because live state is its job. `AxisAlignedBox` (already shipped, with `min`, `max`, `center`, `size` and a default `contains`; "Read-only contract common to every axis-aligned rectangle") is the read-only view: `location` is `min`, `dimensions` is `size`. A library exposes `val bounds: AxisAlignedBox` and the compiler stops callers writing. This answers issue #8's open points: no `ReadOnlySquare` is needed, `Square.dimensions` needs no view of its own, and `equals`/`hashCode` stay on the concrete types. As with `List` over `MutableList`, a caller can still down-cast to `Square`; a library that must prevent that exposes `object : AxisAlignedBox by square { }` (Kotlin delegation, one allocation, still live) or a snapshot (`square.copy()` is now a safe shallow copy, because the two properties are immutable). That is an idiom to document, not an API to add.
- **`CenteredBox`** is already a deeply immutable `AxisAlignedBox` (a data class of `val`s); once `Point` and `Dimensions` are immutable its "treat as immutable" KDoc becomes a guarantee, and likewise for `Segment` and `Ray`.
- **Latent aliasing fixed as a side effect.** `Square.min` returns `location` itself and `Square.size` returns `dimensions` itself; today `square.min.x = 5.0` moves the square. After immutability this is safe.
- **An immutable top-left box** does not exist (`Square` is mutable, `CenteredBox` is centre-origin). Adding one is additive and can wait for a consumer to ask.

### 3.7 `Point` and `Dimensions` stay final; copy constructors stay (reasoning)

- Final: equality is "same concrete class", so a subclass of `Point` would never equal a `Point`; the extension seam is `TwoDoubles`. Opening later is compatible. libGDX is class-strict too (`getClass() != obj.getClass()`); JavaFX's open `Point2D` uses `instanceof` and so equates subclasses.
- Copy constructors `Point(point)` and `Dimensions(dimensions)` stay: removing them is a further break, they cost one allocation, and MyGameTools uses them. Deprecating them would add a warning to every such call; warnings fail builds that use `allWarningsAsErrors`, so leave deprecation to a later minor and document in KDoc that the copy is equal to, and interchangeable with, the original.

### 3.8 Value classes cannot carry two doubles; scalar-first is the hot-path escape hatch (consensus)

- Compose's packing is `value class Offset(val packedValue: Long)` with two `Float`s in one `Long`, `x`/`y` unpacked per access ([Offset.kt @ 62841bb](https://github.com/androidx/androidx/blob/62841bbfbf6d1f0a79ca386cd8ce71182868ef28/compose/ui/ui-geometry/src/commonMain/kotlin/androidx/compose/ui/geometry/Offset.kt); claim 6). Two `Double`s are 128 bits and do not fit. `Rect` is a plain `data class` of four floats, so even Compose has no packed form beyond two-float types.
- Multi-field value classes are KEEP-0454, "Status: Experimental (phase I) in 2.5", and "unlike current (`@JvmInline`) inline value classes, MFVCs are **not** inlined by the Kotlin compiler" ([KEEP-0454](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0454-better-immutability-value-classes-MFVC.md); claim 13). This updates the SpartanGraphics note that their status was unconfirmed. They are unavailable at `languageVersion` 2.2 and, in the first phase, are boxed.
- So a per-frame update of `N` actors allocates `N` points whatever the shape. The escape hatch is the repo-wide pattern the SpartanGraphics research recommends ([idiomatic-kotlin.md](../../../../../SpartanGraphics/docs/roadmap/research/idiomatic-kotlin.md) section 3.2; uncommitted there, its Kool, Compose and `Long`-on-JS claims were re-read here, see claims 6 and 7): hot queries get scalar overloads (`contains(x, y)`, `distanceFrom(x, y)`) so a consumer holding raw doubles never creates a `Point`. Scalar overloads are additive and can be added in a minor after a measurement shows the need, so none are required in 3.0.0.

### 3.9 Allocation cost (JVM: practice; JS: practice; Wasm: not established)

- **JVM.** HotSpot does not stack-allocate; it approximates it with scalar replacement when escape analysis proves the object does not escape. It fails when "control flow merges before the access" or an opaque (non-inlined) instance call receives the object ([Shipilev, Quark 18](https://shipilev.net/jvm/anatomy-quarks/18-scalar-replacement/), updated 2019-03-03; JDK build not named; claim 18). Allocation that does survive is a TLAB pointer bump: about 2.5 GB/s of 16-byte objects on one thread ([Quark 4](https://shipilev.net/jvm/anatomy-quarks/4-tlab-allocation/); claim 18). A `Point` is 28 bytes rounded to 32 on a default 64-bit HotSpot (12-byte header plus two 8-byte fields; my arithmetic). 10,000 actors moving at 60 Hz is 600,000 allocations per second, about 19 MB/s, under 1% of that single-thread rate. Young-generation garbage costs nothing to collect; the cost is paid only on survivors.
- **JS (V8).** A `Point` is a JS object with two number fields. With pointer compression, "the double values simply do not fit into the compressed fields anymore", so a non-integral coordinate is stored as a separate boxed number object: up to three allocations per point, against one on the JVM. The post says double field unboxing may return in a pointer-compression-compatible form; the sentence above is the verbatim quote, and the post's suggestion of typed arrays or WebAssembly for number-heavy code is a paraphrase from a summary of the page, not a verified quote ([V8 blog, 2020-03-30](https://v8.dev/blog/pointer-compression); claim 15). Whether Node (the test runtime) or a given browser build has pointer compression on, and how TurboFan's escape analysis changes this for short-lived points, was not established.
- **Wasm.** Kotlin/Wasm compiles to WasmGC; `struct.new` is a VM operation, and Binaryen's `Heap2Local` pass lowers allocations that never escape a function into locals ([Heap2Local.cpp @ version_133](https://github.com/WebAssembly/binaryen/blob/version_133/src/passes/Heap2Local.cpp); claim 19). The Kotlin Gradle plugin has a `BinaryenExec` optimisation task for Wasm output. Whether that pass fires for `Point` in practice, and how V8 allocates WasmGC structs, was not established. No number for WasmGC allocation cost is claimed.
- **Failure path.** `divideBy` by zero builds an `IllegalArgumentException` eagerly and logs a WARN ([kotlin-logging-adoption.md](kotlin-logging-adoption.md)); on the JVM that includes capturing a stack trace. Do not probe with zero in a hot loop; test the divisor first.

### 3.10 Stability tier of the open `TwoDoubles` seam (reasoning from the tier definitions)

- **Mechanism exists in the language.** `@SubclassOptInRequired(markerClass)` "marks open for subclassing classes and interfaces, and makes implementation and extension of such declarations as requiring an explicit opt-in", is limited to "open and abstract classes, and non-`fun` interfaces", and is documented "Since Kotlin 2.1" with no experimental label ([stdlib KDoc](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-subclass-opt-in-required/); claim 20). Only subclassing is gated; reading `first`/`second`, taking a `TwoDoubles` parameter and using `Point` need no opt-in. It needs a `@RequiresOptIn` marker class of the library's own; GeneralTools has none, while MyGameTools has `ExperimentalGameToolsApi` and a documentary `SupportedExtension` (both in `com.spartanlabs.gaming.annotation`), which GeneralTools does not depend on.
- **Evidence for Experimental.** Nothing has subclassed `TwoDoubles` (it was sealed). The one stated near-term consumer, SpartanGraphics, chose its own `Float`-based `Point`/`Size`/`Rect` rather than extending GeneralTools' types ([idiomatic-kotlin.md](../../../../../SpartanGraphics/docs/roadmap/research/idiomatic-kotlin.md) section 6). The seam's exact shape (factory hook, covariant `divideBy`, `final equals`) is unproven, and the closest outside precedents expose a different shape (an interface with a self type, for operations only). The tier definitions say a seam "whose shape has not been proven by a real consumer yet" is Experimental and graduates once a consumer has built against it.
- **Evidence against.** Defining one's own two-double type is a stated purpose of unsealing, and an opt-in at the point of subclassing is friction for the one audience the change is for.
- **Confidence: medium.** The evidence supports "unproven", not "likely and core", so Supported Extension is not justified yet; whether the maintainer wants the stable promise from day one is a call for them. Moving Experimental to Supported Extension later costs nothing; the reverse costs a major.
- **Everything else in the model is Stable Core:** `TwoDoubles`'s data members, `Point`, `Dimensions`, `Square`, `AxisAlignedBox`. No new interface is recommended, so there is no further tier to place.

## 4. How comparable projects do it

| project (pin) | two-component types | live state / mutation | self type / shared base | `div` | equality |
|---|---|---|---|---|---|
| Compose `ui-geometry` (androidx `62841bb`) | `value class Offset(packedValue: Long)`, `Size`; `Rect` is a `data class` | none; `copy(x, y)` | none | total, IEEE | bitwise on the packed `Long`; data-class total order |
| JavaFX `Point2D`, `Dimension2D` (jfx `cb56d4c`) | `public class Point2D`, fields `final`, `add`/`subtract`/`multiply` return new | none | `Interpolatable<Point2D>` | none (`multiply` only) | IEEE `==` with `doubleToLongBits` hash |
| libGDX `Vector2` (`1.14.2`) | `public class Vector2`, public mutable `x`, `y`; `Vector2.X`/`Y`/`Zero`/`One` are public static instances | mutates in place, `cpy()` copies; `epsilonEquals` | `Vector<T extends Vector<T>>` | `scl(1/s)` style | `floatToIntBits`, `getClass()` |
| OPENRNDR (`v0.5.0`) | `data class Vector2(val x, val y)` | none | `LinearType<T : LinearType<T>>`, `EuclideanVector<T>` | total `div(scale)` | data class |
| Kool (`v0.19.0`) | `open class Vec2d(open val x, open val y)`; `MutableVec2d : Vec2d` | `set`, `plusAssign`, `add(that, result)` | none | total `div`; `divAssign` multiplies by the reciprocal | IEEE `==`, cross-class |
| korlibs (`94e645c`) | `data class Vector2D(val x, val y)`, `Size2D`; `MPoint` deprecated | `MPoint` mutable twin, deprecated | `IsAlmostEquals<T>` | total `div` | data class |
| kotlin-math (`v1.8.0`) | `data class Float2(var x, var y)` | mutable fields, `inline operator fun plus` allocates | none | total `div` | data class |
| Kotlin stdlib collections | `List`/`MutableList` | read-only interface plus mutable subtype | n/a | n/a | n/a |

Signatures worth copying: Compose `public fun copy(x: Float = unpackFloat1(packedValue), y: Float = unpackFloat2(packedValue)): Offset`; OPENRNDR `override operator fun plus(right: Vector2) = Vector2(x + right.x, y + right.y)`; JavaFX `public Point2D add(Point2D point)`. The two precedents for the exact problem GeneralTools has (a value pair with a mutable twin) end in the same place: korlibs deprecated the twin; Kool keeps it and carries the cross-class equality and "is a mutable vector an immutable one" questions with it.

## 5. Pitfalls & regrets

- **Silent aliasing break in MyGameTools.** `VisibleObject(area: Square) : GameObject(area.location)` shares one `Point` between the game object and its `Square`; `Actor` then moves it with `location += locmod`. With immutable `Point`, flipping `GameObject.location` from `val` to `var` so that `+=` compiles gives the game object its own variable: `area.location` goes stale and nothing fails to compile. The fix is a single owner of the position (`VisibleObject` overrides `location` to read and write `area.location`, so `GameObject.location` must be `open var`). A mutable twin keeps the aliasing working; that is the strongest argument for the pair, and the reason the migration notes must call out shared `Point` fields explicitly.
- **`p divideBy k` as a statement compiles and does nothing** (section 3.4).
- **`+=` on a `var` of a shared object** (a field copied between holders) now rebinds the variable instead of mutating the shared instance, so other holders no longer see the change. The same class of silent change as the first bullet.
- **Keeping `plusAssign` and adding `plus`** makes `+=` on a `var` of that type a compile error.
- **Making `TwoDoubles` generic** breaks source for every plain `TwoDoubles` mention.
- **Total-order equality** splits `0.0` and `-0.0`; `-0.0` is easy to produce (`0.0 * -1.0`, negation), so `position == Point()` after scaling by a negative would start failing.
- **`this::class` in `equals`** allocates a `ClassReference` on the JVM where `javaClass` did not; put an identity shortcut first and only add an `expect`/`actual` class comparison if a measurement asks ([cross-platform-behaviour-parity.md](cross-platform-behaviour-parity.md) section 3.7). Do not use `KClass.qualifiedName`.
- **libGDX's public mutable constants** (`Vector2.Zero`, `.One`, `.X`, `.Y` are `public final static` instances of a mutable class) are the standing example of why shared geometry constants must be immutable. With immutable `Point`, a `Point.ORIGIN` constant would be safe to add later; it is not needed now.
- **Data classes freeze `copy` and `componentN`.** `Square` is already a `data class`, so its property order is frozen; do not turn `Point` into one.

## 6. Recommendation for this repo

Take the "immutable values, per-type operators, non-generic open base" shape. Sketch of the public signatures (shape, not final code; KDoc at the repo's Level-2 standard on every member):

```kotlin
// common
@SubclassOptInRequired(ExperimentalGeneralToolsApi::class)   // marker: a new @RequiresOptIn class; name is the maintainer's
abstract class TwoDoubles(val first: Double = 0.0, val second: Double = 0.0) {
    constructor(doubles: TwoDoubles) : this(doubles.first, doubles.second)
    protected abstract fun create(first: Double, second: Double): TwoDoubles
    val containsNaN: Boolean get() = first.isNaN() || second.isNaN()
    open infix fun divideBy(magnitude: Double): Result<TwoDoubles>   // zero (including -0.0): log.failure(...), IllegalArgumentException
    final override fun equals(other: Any?): Boolean                  // same concrete class; IEEE per component, NaN == NaN
    final override fun hashCode(): Int                               // class folded in; -0.0 hashes as 0.0, every NaN alike
    override fun toString(): String                                  // "first, second", unchanged
}

class Point(x: Double = 0.0, y: Double = 0.0) : TwoDoubles(x, y) {   // final
    constructor(point: Point) : this(point.x, point.y)                // kept
    val x: Double get() = first
    val y: Double get() = second
    operator fun plus(other: TwoDoubles): Point                       // operand type as the old plusAssign, so p + dims still compiles
    operator fun minus(other: TwoDoubles): Point
    operator fun times(factor: Double): Point
    operator fun unaryMinus(): Point
    fun copy(x: Double = this.x, y: Double = this.y): Point
    override infix fun divideBy(magnitude: Double): Result<Point>
    infix fun distanceFrom(other: Point): Result<Double>              // unchanged
    protected override fun create(first: Double, second: Double): Point
}

class Dimensions(width: Double = 0.0, height: Double = 0.0) : TwoDoubles(width, height) {   // final
    constructor(dimensions: Dimensions) : this(dimensions.width, dimensions.height)           // kept
    val width: Double get() = first
    val height: Double get() = second
    operator fun plus(other: TwoDoubles): Dimensions
    operator fun minus(other: TwoDoubles): Dimensions
    operator fun times(factor: Double): Dimensions
    fun copy(width: Double = this.width, height: Double = this.height): Dimensions
    override infix fun divideBy(magnitude: Double): Result<Dimensions>
    protected override fun create(first: Double, second: Double): Dimensions
}

// unchanged signature: data class Square(var location: Point = Point(), var dimensions: Dimensions = Dimensions()) : AxisAlignedBox
// unchanged: AxisAlignedBox, CenteredBox, Segment, Ray, Vectors.kt, Intersections.kt
```

Equality and hash sketch (explicit comparisons, no `Long`):

```kotlin
private fun Double.sameValueAs(o: Double) = this == o || (isNaN() && o.isNaN())
private fun Double.valueHash() = when { this == 0.0 -> 0; isNaN() -> Double.NaN.hashCode(); else -> hashCode() }
// equals: this === other || (other is TwoDoubles && other::class == this::class && first.sameValueAs(other.first) && ...)
// hashCode: 31 * (31 * this::class.hashCode() + first.valueHash()) + second.valueHash()
```
On the JVM and Wasm this leaves every non-zero, non-`NaN` hash as it is today and changes only `-0.0` (now `0`).

Reasoning: immutable values with per-type operators are what every comparable that ships operators does, and they need the least machinery; the non-generic base is the only way to make `TwoDoubles` open without changing how existing code names it; the monotone equality fix removes a real defect without risking the downstream positions that rely on IEEE zeros.

**Rejected, and why:** a mutable twin (equality split, doubled surface, aliasing hazard moves rather than disappears, korlibs reversed it); a recursive-generic `TwoDoubles<T>` (source break); a `TwoDoubles` interface (cannot hold `equals`/`hashCode`); a `div` operator in 3.0.0 (irreversible, silently drops the zero policy); an immutable `Square` (further break); total-order equality (not monotone); a new `ReadOnlyPoint`/`ReadOnlySquare` interface (no remaining job).

**Options that would add a break beyond the 3.0.0 set, named:**

- `TwoDoubles<T : TwoDoubles<T>>`: source break for every `TwoDoubles` reference without a type argument (MyGameTools: `TwoDoublesSnapshot.from`).
- `TwoDoubles` as an interface: binary break for compiled callers (class to interface), loss of the shared `equals`/`hashCode`/copy constructor.
- No shared base: removes the type.
- Making `Square` immutable: `Square.location` and `Square.dimensions` lose their setters (source and binary); GeneralTools' own `SquareTest` ("mutating a square's location") and MyGameTools' `VisibleObject` (`area.dimensions = value`) assign them.
- Removing the `Point`/`Dimensions` copy constructors.
- Putting `divideBy` on `Point`/`Dimensions` only (the fallback in section 8): source break for callers holding a plain `TwoDoubles`.
- Retyping `Segment.a`, `Ray.origin`, `CenteredBox.center`, `AxisAlignedBox.min` to a new read-only interface (the read-only/mutable pair).
- Keeping `plusAssign`/`minusAssign`/`timesAssign` beside `plus`/`minus`/`times`: `+=`, `-=`, `*=` on a `var` of that type stop compiling (ambiguity).
- `div` returning `Result`: not an extra break (`/=` goes with the removed `divAssign`), but it makes `p / k` unusable in arithmetic expressions.
- Total-order equality: no source break; a behaviour change for `0.0`/`-0.0` pairs.

**Test implications** (levels and placement belong to the test plan): the twelve mutator tests in `PointTest`/`TwoDoublesTest` become operator and `copy` tests; add Level-4a laws for equality (reflexive on `NaN`, `0.0` equals `-0.0`, equal implies equal hash, `Point` differs from `Dimensions` with the same values), run on all three targets; a `divideBy` test for `0.0`, `-0.0` and a success case; a test that a consumer subclass inherits the equality contract.

## 7. Load-bearing claims

1. **Augmented assignment compiles through `plus` when `plusAssign` is absent, and is an ambiguity error when both exist on a `var`.** "If the corresponding binary function (that means `plus()` for `plusAssign()`) is available too, `a` is a mutable variable, and the return type of `plus` is a subtype of the type of `a`, report an error (ambiguity)." and "Otherwise, try to generate code for `a = a + b` (this includes a type check: the type of `a + b` must be a subtype of `a`)." Source: <https://kotlinlang.org/docs/operator-overloading.html>, read 2026-10-17.
2. **Kotlin floating-point equality depends on the static type.** "When the operands are statically known to be `Float` or `Double`, operations on the numbers and the range that they form follow the IEEE 754 Standard for Floating-Point Arithmetic." and "In these cases, Kotlin uses the `equals()` and `compareTo()` implementations for `Float` and `Double`. As a result: `NaN` is considered equal to itself ... `-0.0` is considered less than `0.0`". Source: <https://kotlinlang.org/docs/numbers.html>, read 2026-10-17.
3. **A Kotlin data class with a `Double` property uses total-order equality, and its hash agrees.** Compiler test data: `if (A(+0.0) == A(-0.0)) return "Fail: +0.0 == -0.0"`, `if (A(+0.0).hashCode() == A(-0.0).hashCode()) return "Fail: hash(+0.0) == hash(-0.0)"`, `if (A(NAN) != A(NAN)) return "Fail: NaN != NaN"`, with `// IGNORE_BACKEND: JS_IR`. Source: <https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/testData/codegen/box/dataClasses/doubleParam.kt>, tag `v2.4.21`. Generated data-class `equals` begins `+irIfThenReturnTrue(irEqeqeq(irThis(), irOther()))` then compares each property with `irEquals`: <https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/ir/ir.tree/src/org/jetbrains/kotlin/ir/util/DataClassMembersGenerator.kt>.
4. **Per-target `Double` primitives.** Wasm: `public actual override fun equals(other: Any?): Boolean = other is Double && this.toBits() == other.toBits()` (<https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/stdlib/wasm/builtins/kotlin/Primitives.kt>). JS runtime: `return obj1 === obj2 && (obj1 !== 0 || 1.asDynamic() / obj1 === 1.asDynamic() / obj2)` and `if (obj1 !== obj1) { return obj2 !== obj2 }` (<https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/stdlib/js/runtime/coreRuntime.kt>); JS `public actual fun Double.toBits(): Long = doubleToRawBits(if (this.isNaN()) Double.NaN else this)` (<https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/stdlib/js/src/kotlin/NumbersJs.kt>); JS hash `if (jsBitwiseOr(obj, 0).unsafeCast<Double>() === obj) { return obj.toInt() }` (<https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/stdlib/js/runtime/bitUtils.kt>). All `v2.4.21`.
5. **JavaFX `Point2D.equals` is IEEE and `hashCode` is bit-based.** `return getX() == other.getX() && getY() == other.getY();` and `bits = 31L * bits + Double.doubleToLongBits(getX());`; the class is declared `public class Point2D implements Interpolatable<Point2D>`. Source: <https://github.com/openjdk/jfx/blob/cb56d4c90fe7a43079688b92c54c7d2b364c7232/modules/javafx.graphics/src/main/java/javafx/geometry/Point2D.java>, commit `cb56d4c`. `Dimension2D` has the same pattern.
6. **Compose `ui-geometry` shapes.** `@kotlin.jvm.JvmInline public value class Offset(public val packedValue: Long)`; `public fun copy(x: Float = unpackFloat1(packedValue), y: Float = unpackFloat2(packedValue)): Offset`; `public operator fun div(operand: Float): Offset { return Offset(packFloats(unpackFloat1(packedValue) / operand, unpackFloat2(packedValue) / operand)) }`; `Rect` is `public data class Rect(`. Source: <https://github.com/androidx/androidx/tree/62841bbfbf6d1f0a79ca386cd8ce71182868ef28/compose/ui/ui-geometry/src/commonMain/kotlin/androidx/compose/ui/geometry> (`Offset.kt`, `Size.kt`, `Rect.kt`), commit `62841bb`.
7. **Kool pairs an immutable `open class` with a mutable subclass and compares with IEEE.** `open class Vec2d(open val x: Double, open val y: Double)`, `open class MutableVec2d(override var x: Double, override var y: Double) : Vec2d(x, y)`, `if (other !is Vec2d) return false; return x == other.x && y == other.y`, `override fun hashCode(): Int { var result = x.hashCode()`. Source: <https://github.com/kool-engine/kool/blob/v0.19.0/kool-core/src/commonMain/kotlin/de/fabmax/kool/math/Vec2.kt>, tag `v0.19.0`.
8. **korlibs deprecated its mutable point and documented the twin convention.** `@KormaMutableApi @Deprecated("Use Point instead") data class MPoint(var x: Double, var y: Double)`; the annotations file: "Mutable APIs follow the following convention: ... `interface IType { val ... }` `class MType : IType(override var ...) : IType`"; `data class Vector2D(val x: Double, val y: Double) : IsAlmostEquals<Vector2D>`. Source: <https://github.com/korlibs/korlibs/tree/94e645c59f3c54a268cba01d566e0a0b4654207a/korlibs-math/src/commonMain/kotlin/korlibs/math/geom> (`_MathGeomMutable.kt`), `.../korlibs-math-core/.../annotations/_Math_annotations.kt`, `.../korlibs-math-vector/.../VectorsDouble.kt`, commit `94e645c`.
9. **OPENRNDR uses an immutable data class plus a self-typed interface for custom types.** "`interface LinearType<T : LinearType<T>>`" documented as "This interface enforces a linear structure that can be used to define custom mathematical types."; `data class Vector2(val x: Double, val y: Double) : LinearType<Vector2>, EuclideanVector<Vector2>, GeometricPrimitive2D`; `override operator fun div(scale: Double) = Vector2(x / scale, y / scale)`. Source: <https://github.com/openrndr/openrndr/tree/v0.5.0/openrndr-math/src/commonMain/kotlin/org/openrndr/math> (`LinearType.kt`, `Vector2.kt`), tag `v0.5.0`.
10. **libGDX `Vector2`.** `public class Vector2 implements Serializable, Vector<Vector2>`; `if (getClass() != obj.getClass()) return false;`, `if (NumberUtils.floatToIntBits(x) != NumberUtils.floatToIntBits(other.x)) return false;`; `public final static Vector2 Zero = new Vector2(0, 0);`; `public interface Vector<T extends Vector<T>>`. Source: <https://github.com/libgdx/libgdx/blob/1.14.2/gdx/src/com/badlogic/gdx/math/Vector2.java> and `.../Vector.java`, tag `1.14.2`.
11. **Java records compare floating-point components with `compare`.** "If the component is of a primitive type, using the corresponding primitive wrapper class `PW` ... the component is considered equal if and only if `PW.compare(this.c, r.c)` would return `0`." Source: <https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html>, Java 21 API.
12. **kotlin-math is a mutable data class with allocating operators.** `data class Float2(var x: Float = 0.0f, var y: Float = 0.0f) {` and `inline operator fun plus(v: Float2) = Float2(x + v.x, y + v.y)`. Source: <https://github.com/romainguy/kotlin-math/blob/v1.8.0/src/commonMain/kotlin/dev/romainguy/kotlin/math/Vector.kt>, tag `v1.8.0`.
13. **Multi-field value classes are not available and are boxed at first.** "**Status**: Experimental (phase I) in 2.5" and "Unlike current (`@JvmInline`) inline value classes, MFVCs are **not** inlined by the Kotlin compiler." Source: <https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0454-better-immutability-value-classes-MFVC.md>, read 2026-10-17 (branch `main`, not pinned to a commit).
14. **The `Object` contract.** "It is _reflexive_: for any non-null reference value `x`, `x.equals(x)` should return `true`." and "If two objects are equal according to the `equals` method, then calling the `hashCode` method on each of the two objects must produce the same integer result." Source: <https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html>, Java 21 API.
15. **V8 and double fields.** "With Pointer Compression enabled, the double values simply do not fit into the compressed fields anymore." (only this sentence is verbatim; the page's typed-array/WebAssembly suggestion is not quoted). Source: <https://v8.dev/blog/pointer-compression>, 30 March 2020. Context: [V8 commit "[ptr-compr] Disable double fields unboxing"](https://chromium-review.googlesource.com/c/v8/v8/+/1835546): "Double field unboxing optimization is incompatible with pointer compression".
16. **The unused-return-value checker is opt-in.** "`disable` disables the unused return value checker (default)." and "`check` enables the checker, and reports warnings for ignored results from marked functions." Source: <https://kotlinlang.org/docs/unused-return-value-checker.html>, read 2026-10-17. Experimental status of the `returnsResultOf()` contract: <https://kotlinlang.org/docs/whatsnew24.html>.
17. **Kotlin's read-only collection interface.** "Functions in this interface support only read-only access to the list; read/write access is supported through the MutableList interface." Source: <https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/>, read 2026-10-17.
18. **HotSpot scalar replacement and TLAB allocation.** "Hotspot does not do stack allocations per se, but it does approximate that with Scalar Replacement."; "The most trivial breakage in current Hotspot EA is when control flow merges before the access."; "Calling non-inlined — and thus opaque for current analysis — instance method bails."; "we are able to pull off 2.5 GB/sec allocation rate in a single thread". Sources: <https://shipilev.net/jvm/anatomy-quarks/18-scalar-replacement/> (updated 2019-03-03) and <https://shipilev.net/jvm/anatomy-quarks/4-tlab-allocation/>.
19. **Binaryen can remove non-escaping WasmGC allocations.** "Find heap allocations that never escape the current function, and lower the allocation's data into locals. That is, avoid allocating a GC object, and instead use one local for each of its fields." Source: <https://github.com/WebAssembly/binaryen/blob/version_133/src/passes/Heap2Local.cpp>, tag `version_133` (2026-09-21).
20. **`@SubclassOptInRequired`.** "Annotation that marks open for subclassing classes and interfaces, and makes implementation and extension of such declarations as requiring an explicit opt-in." and "Uses of this annotation are limited to open and abstract classes, and non-`fun` interfaces." Page shows "Since Kotlin 2.1". Source: <https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-subclass-opt-in-required/>, read 2026-10-17.

## 8. Gaps

- **Nothing was compiled.** Items a short spike in a scratch Gradle module settles, in this order:
  1. A covariant override `override infix fun divideBy(magnitude: Double): Result<Point>` of `open infix fun divideBy(magnitude: Double): Result<TwoDoubles>` compiles, links and returns correctly on `jvm`, `js` and `wasmJs`. `Result` is an inline class and no comparable code overriding with a narrowed `Result` was found. **Fallback if it fails:** keep `Result<TwoDoubles>` on the base and give `Point` and `Dimensions` a differently-named typed form, or move `divideBy` to the concrete types only, which is a named further break (callers holding a plain `TwoDoubles`; none exist in MyGameTools).
  2. `@SubclassOptInRequired` on `TwoDoubles` with `@OptIn` on `Point`/`Dimensions` compiles in common code at `languageVersion` 2.2 and does not leak an opt-in requirement to code that only uses `Point`.
  3. The equality and hash sketch passes the Level-4a laws on all three targets, including `-0.0` and several `NaN` payloads on JS. JS handling of different `NaN` payloads was not read; the sketch hashes every `NaN` alike for that reason.
  4. The delegation idiom `object : AxisAlignedBox by square {}` also delegates the interface's default `contains`.
- **Data-class equality on `js` is not established.** The compiler test that fixes total-order semantics is marked `IGNORE_BACKEND: JS_IR`; the JS runtime `equals` helper has total-order semantics but the code the JS backend emits for a data class's `Double` property was not read. This matters only to the table in section 3.5, not to the recommendation, which uses explicit comparisons.
- **Allocation cost on JS and Wasm is reasoned, not measured.** Not established: whether Node or the browsers a consumer targets run V8 with pointer compression; what TurboFan's escape analysis does to short-lived `Point`s; how V8 allocates WasmGC structs or stores `f64` fields; whether Binaryen's `heap2local` fires on Kotlin/Wasm output for these classes; the Kotlin/Wasm struct layout of `Point`. The 32-byte and 19 MB/s figures are my arithmetic. A benchmark would settle the matter, but nothing in the recommendation depends on it.
- **Tier choice is a judgement.** The evidence supports Experimental; the cost of choosing Supported Extension instead is only the removal of the opt-in friction and the commitment to the current base-class shape. Which marker annotation name and package GeneralTools uses, and whether it reuses a shared marker, is the maintainer's call; GeneralTools has none today.
- **`div`** is a trade-off with a recommendation (omit), not an established fact.
- **Reciprocal versus true division in `divideBy`** stays open: today's code multiplies by the reciprocal; the signature does not depend on the choice.
- **Downstream migration size was read only enough to compare options.** In MyGameTools `src/main`: three `setTo` sites (`Actor.stepTowardsDestination`, `Alive` respawn and `updateHealthBar`), two `location +=` sites, one `healthBar.dimensions.width = ...`, 21 files importing the geometry types, one use of `TwoDoubles` as a type; about 30 more in tests. The full sweep, and the `VisibleObject`/`GameObject` aliasing fix, are the downstream-blast-radius question's job ([README.md](README.md) question 8).
- **Items from the SpartanGraphics note that were not re-verified:** "`Long` is emulated on Kotlin/JS" and its performance cost (not needed for the recommendation, since packing is rejected on the `Double` width alone); soywiz's posts on Korma's value-class plans and pooled temporaries.
- **No Kotlin geometry library with a state-bearing recursive-generic base class was found**, among the libraries read for this report; the absence is from reading those libraries, not from an exhaustive search.
