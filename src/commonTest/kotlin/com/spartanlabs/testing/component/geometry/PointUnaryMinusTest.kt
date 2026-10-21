package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for `Point.unaryMinus`: `-point` is a new Point with both
 * coordinates negated, and the receiver is left unchanged.
 */
class PointUnaryMinusTest {
    @Test
    fun `unaryMinus negates both coordinates`() {
        assertEquals(Point(-1.5, 2.5), -Point(1.5, -2.5))
    }

    @Test
    fun `unaryMinus returns a new Point and leaves the receiver unchanged`() {
        val point = Point(1.0, 2.0)
        val before = Point(point)
        val negated = -point
        assertNotSame(point, negated)
        assertEquals(before, point)
    }

    @Test
    fun `mutating the negated Point does not change the original`() {
        val point = Point(1.0, 2.0)
        val negated = -point
        negated.setTo(9.0, 9.0)
        assertEquals(Point(1.0, 2.0), point)
    }

    @Test
    fun `unaryMinus of the origin equals the origin`() {
        assertEquals(Point(0.0, 0.0), -Point(0.0, 0.0))
    }

    @Test
    fun `unaryMinus keeps NaN and negates the other coordinate`() {
        val negated = -Point(Double.NaN, 2.0)
        assertTrue(negated.x.isNaN())
        assertEquals(-2.0, negated.y)
    }

    @Test
    fun `negating twice gives back an equal Point`() {
        val point = Point(3.0, -4.0)
        assertEquals(point, -(-point))
    }
}
