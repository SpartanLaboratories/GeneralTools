package com.spartanlabs.geometry

/**
 * The shape contract common to every axis-aligned rectangle in this package, however it stores its origin:
 * top-left for [Rectangle], centre for [CenteredBox], origin-plus-size for [ImmutableRectangle].
 *
 * **A shape contract, not a read-only guarantee.** The interface has no setters, but the values it returns
 * need not be copies: a [Rectangle] handed out as an `AxisAlignedBox` still exposes its live
 * [Rectangle.location] and [Rectangle.dimensions] through [min] and [size], and a [CenteredBox] exposes its
 * stored [center]. To give callers a box they cannot change, copy it once with [Rectangle.toImmutable] and
 * hand out the [ImmutableRectangle].
 *
 * **Primitive accessors.** [minX], [minY], [maxX], [maxY], [width] and [height] read one coordinate without
 * building a [Point] or [Dimensions]. Their default bodies derive them from [min], [max] and [size] (and
 * allocate if those are built per read), so an existing implementor keeps compiling. An implementor that
 * stores scalars should override all six without allocating. Every accessor must return exactly - bit for
 * bit - the matching component: `minX == min.x`, `minY == min.y`, `maxX == max.x`, `maxY == max.y`,
 * `width == size.width`, `height == size.height`. [contains], [segmentIntersectsBox] and
 * [rayIntersectsBox] read only the accessors. An implementor must not define [min], [max] or [size] in terms
 * of the accessors while also leaving those accessors at their defaults; that recurses without end.
 *
 * Adding the six accessors in 3.0.0 breaks an implementor only if it already declares a member with one of
 * these names.
 */
public interface AxisAlignedBox {
    /** The corner with the smallest `x` and `y`. */
    public val min: Point

    /** The corner with the largest `x` and `y`. */
    public val max: Point

    /** The geometric centre, `(min + max) / 2`. */
    public val center: Point

    /** Width and height (`max - min`); non-negative for a valid box. */
    public val size: Dimensions

    /**
     * The smallest `x`; defaults to `min.x`.
     *
     * @since 3.0.0
     */
    public val minX: Double get() = min.x

    /**
     * The smallest `y`; defaults to `min.y`.
     *
     * @since 3.0.0
     */
    public val minY: Double get() = min.y

    /**
     * The largest `x`; defaults to `max.x`.
     *
     * @since 3.0.0
     */
    public val maxX: Double get() = max.x

    /**
     * The largest `y`; defaults to `max.y`.
     *
     * @since 3.0.0
     */
    public val maxY: Double get() = max.y

    /**
     * The horizontal extent; defaults to `size.width`.
     *
     * @since 3.0.0
     */
    public val width: Double get() = size.width

    /**
     * The vertical extent; defaults to `size.height`.
     *
     * @since 3.0.0
     */
    public val height: Double get() = size.height

    /**
     * Whether [p] lies inside this box or on its boundary
     * (`minX <= p.x <= maxX` and `minY <= p.y <= maxY`). Usable as `p in box` and `box contains p`.
     *
     * @param p the point to test.
     * @return `true` if [p] is inside or on the edge; `false` otherwise, including when any coordinate
     *   involved is `NaN`.
     */
    public infix operator fun contains(p: Point): Boolean =
        p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY
}
