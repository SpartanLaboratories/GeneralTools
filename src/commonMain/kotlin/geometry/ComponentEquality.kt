package com.spartanlabs.geometry

/**
 * Equality of two components under the [TwoDoubles] contract: IEEE `==`, except that NaN equals NaN.
 * `0.0` and `-0.0` are equal. Pure and total. Shared with `ImmutableRectangle` so both types agree.
 */
internal fun componentEquals(a: Double, b: Double): Boolean = a == b || (a.isNaN() && b.isNaN())

/**
 * Hash of one component, consistent with [componentEquals]: `-0.0` hashes as `0.0` and every NaN hashes as
 * [Double.NaN]; any other value hashes as `value.hashCode()`. Pure and total.
 */
internal fun componentHash(value: Double): Int = when {
    value.isNaN() -> Double.NaN.hashCode()
    // IEEE comparison: true for both 0.0 and -0.0, which must hash alike because they are equal.
    value == 0.0 -> 0.0.hashCode()
    else -> value.hashCode()
}
