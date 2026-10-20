package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for TwoDoubles.
 * TwoDoubles is abstract, so its behavior is exercised here through its concrete
 * subclass Point; the subclassing seam is covered by TwoDoublesSubclassTest.
 * Point/Dimensions-specific behavior (x/y, width/height,
 * distanceFrom, etc.) is covered separately in PointTest.
 */
class TwoDoublesTest {
    @Test
    fun `containsNaN is true when either value is NaN`() {
        assertTrue(Point(Double.NaN, 0.0).containsNaN)
        assertTrue(Point(0.0, Double.NaN).containsNaN)
    }

    @Test
    fun `containsNaN is false for regular numbers`() {
        assertFalse(Point(1.0, 2.0).containsNaN)
    }

    @Test
    fun `infix setTo copies another instance's values`() {
        val point = Point()
        point setTo Point(4.0, 5.0)
        assertEquals(Point(4.0, 5.0), point)
    }

    @Test
    fun `infix modBy adjusts by another instance's values`() {
        val point = Point(1.0, 1.0)
        point modBy Point(2.0, 3.0)
        assertEquals(Point(3.0, 4.0), point)
    }

    @Test
    fun `amplifyBy multiplies both values`() {
        val point = Point(2.0, 3.0)
        point amplifyBy 3.0
        assertEquals(Point(6.0, 9.0), point)
    }

    @Test
    fun `equals is false when compared to null`() {
        assertFalse(Point(1.0, 1.0).equals(null))
    }

    @Test
    fun `equals is true for the same values and type`() {
        assertTrue(Point(1.0, 1.0) == Point(1.0, 1.0))
    }
}
