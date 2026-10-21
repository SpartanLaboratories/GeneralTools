package com.spartanlabs.geometry

/**
 * An axis-aligned rectangle defined by a top-left [location] and its [dimensions]; the mutable box form.
 *
 * [location] is the **top-left corner** - the [min] corner in [AxisAlignedBox] terms. For centre-origin work
 * use [CenteredBox], or convert with [toCenteredBox].
 *
 * **Mutability.** [location] and [dimensions] are `val` references: a holder cannot swap them. The [Point]
 * and [Dimensions] they hold are mutable and stay so; change them in place
 * (`rectangle.location.setTo(...)`), and every holder of this `Rectangle`, or of the same part instance,
 * sees the change. [min] returns [location] itself and [size] returns [dimensions] itself - the live
 * parts - while [max] and [center] return new points. A `Rectangle` handed out as an [AxisAlignedBox]
 * therefore still exposes its live parts through `min` and `size`.
 *
 * **Copying.** [copy] is a shallow copy: the copy shares this rectangle's [location] and [dimensions]
 * instances. `Rectangle(Point(location), Dimensions(dimensions))`, `toImmutable().toRectangle()`, or
 * `toRectangle()` on any [AxisAlignedBox], builds an independent mutable copy.
 *
 * **Equality.** [equals] and [hashCode] follow the current values of the parts. A `Rectangle` used as a key
 * in a hash-based collection must not be changed - through [location], [dimensions], [min] or [size] -
 * while it is in the collection; use [toImmutable] for a key that cannot change.
 *
 * Nothing is validated: a negative or `NaN` size is accepted here and reported by the intersection
 * functions, which return a failed `Result` for it.
 *
 * @property location the top-left (minimum-x, minimum-y) corner; a live, mutable [Point].
 * @property dimensions the width and height; a live, mutable [Dimensions].
 */
public data class Rectangle(
    public val location: Point = Point(x = 0.0, y = 0.0),
    public val dimensions: Dimensions = Dimensions(width = 0.0, height = 0.0),
) : AxisAlignedBox {
    /** The smallest `x`, `location.x`. */
    override val minX: Double get() = location.x

    /** The smallest `y`, `location.y`. */
    override val minY: Double get() = location.y

    /** The largest `x`, `location.x + dimensions.width`. */
    override val maxX: Double get() = location.x + dimensions.width

    /** The largest `y`, `location.y + dimensions.height`. */
    override val maxY: Double get() = location.y + dimensions.height

    /** The horizontal extent, `dimensions.width`. */
    override val width: Double get() = dimensions.width

    /** The vertical extent, `dimensions.height`. */
    override val height: Double get() = dimensions.height

    /** The top-left corner, i.e. [location] itself (not a copy). */
    override val min: Point get() = location

    /** The bottom-right corner, `location + dimensions`, as a new point on every read. */
    override val max: Point get() = Point(maxX, maxY)

    /** The geometric centre, `location + dimensions / 2`, as a new point on every read. */
    override val center: Point
        get() = Point(location.x + dimensions.width / 2.0, location.y + dimensions.height / 2.0)

    /** The width and height, i.e. [dimensions] itself (not a copy). */
    override val size: Dimensions get() = dimensions

    /**
     * An immutable copy of this rectangle's current values, for handing out or for use as a hash key.
     *
     * The copy takes the four numbers now; it shares no part with this rectangle, so later changes to
     * [location] or [dimensions] do not reach it, and changes to it are impossible.
     *
     * @return a new [ImmutableRectangle] with `x = location.x`, `y = location.y`, `width = dimensions.width`,
     *   `height = dimensions.height`.
     * @since 3.0.0
     */
    public fun toImmutable(): ImmutableRectangle = ImmutableRectangle(minX, minY, width, height)
}
