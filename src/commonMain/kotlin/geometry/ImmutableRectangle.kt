package com.spartanlabs.geometry

/**
 * An immutable axis-aligned rectangle: a top-left corner ([x], [y]) and a size ([width], [height]).
 * Use it to hand a box to callers who must not change it: copy once with [Rectangle.toImmutable] when the
 * box is built, then return the copy on every read - no per-read copy is needed.
 *
 * - **Immutable.** All four values are fixed at construction; [min], [max], [center] and [size] return a
 *   new [Point]/[Dimensions] on every read, so changing a returned value cannot reach the rectangle.
 *   Safe to share between threads.
 * - **Value equality.** [equals] and [hashCode] follow the same contract as [Point] and [Dimensions]:
 *   components are IEEE-equal except that `NaN` equals `NaN`, and `-0.0` equals and hashes like `0.0`. An
 *   `ImmutableRectangle` is never equal to a [Rectangle], a [CenteredBox] or any other type, even over the
 *   same numbers; compare footprints through [toRectangle] or the accessors. It is a stable hash key.
 * - **Unvalidated.** Negative, `NaN` and infinite components are accepted, as in [Rectangle] and
 *   [CenteredBox]; the intersection functions return a failed `Result` for an unusable size.
 * - **[toString]** prints `ImmutableRectangle(x=..., y=..., width=..., height=...)` with each number in the
 *   platform's own format (on JavaScript, `1.0` prints as `1`).
 *
 * @property x the smallest `x` (the left edge).
 * @property y the smallest `y` (the top edge in this library's top-left convention).
 * @property width the horizontal extent.
 * @property height the vertical extent.
 * @since 3.0.0
 */
public class ImmutableRectangle(
    public val x: Double,
    public val y: Double,
    override val width: Double,
    override val height: Double,
) : AxisAlignedBox {
    override val minX: Double get() = x
    override val minY: Double get() = y
    override val maxX: Double get() = x + width
    override val maxY: Double get() = y + height

    /** The low corner `(x, y)`, as a new point on every read. */
    override val min: Point get() = Point(minX, minY)
    /** The high corner `(x + width, y + height)`, as a new point on every read. */
    override val max: Point get() = Point(maxX, maxY)
    /** The centre `(x + width / 2, y + height / 2)`, as a new point on every read. */
    override val center: Point get() = Point(x + width / 2.0, y + height / 2.0)
    /** The size `(width, height)`, as a new value on every read. */
    override val size: Dimensions get() = Dimensions(width, height)

    /**
     * A copy with the named components replaced and the rest kept.
     * @return a new `ImmutableRectangle`; this instance is unchanged.
     */
    public fun copy(
        x: Double = this.x,
        y: Double = this.y,
        width: Double = this.width,
        height: Double = this.height,
    ): ImmutableRectangle = ImmutableRectangle(x, y, width, height)

    override fun equals(other: Any?): Boolean =
        this === other || (other is ImmutableRectangle &&
            componentEquals(x, other.x) && componentEquals(y, other.y) &&
            componentEquals(width, other.width) && componentEquals(height, other.height))

    override fun hashCode(): Int =
        ((componentHash(x) * 31 + componentHash(y)) * 31 + componentHash(width)) * 31 + componentHash(height)

    override fun toString(): String = "ImmutableRectangle(x=$x, y=$y, width=$width, height=$height)"
}
