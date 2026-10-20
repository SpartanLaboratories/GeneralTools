package com.spartanlabs.geometry

import org.slf4j.LoggerFactory

/** Logger used for diagnostic output from this file's functions. */
private val log = LoggerFactory.getLogger("com.spartanlabs.geometry.Segment")

/**
 * A directed line segment from [a] to [b].
 *
 * Value type. [Point] is mutable, so do not mutate [a] or [b] after handing
 * them to a `Segment`, and note that [copy] shares the same [Point] instances.
 * Treat `Segment` as immutable (see plan §7 / decision D-mut).
 *
 * @property a the start point (parameter `t = 0`).
 * @property b the end point (parameter `t = 1`).
 */
data class Segment(val a: Point, val b: Point) {
    /** The direction vector `b - a` (not normalised). */
    val delta: Point get() = Point(b.x - a.x, b.y - a.y)

    /** The Euclidean length `|b - a|`; `NaN` if an endpoint contains `NaN`. */
    val length: Double get() = delta.length

    /** The squared length `delta.lengthSquared`; cheaper than [length]. */
    val lengthSquared: Double get() = delta.lengthSquared

    /**
     * The point at parameter [t] along the segment: `a + t * (b - a)`.
     *
     * @param t the parameter; `0.0` returns [a], `1.0` returns [b]. Values
     *   outside `[0, 1]` extrapolate along the segment's supporting line.
     * @return the interpolated point.
     */
    fun pointAt(t: Double): Point =
        Point(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))

    /**
     * This segment as a [Ray] starting at [a] with direction [delta].
     *
     * @return `Result.failure(IllegalArgumentException)` if the segment is
     *   degenerate ([length] `< EPSILON`) or contains `NaN`; otherwise
     *   `Result.success(Ray(a, delta))`.
     */
    fun asRay(): Result<Ray> {
        if (a.containsNaN || b.containsNaN || length < EPSILON) {
            log.warn("Cannot build a Ray from a degenerate or non-finite Segment: {}", this)
            return Result.failure(
                IllegalArgumentException("Segment must be finite and longer than EPSILON")
            )
        }
        return Result.success(Ray(Point(a.x, a.y), delta))
    }
}
