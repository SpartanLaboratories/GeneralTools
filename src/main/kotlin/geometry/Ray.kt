package com.spartanlabs.geometry

/**
 * A half-line from [origin] extending along [direction].
 *
 * [direction] need not be unit length. Parametric distances returned by the
 * intersection tests (see [rayIntersectsBox]) are therefore in units of
 * `direction.length`: use [pointAt] to turn a `t` back into a world point, or
 * [unit] to obtain an equivalent ray whose `t` values are true world distances.
 *
 * Value type; see [Segment] KDoc regarding [Point] mutability.
 *
 * @property origin the start of the half-line (parameter `t = 0`).
 * @property direction the direction of travel; not required to be normalised.
 */
data class Ray(val origin: Point, val direction: Point) {
    /**
     * The point at parameter [t]: `origin + t * direction`.
     *
     * @param t the parameter, in units of `direction.length`; negative values
     *   lie behind the origin.
     * @return the point on this ray's supporting line at [t].
     */
    fun pointAt(t: Double): Point =
        Point(origin.x + t * direction.x, origin.y + t * direction.y)

    /**
     * A copy of this ray with [direction] scaled to unit length, so that `t`
     * values returned by the intersection tests become true world distances.
     *
     * @return `Result.failure(IllegalArgumentException)` if [direction] has
     *   `length < EPSILON` or contains `NaN`; otherwise `Result.success` of a
     *   ray with the same [origin] and supporting line.
     */
    fun unit(): Result<Ray> =
        direction.normalized().map { Ray(Point(origin.x, origin.y), it) }
}
