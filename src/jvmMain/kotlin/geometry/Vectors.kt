package com.spartanlabs.geometry

import org.slf4j.LoggerFactory
import kotlin.math.sqrt

/** Logger used for diagnostic output from this file's functions. */
private val log = LoggerFactory.getLogger("com.spartanlabs.geometry.Vectors")

/**
 * Absolute tolerance for parallel / collinear / on-boundary geometry tests in
 * this package.
 *
 * It is used as:
 * - the zero-length guard in [normalized], [Segment.asRay] and [Ray.unit];
 * - the denominator cut-off that classifies two segments as parallel /
 *   collinear in [segmentIntersectsSegment];
 * - the "is the box degenerate?" and on-face checks in the intersection
 *   functions.
 *
 * This is an **absolute** value. At very large coordinate magnitudes (beyond
 * roughly `1e5`) the accumulated floating-point error in the cross-product and
 * slab arithmetic can exceed it, and near-collinear inputs may be
 * misclassified. Translate geometry toward the origin before testing when
 * working at such scales; exact orientation predicates are a possible future
 * enhancement.
 */
const val EPSILON: Double = 1e-10

/**
 * Dot product `x * other.x + y * other.y`.
 *
 * Total on finite input; if either operand contains `NaN` the result is `NaN`.
 *
 * @param other the vector to dot with this one.
 * @return the scalar dot product.
 */
infix fun Point.dot(other: Point): Double = x * other.x + y * other.y

/**
 * 2D scalar cross product (perp-dot) `x * other.y - y * other.x`.
 *
 * The sign gives the orientation of [other] relative to this vector (positive
 * when [other] is counter-clockwise from this one). Total on finite input;
 * `NaN` propagates.
 *
 * @param other the vector to cross with this one.
 * @return the scalar (z-component) cross product.
 */
infix fun Point.cross(other: Point): Double = x * other.y - y * other.x

/**
 * Euclidean magnitude `sqrt(x * x + y * y)`.
 *
 * `NaN` if this point contains `NaN`.
 */
val Point.length: Double get() = sqrt(x * x + y * y)

/**
 * Squared Euclidean magnitude `x * x + y * y`.
 *
 * Cheaper than [length] (no `sqrt`); prefer it when only relative comparisons
 * are needed. `NaN` if this point contains `NaN`.
 */
val Point.lengthSquared: Double get() = x * x + y * y

/**
 * This vector scaled to unit length.
 *
 * @return `Result.failure(IllegalArgumentException)` if this point contains
 *   `NaN` or its [length] is `< EPSILON` (a zero-ish vector has no defined
 *   direction); otherwise `Result.success` of a new [Point] whose [length] is
 *   `1.0`.
 */
fun Point.normalized(): Result<Point> {
    // A vector shorter than EPSILON has no well-defined direction: dividing by
    // its length would blow up to +/-Infinity or NaN. Guard the same degeneracy
    // TwoDoubles.divideBy guards, but return a Result instead of throwing
    // (consistent with Point.distanceFrom).
    val len = length
    if (containsNaN || len < EPSILON) {
        log.warn("Cannot normalize a zero-length or non-finite vector: {}", this)
        return Result.failure(
            IllegalArgumentException("Vector must be finite and longer than EPSILON to be normalized")
        )
    }
    return Result.success(Point(x / len, y / len))
}

/**
 * The vector projection of this vector onto [axis]:
 * `axis * ((this dot axis) / (axis dot axis))`.
 *
 * Total: if [axis] has [length] `< EPSILON` the projection is undefined and the
 * zero vector `Point(0.0, 0.0)` is returned (documented; not a `Result`).
 *
 * @param axis the vector to project onto.
 * @return the component of this vector parallel to [axis], or `Point(0.0, 0.0)`
 *   when [axis] is zero-ish.
 */
infix fun Point.projectedOnto(axis: Point): Point {
    if (axis.length < EPSILON) return Point(0.0, 0.0)
    val scale = (this dot axis) / (axis dot axis)
    return Point(axis.x * scale, axis.y * scale)
}
