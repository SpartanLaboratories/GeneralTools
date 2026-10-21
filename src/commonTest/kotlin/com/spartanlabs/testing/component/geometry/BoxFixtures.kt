package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.ImmutableRectangle
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Rectangle

/** A box that implements only min, max, center and size: exercises every default body of AxisAlignedBox. */
internal class DefaultsOnlyBox(private val x: Double, private val y: Double,
                               private val w: Double, private val h: Double) : AxisAlignedBox {
    override val min: Point get() = Point(x, y)
    override val max: Point get() = Point(x + w, y + h)
    override val center: Point get() = Point(x + w / 2.0, y + h / 2.0)
    override val size: Dimensions get() = Dimensions(w, h)
}

internal object BoxFixtures {
    /** One box per implementor, each describing the footprint (x, y, w, h) in its own storage form. */
    fun forFootprint(x: Double, y: Double, w: Double, h: Double): List<Pair<String, AxisAlignedBox>> = listOf(
        "Rectangle" to Rectangle(Point(x, y), Dimensions(w, h)),
        "CenteredBox" to CenteredBox(Point(x + w / 2.0, y + h / 2.0), Dimensions(w / 2.0, h / 2.0)),
        "defaults-only box" to DefaultsOnlyBox(x, y, w, h),
        "ImmutableRectangle" to ImmutableRectangle(x, y, w, h),
    )
}
