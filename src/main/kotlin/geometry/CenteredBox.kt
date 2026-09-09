package com.spartanlabs.geometry

/**
 * An axis-aligned box centred at [center] with the given [halfExtents]
 * (half-width, half-height).
 *
 * Centre-origin, so — unlike [Square], whose origin is its top-left corner —
 * there is no corner-vs-centre ambiguity.
 *
 * Value type. [Point] and [Dimensions] are mutable, so do not mutate [center]
 * or [halfExtents] after handing them to a `CenteredBox`, and note that [copy]
 * shares the same instances. Treat `CenteredBox` as immutable (see plan §7 /
 * decision D-mut).
 *
 * Negative or `NaN` [halfExtents] are not rejected here (consistent with
 * [Point] / [Dimensions], which validate nothing); the intersection functions
 * reject a box whose [size] is negative or non-finite.
 *
 * @property center the geometric centre of the box.
 * @property halfExtents half the width and half the height.
 */
data class CenteredBox(
    override val center: Point,
    val halfExtents: Dimensions,
) : AxisAlignedBox {
    /** The low corner, `center - halfExtents`. */
    override val min: Point
        get() = Point(center.x - halfExtents.width, center.y - halfExtents.height)

    /** The high corner, `center + halfExtents`. */
    override val max: Point
        get() = Point(center.x + halfExtents.width, center.y + halfExtents.height)

    /** The full width and height, `halfExtents * 2`. */
    override val size: Dimensions
        get() = Dimensions(halfExtents.width * 2, halfExtents.height * 2)

    companion object {
        /**
         * Builds a [CenteredBox] spanning the two opposite corners [c1] and
         * [c2], given in any order.
         *
         * @param c1 one corner.
         * @param c2 the opposite corner.
         * @return a box whose [min] / [max] are the component-wise minimum /
         *   maximum of the two corners.
         */
        fun fromCorners(c1: Point, c2: Point): CenteredBox {
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
 * This [Square] as a [CenteredBox] covering the same footprint (same [min] and
 * [max]).
 *
 * @return a centre-origin box over the same area.
 */
fun Square.toCenteredBox(): CenteredBox =
    CenteredBox(
        Point(location.x + dimensions.width / 2.0, location.y + dimensions.height / 2.0),
        Dimensions(dimensions.width / 2.0, dimensions.height / 2.0),
    )

/**
 * This box as a top-left-origin [Square] covering the same footprint.
 *
 * @return a [Square] whose `location` is a copy of [AxisAlignedBox.min] and
 *   whose `dimensions` are a copy of [AxisAlignedBox.size].
 */
fun AxisAlignedBox.toSquare(): Square =
    Square(Point(min.x, min.y), Dimensions(size.width, size.height))
