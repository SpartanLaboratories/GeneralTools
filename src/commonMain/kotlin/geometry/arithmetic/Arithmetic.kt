/**
 * Value-returning arithmetic for [com.spartanlabs.geometry.Point] and [com.spartanlabs.geometry.Dimensions]:
 * `plus`, `minus`, `times` and `div` operators that build a new value and leave their operands unchanged.
 *
 * ### Opting in
 * The operators are extension functions in this package, so a file uses them only after importing them:
 *
 *     import com.spartanlabs.geometry.arithmetic.*      // every operator of the package
 *     import com.spartanlabs.geometry.arithmetic.plus   // one operator name
 *
 * An import by name brings in every overload of that name (`plus` imports `Point + Point`, `Point + Dimensions`
 * and `Dimensions + Dimensions`). `import com.spartanlabs.geometry.*` does not reach this package, because a star
 * import does not reach subpackages, and a file with no import of it resolves exactly as it did before this
 * package existed.
 *
 * ### The twelve operators
 * | Expression | Result |
 * |---|---|
 * | `Point + Point` | `Point` |
 * | `Point + Dimensions` | `Point`: the corner formula (`Rectangle.max`) |
 * | `Point - Point` | `Point` |
 * | `Point - Dimensions` | `Point`: the inverse of `Point + Dimensions` |
 * | `Point * Double` | `Point` |
 * | `Double * Point` | `Point` |
 * | `Point / Double` | `Point` |
 * | `Dimensions + Dimensions` | `Dimensions` |
 * | `Dimensions - Dimensions` | `Dimensions` |
 * | `Dimensions * Double` | `Dimensions` |
 * | `Double * Dimensions` | `Dimensions` |
 * | `Dimensions / Double` | `Dimensions` |
 *
 * ### Accepted costs
 * Importing an operator from this package has three costs, all confined to the importing file:
 * - `+=`, `-=`, `*=` and `/=` on a `var` `Point` or `Dimensions` holder are ambiguous and stop compiling; a `val`
 *   holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when `p + q` does
 *   not resolve;
 * - a member operator added to `Point` or `Dimensions` in a later release would shadow these extensions.
 *
 * ### Allocation-free alternative
 * Each operator allocates its result. The in-place members `+=`, `-=`, `*=`, `/=`, `setTo`, `modBy`, `amplifyBy`
 * and `divideBy` change a value without allocating.
 *
 * ### Tier and worked example
 * These functions are a Supported Extension ([com.spartanlabs.generaltools.SupportedExtension]): they carry the
 * same compatibility guarantee as the Stable Core. They are also the worked example for a consumer adding the
 * same operators to its own `TwoDoubles` subclass:
 *
 * ```kotlin
 * @OptIn(ExperimentalForInheritanceGeneralToolsApi::class)
 * class Velocity(dx: Double, dy: Double) : TwoDoubles(dx, dy)
 *
 * operator fun Velocity.plus(other: Velocity): Velocity =
 *     Velocity(first + other.first, second + other.second)
 * ```
 */
package com.spartanlabs.geometry.arithmetic

import com.spartanlabs.generaltools.SupportedExtension
import com.spartanlabs.geometry.DIVISION_BY_ZERO_MESSAGE
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.TwoDoubles

/**
 * Adds two points component-wise: `Point(x + other.x, y + other.y)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param other the point to add.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Point.plus(other: Point): Point = Point(x + other.x, y + other.y)

/**
 * Offsets this point by [offset]: `Point(x + offset.width, y + offset.height)`, the corner formula
 * of `Rectangle.max`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param offset the size to add.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Point.plus(offset: Dimensions): Point = Point(x + offset.width, y + offset.height)

/**
 * Subtracts [other] from this point component-wise: `Point(x - other.x, y - other.y)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param other the point to subtract.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)

/**
 * Offsets this point back by [offset], the inverse of `Point + Dimensions`:
 * `Point(x - offset.width, y - offset.height)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param offset the size to subtract.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Point.minus(offset: Dimensions): Point = Point(x - offset.width, y - offset.height)

/**
 * Scales both coordinates by [factor]: `Point(x * factor, y * factor)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param factor the scale factor.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Point.times(factor: Double): Point = Point(x * factor, y * factor)

/**
 * Divides both coordinates by [divisor]: `Point(x / divisor, y / divisor)`.
 *
 * A zero divisor (`0.0` or `-0.0`) throws, as `/=` does, and logs nothing; each component is divided
 * directly, so `p / k` equals what `p /= k` leaves.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param divisor the divisor.
 * @return a new instance; the operands are unchanged.
 * @throws IllegalArgumentException with the message "Cannot divide by zero" if [divisor] is `0.0` or
 *   `-0.0`; the operand is left unchanged.
 */
@SupportedExtension
public operator fun Point.div(divisor: Double): Point {
    require(divisor != 0.0) { DIVISION_BY_ZERO_MESSAGE }   // same policy as /=: throw, no log
    return Point(x / divisor, y / divisor)                  // each component directly, as /= does
}

/**
 * Scales both coordinates of [point] by this factor: `Point(this * point.x, this * point.y)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param point the point to scale.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
// commutative in IEEE, so equal to the Point-times-Double form
public operator fun Double.times(point: Point): Point = Point(this * point.x, this * point.y)

/**
 * Adds two sizes component-wise: `Dimensions(width + other.width, height + other.height)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param other the size to add.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Dimensions.plus(other: Dimensions): Dimensions =
    Dimensions(width + other.width, height + other.height)

/**
 * Subtracts [other] from this size component-wise:
 * `Dimensions(width - other.width, height - other.height)`. The result is not clamped and may be
 * negative.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param other the size to subtract.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Dimensions.minus(other: Dimensions): Dimensions =
    Dimensions(width - other.width, height - other.height)

/**
 * Scales both components by [factor]: `Dimensions(width * factor, height * factor)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param factor the scale factor.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
public operator fun Dimensions.times(factor: Double): Dimensions = Dimensions(width * factor, height * factor)

/**
 * Divides both components by [divisor]: `Dimensions(width / divisor, height / divisor)`.
 *
 * A zero divisor (`0.0` or `-0.0`) throws, as `/=` does, and logs nothing; each component is divided
 * directly, so `p / k` equals what `p /= k` leaves.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param divisor the divisor.
 * @return a new instance; the operands are unchanged.
 * @throws IllegalArgumentException with the message "Cannot divide by zero" if [divisor] is `0.0` or
 *   `-0.0`; the operand is left unchanged.
 */
@SupportedExtension
public operator fun Dimensions.div(divisor: Double): Dimensions {
    require(divisor != 0.0) { DIVISION_BY_ZERO_MESSAGE }   // same policy as /=: throw, no log
    return Dimensions(width / divisor, height / divisor)    // each component directly, as /= does
}

/**
 * Scales both components of [dimensions] by this factor:
 * `Dimensions(this * dimensions.width, this * dimensions.height)`.
 *
 * Use the in-place members (`+=`, [TwoDoubles.modBy], [TwoDoubles.amplifyBy]) when an allocation per
 * operation is not acceptable.
 *
 * Costs of importing this operator (this package is opt in per file):
 * - in a file that imports it, `+=`, `-=`, `*=` and `/=` on a `var` [Point] or [Dimensions] holder are
 *   ambiguous and stop compiling; a `val` holder is unaffected;
 * - a wildcard import brings every operator of this package in, and an IDE may add the import when
 *   `p + q` does not resolve;
 * - a member operator added to [Point] or [Dimensions] in a later release would shadow this extension.
 *
 * @param dimensions the size to scale.
 * @return a new instance; the operands are unchanged.
 */
@SupportedExtension
// commutative in IEEE, so equal to the Dimensions-times-Double form
public operator fun Double.times(dimensions: Dimensions): Dimensions =
    Dimensions(this * dimensions.width, this * dimensions.height)
