package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Level 2 (Isolated Component Behaviour) coverage for [Square]. Pure value type - nothing to mock.
 */
class SquareTest {
    @Test
    fun `default square has zeroed location and dimensions`() {
        val square = Square()
        assertEquals(Point(0.0, 0.0), square.location)
        assertEquals(Dimensions(0.0, 0.0), square.dimensions)
    }

    @Test
    fun `squares with the same location and dimensions are equal`() {
        val first = Square(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val second = Square(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        assertEquals(first, second)
    }

    @Test
    fun `squares with different dimensions are not equal`() {
        val first = Square(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val second = Square(Point(1.0, 2.0), Dimensions(5.0, 6.0))
        assertNotEquals(first, second)
    }

    @Test
    fun `mutating a square's location updates its properties`() {
        val square = Square()
        square.location = Point(9.0, 9.0)
        assertEquals(9.0, square.location.x)
        assertEquals(9.0, square.location.y)
    }
}
