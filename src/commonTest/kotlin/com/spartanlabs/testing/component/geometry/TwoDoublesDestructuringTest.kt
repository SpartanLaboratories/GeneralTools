package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Level 2 (Isolated Component Behaviour) coverage for `TwoDoubles.component1` and `component2`, which let a
 * Point or a Dimensions destructure into its two components.
 */
class TwoDoublesDestructuringTest {
    @Test
    fun `a Point destructures into x and y`() {
        val (x, y) = Point(1.5, 2.5)
        assertEquals(1.5, x)
        assertEquals(2.5, y)
    }

    @Test
    fun `a Dimensions destructures into width and height`() {
        val (width, height) = Dimensions(3.0, 4.0)
        assertEquals(3.0, width)
        assertEquals(4.0, height)
    }

    @Test
    fun `component1 and component2 return first and second`() {
        val point = Point(5.0, 6.0)
        assertEquals(point.first, point.component1())
        assertEquals(point.second, point.component2())
        val dimensions = Dimensions(7.0, 8.0)
        assertEquals(dimensions.first, dimensions.component1())
        assertEquals(dimensions.second, dimensions.component2())
    }

    @Test
    fun `destructuring works in a lambda over a list of Points`() {
        val sums = listOf(Point(1.0, 2.0), Point(3.0, 4.0)).map { (x, y) -> x + y }
        assertEquals(listOf(3.0, 7.0), sums)
    }
}
