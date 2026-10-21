package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.ImmutableRectangle
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Level 2 (Isolated Component Behaviour) coverage for [Rectangle]. Pure value type - nothing to mock.
 */
class RectangleTest {
    @Test
    fun `default rectangle has zeroed location and dimensions`() {
        val rectangle = Rectangle()
        assertEquals(Point(0.0, 0.0), rectangle.location)
        assertEquals(Dimensions(0.0, 0.0), rectangle.dimensions)
    }

    @Test
    fun `rectangles with the same location and dimensions are equal`() {
        val first = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val second = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        assertEquals(first, second)
    }

    @Test
    fun `rectangles with different dimensions are not equal`() {
        val first = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val second = Rectangle(Point(1.0, 2.0), Dimensions(5.0, 6.0))
        assertNotEquals(first, second)
    }

    @Test
    fun `mutating a rectangle's location updates its properties`() {
        val rectangle = Rectangle()
        rectangle.location.setTo(Point(9.0, 9.0))
        assertEquals(9.0, rectangle.location.x)
        assertEquals(9.0, rectangle.location.y)
    }

    @Test
    fun `min is the live location and size is the live dimensions`() {
        val rect = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        assertSame(rect.location, rect.min)
        assertSame(rect.dimensions, rect.size)
    }

    @Test
    fun `changing the point returned by min moves the rectangle`() {
        val rect = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        rect.min.setTo(5.0, 6.0)
        assertEquals(Point(5.0, 6.0), rect.location)
    }

    @Test
    fun `max and center are fresh points that do not write back`() {
        val rect = Rectangle(Point(1.0, 2.0), Dimensions(4.0, 6.0))
        assertNotSame(rect.max, rect.max)
        assertEquals(Point(5.0, 8.0), rect.max)
        assertEquals(Point(3.0, 5.0), rect.center)

        rect.max.setTo(100.0, 100.0)
        assertEquals(Rectangle(Point(1.0, 2.0), Dimensions(4.0, 6.0)), rect)
    }

    @Test
    fun `copy shares the location and dimensions with the original`() {
        val rect = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val copy = rect.copy()
        assertSame(rect.location, copy.location)
        assertSame(rect.dimensions, copy.dimensions)

        rect.location.setTo(7.0, 8.0)
        assertEquals(Point(7.0, 8.0), copy.location)
    }

    @Test
    fun `toString prints Rectangle and the parts`() {
        val rect = Rectangle(Point(1.5, 2.5), Dimensions(3.5, 4.5))
        assertEquals("Rectangle(location=1.5, 2.5, dimensions=3.5, 4.5)", rect.toString())
    }

    @Test
    fun `a rectangle destructures into its location and dimensions`() {
        val rect = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val (l, d) = rect
        assertSame(rect.location, l)
        assertSame(rect.dimensions, d)
    }

    @Test
    fun `toImmutable copies the current location and dimensions values`() {
        val rect = Rectangle(Point(1.5, 2.5), Dimensions(3.5, 4.5))
        assertEquals(ImmutableRectangle(1.5, 2.5, 3.5, 4.5), rect.toImmutable())
    }

    @Test
    fun `a change to the rectangle after toImmutable does not reach the copy`() {
        val rect = Rectangle(Point(1.5, 2.5), Dimensions(3.5, 4.5))
        val copy = rect.toImmutable()
        rect.location.setTo(9.0, 9.0)
        rect.dimensions.setTo(7.0, 7.0)
        assertEquals(ImmutableRectangle(1.5, 2.5, 3.5, 4.5), copy)
        assertNotEquals<Any>(rect, copy)
    }
}
