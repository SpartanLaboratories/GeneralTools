package com.spartanlabs.geometry

/**
 * An axis-aligned box centred at [center] with the given [halfExtents]
 * (half-width, half-height).
 *
 * Centre-origin, so — unlike [Rectangle], whose origin is its top-left corner —
 * there is no corner-vs-centre ambiguity.
 *
 * Value type. [Point] and [Dimensions] are mutable, so do not mutate [center]
 * or [halfExtents] after handing them to a `CenteredBox`, and note that [copy]
 * shares the same instances. Treat `CenteredBox` as immutable.
 *
 * Negative or `NaN` [halfExtents] are not rejected here (consistent with
 * [Point] / [Dimensions], which validate nothing); the intersection functions
 * reject a box whose [size] is negative or non-finite.
 *
 * @property center the geometric centre of the box.
 * @property halfExtents half the width and half the height.
 */
public data class CenteredBox(
    override val center: Point,
    val halfExtents: Dimensions,
) : AxisAlignedBox {
    /** The smallest `x`, `center.x - halfExtents.width`. */
    override val minX: Double get() = center.x - halfExtents.width

    /** The smallest `y`, `center.y - halfExtents.height`. */
    override val minY: Double get() = center.y - halfExtents.height

    /** The largest `x`, `center.x + halfExtents.width`. */
    override val maxX: Double get() = center.x + halfExtents.width

    /** The largest `y`, `center.y + halfExtents.height`. */
    override val maxY: Double get() = center.y + halfExtents.height

    /** The horizontal extent, `halfExtents.width * 2`. */
    override val width: Double get() = halfExtents.width * 2

    /** The vertical extent, `halfExtents.height * 2`. */
    override val height: Double get() = halfExtents.height * 2

    /** The low corner, `center - halfExtents`. */
    override val min: Point get() = Point(minX, minY)

    /** The high corner, `center + halfExtents`. */
    override val max: Point get() = Point(maxX, maxY)

    /** The full width and height, `halfExtents * 2`. */
    override val size: Dimensions get() = Dimensions(width, height)

    /** Factories for [CenteredBox]. */
    public companion object {
        /**
         * Builds a [CenteredBox] spanning the two opposite corners [c1] and
         * [c2], given in any order.
         *
         * @param c1 one corner.
         * @param c2 the opposite corner.
         * @return a box whose [min] / [max] are the component-wise minimum /
         *   maximum of the two corners.
         */
        public fun fromCorners(c1: Point, c2: Point): CenteredBox {
            val minX = minOf(c1.x, c2.x)
            val minY = minOf(c1.y, c2.y)
            val maxX = maxOf(c1.x, c2.x)
            val maxY = maxOf(c1.y, c2.y)
            return CenteredBox(
                Point((minX + maxX) / 2.0, (minY + maxY) / 2.0),
                Dimensions((maxX - minX) / 2.0, (maxY - minY) / 2.0),
            )
        }
    }
}

/**
 * This [Rectangle] as a [CenteredBox] covering the same footprint (same [Rectangle.min] and [Rectangle.max]).
 *
 * @receiver the rectangle to convert; not changed.
 * @return a new centre-origin box over the same area. It shares no part with the receiver.
 */
public fun Rectangle.toCenteredBox(): CenteredBox =
    CenteredBox(
        Point(location.x + dimensions.width / 2.0, location.y + dimensions.height / 2.0),
        Dimensions(dimensions.width / 2.0, dimensions.height / 2.0),
    )

/**
 * This box as a top-left-origin [Rectangle] covering the same footprint. An [ImmutableRectangle] converts back
 * to a mutable [Rectangle] the same way.
 *
 * @receiver any [AxisAlignedBox]; not changed.
 * @return an independent, mutable [Rectangle] whose `location` is a new [Point] holding [AxisAlignedBox.min]
 *   and whose `dimensions` is a new [Dimensions] holding [AxisAlignedBox.size]; later changes to either
 *   side do not reach the other.
 */
public fun AxisAlignedBox.toRectangle(): Rectangle =
    Rectangle(Point(minX, minY), Dimensions(width, height))
