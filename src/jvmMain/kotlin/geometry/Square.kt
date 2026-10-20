package com.spartanlabs.geometry

/**
 * Represents an axis-aligned square (or rectangle) defined by a top-left
 * [location] and its [dimensions].
 *
 * This library's convention is that [location] is the **top-left corner** — the
 * [min] corner in [AxisAlignedBox] terms. For centre-origin work use
 * [CenteredBox], or convert with [Square.toCenteredBox].
 *
 * `Square` implements [AxisAlignedBox]: [min] / [max] / [center] / [size] are
 * computed from [location] and [dimensions], and [AxisAlignedBox.contains]
 * comes from the interface default. The fields and constructor are unchanged,
 * so this is purely additive.
 */
data class Square (
    /** The top-left (minimum-x, minimum-y) corner of the square. */
    var location:Point = Point(x = 0.0,y = 0.0),
    /** The width and height of the square */
    var dimensions: Dimensions = Dimensions(width = 0.0,height = 0.0)
) : AxisAlignedBox {
    /** The top-left corner, i.e. [location] itself. */
    override val min: Point get() = location

    /** The bottom-right corner, `location + dimensions`. */
    override val max: Point
        get() = Point(location.x + dimensions.width, location.y + dimensions.height)

    /** The geometric centre, `location + dimensions / 2`. */
    override val center: Point
        get() = Point(location.x + dimensions.width / 2.0, location.y + dimensions.height / 2.0)

    /** The width and height, i.e. [dimensions] itself. */
    override val size: Dimensions get() = dimensions
}
