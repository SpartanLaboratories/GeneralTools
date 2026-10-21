package com.spartanlabs.testing.e2e.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.ImmutableRectangle
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Ray
import com.spartanlabs.geometry.Rectangle
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.rayIntersectsBox
import com.spartanlabs.geometry.segmentIntersectsBox
import kotlin.test.Test
import kotlin.test.assertEquals

/** A library type that copies its box once at construction and hands the copy out on every read. */
private class Region(source: Rectangle) {
    val bounds: ImmutableRectangle = source.toImmutable()
}

/**
 * Level 4b (End-to-End) journey of the issue #8 scenario through the real types: a library builds a box from a
 * mutable [Rectangle], hands its callers an [ImmutableRectangle] copied once at construction, and queries it
 * with the intersection functions; contrasted with handing out the live [Rectangle] as an [AxisAlignedBox].
 */
class BoxHandOutTest {
    @Test
    fun `a library hands out an immutable copy and its callers cannot change the box`() {
        val source = Rectangle(Point(1.0, 2.0), Dimensions(4.0, 6.0))
        val region = Region(source)
        val bounds = region.bounds

        source.location.setTo(50.0, 50.0)
        source.dimensions.setTo(70.0, 70.0)
        bounds.min.setTo(-1.0, -1.0)
        bounds.max.setTo(-1.0, -1.0)
        bounds.center.setTo(-1.0, -1.0)
        bounds.size.setTo(-1.0, -1.0)

        assertEquals(ImmutableRectangle(1.0, 2.0, 4.0, 6.0), region.bounds)
        assertEquals(Point(1.0, 2.0), region.bounds.min)
        assertEquals(Point(5.0, 8.0), region.bounds.max)
        assertEquals(Point(3.0, 5.0), region.bounds.center)
        assertEquals(Dimensions(4.0, 6.0), region.bounds.size)
    }

    @Test
    fun `a live Rectangle handed out as AxisAlignedBox still exposes its parts`() {
        val source = Rectangle(Point(1.0, 2.0), Dimensions(4.0, 6.0))
        val view: AxisAlignedBox = source
        view.min.setTo(10.0, 20.0)
        assertEquals(Point(10.0, 20.0), source.location)
    }

    @Test
    fun `queries against the handed-out copy match queries against the original footprint`() {
        val source = Rectangle(Point(-1.0, -1.0), Dimensions(2.0, 2.0))
        val bounds = Region(source).bounds

        val segments = listOf(
            Segment(Point(-0.5, 0.0), Point(0.5, 0.0)),
            Segment(Point(-3.0, 0.5), Point(3.0, 0.5)),
            Segment(Point(2.0, 2.0), Point(3.0, 3.0)),
        )
        segments.forEach { s ->
            assertEquals(segmentIntersectsBox(s, source).getOrThrow(), segmentIntersectsBox(s, bounds).getOrThrow(), "$s")
        }

        val rays = listOf(
            Ray(Point(-3.0, 0.0), Point(1.0, 0.0)),
            Ray(Point(0.0, 0.0), Point(0.0, 1.0)),
            Ray(Point(3.0, 0.0), Point(1.0, 0.0)),
        )
        rays.forEach { r ->
            assertEquals(rayIntersectsBox(r, source).getOrThrow(), rayIntersectsBox(r, bounds).getOrThrow(), "$r")
        }

        listOf(Point(0.0, 0.0), Point(1.0, 1.0), Point(1.5, 0.0)).forEach { p ->
            assertEquals(p in source, p in bounds, "$p")
        }
    }

    @Test
    fun `an ImmutableRectangle stays a usable hash key while its source rectangle changes`() {
        val source = Rectangle(Point(1.0, 2.0), Dimensions(4.0, 6.0))
        val region = Region(source)
        val owners = HashMap<ImmutableRectangle, String>()
        owners[region.bounds] = "zone A"

        source.location.setTo(50.0, 50.0)
        source.dimensions.setTo(70.0, 70.0)

        assertEquals("zone A", owners[ImmutableRectangle(1.0, 2.0, 4.0, 6.0)])
    }
}
