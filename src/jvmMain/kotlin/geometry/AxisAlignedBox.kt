package com.spartanlabs.geometry

/**
 * Read-only contract common to every axis-aligned rectangle in this package,
 * regardless of how it stores its origin — top-left for [Square], centre for
 * [CenteredBox].
 *
 * Every member is derived from the implementor's own fields; implementors add
 * no state of their own beyond what they already carry. The box-intersection
 * functions ([segmentIntersectsBox], [rayIntersectsBox]) accept this interface,
 * so a [Square] and a [CenteredBox] can be passed interchangeably without
 * conversion.
 */
interface AxisAlignedBox {
    /** The corner with the smallest `x` and `y`. */
    val min: Point

    /** The corner with the largest `x` and `y`. */
    val max: Point

    /** The geometric centre, `(min + max) / 2`. */
    val center: Point

    /** Width and height (`max - min`); non-negative for a valid box. */
    val size: Dimensions

    /**
     * Whether [p] lies inside this box or on its boundary
     * (`min.x <= p.x <= max.x` and `min.y <= p.y <= max.y`).
     *
     * @param p the point to test.
     * @return `true` if [p] is inside or on the edge; `false` otherwise
     *   (including when any coordinate involved is `NaN`).
     */
    infix fun contains(p: Point): Boolean =
        p.x >= min.x && p.x <= max.x && p.y >= min.y && p.y <= max.y
}
