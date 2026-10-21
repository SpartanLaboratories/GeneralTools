package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Segment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the TwoDoubles equality and hash contract: same
 * concrete class, components equal per IEEE except that NaN equals NaN, `0.0` equal to `-0.0`, and equal
 * instances hashing alike, so Points work as hash keys. The pre-existing cases in TwoDoublesHashCodeTest
 * stay as they are.
 */
class TwoDoublesEqualityTest {
    private val nonCanonicalNaN = Double.fromBits(0x7ff8000000000001)

    @Test
    fun `a Point with NaN equals an identical Point with NaN`() {
        assertEquals(Point(Double.NaN, 1.0), Point(Double.NaN, 1.0))
    }

    @Test
    fun `a Point with NaN equals itself`() {
        val point = Point(Double.NaN, Double.NaN)
        assertTrue(point.equals(point))
    }

    @Test
    fun `NaN is compared per component`() {
        assertNotEquals(Point(Double.NaN, 1.0), Point(1.0, Double.NaN))
        assertNotEquals(Point(Double.NaN, 1.0), Point(Double.NaN, 2.0))
    }

    @Test
    fun `Points with zero and negative zero are equal`() {
        assertEquals(Point(0.0, 0.0), Point(-0.0, -0.0))
        assertEquals(Point(0.0, 1.0), Point(-0.0, 1.0))
    }

    @Test
    fun `Points with zero and negative zero have equal hash codes`() {
        assertEquals(Point(0.0, 0.0).hashCode(), Point(-0.0, -0.0).hashCode())
        assertEquals(Point(0.0, 1.0).hashCode(), Point(-0.0, 1.0).hashCode())
    }

    @Test
    fun `Points with different NaN bit patterns are equal and hash alike`() {
        val a = Point(nonCanonicalNaN, 1.0)
        val b = Point(Double.NaN, 1.0)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `a Dimensions never equals a Point with the same values`() {
        val dimensions = Dimensions(1.0, 2.0)
        val point = Point(1.0, 2.0)
        assertFalse(dimensions.equals(point))
        assertFalse(point.equals(dimensions))
    }

    @Test
    fun `equal values of the same class hash alike over a table of cases`() {
        val cases = listOf(
            0.0 to -0.0,
            Double.NaN to Double.NaN,
            1.5 to 1.5,
            Double.POSITIVE_INFINITY to Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY to Double.NEGATIVE_INFINITY,
        )
        for ((a, b) in cases) {
            assertEquals(Point(a, a), Point(b, b), "Point case ($a, $b)")
            assertEquals(Point(a, a).hashCode(), Point(b, b).hashCode(), "Point hash case ($a, $b)")
            assertEquals(Dimensions(a, a), Dimensions(b, b), "Dimensions case ($a, $b)")
            assertEquals(Dimensions(a, a).hashCode(), Dimensions(b, b).hashCode(), "Dimensions hash case ($a, $b)")
        }
    }

    @Test
    fun `unequal finite values are not equal`() {
        val point = Point(1.0, 2.0)
        assertNotEquals(point, Point(1.0, 3.0))
        assertNotEquals(point, Point(2.0, 2.0))
        assertNotEquals(point, Point(2.0, 1.0))
    }

    @Test
    fun `equals is symmetric and transitive across zeros and NaN`() {
        val a = Point(0.0, Double.NaN)
        val b = Point(-0.0, Double.NaN)
        val c = Point(0.0, Double.NaN)
        assertTrue(a == b)
        assertTrue(b == a)
        assertTrue(b == c)
        assertTrue(a == c)
    }

    @Test
    fun `equals is false for other types`() {
        val point = Point(1.0, 2.0)
        assertFalse(point.equals("1.0, 2.0"))
        assertFalse(point.equals(1.0))
        assertFalse(point.equals(null))
    }

    @Test
    fun `a Point with NaN works as a HashMap key`() {
        val map = hashMapOf(Point(Double.NaN, 1.0) to "nan")
        assertEquals("nan", map[Point(Double.NaN, 1.0)])
    }

    @Test
    fun `a Point with negative zero finds a map entry stored under zero`() {
        val map = hashMapOf(Point(0.0, 0.0) to "origin")
        assertEquals("origin", map[Point(-0.0, -0.0)])
    }

    @Test
    fun `a HashSet collapses Points that differ only in the sign of zero`() {
        val set = hashSetOf(Point(0.0, 1.0), Point(-0.0, 1.0))
        assertEquals(1, set.size)
    }

    @Test
    fun `a Segment with a NaN endpoint equals an identical Segment`() {
        assertEquals(
            Segment(Point(Double.NaN, 0.0), Point(1.0, 1.0)),
            Segment(Point(Double.NaN, 0.0), Point(1.0, 1.0)),
        )
    }
}
