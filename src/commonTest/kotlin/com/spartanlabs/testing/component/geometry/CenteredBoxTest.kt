package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square
import com.spartanlabs.geometry.toCenteredBox
import com.spartanlabs.geometry.toSquare
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [CenteredBox] and the
 * [Square] / [CenteredBox] converters. Pure value types — nothing to mock.
 */
class CenteredBoxTest {
    @Test
    fun `min max center and size are derived from centre and half-extents`() {
        val box = CenteredBox(Point(3.0, 3.0), Dimensions(2.0, 1.0))
        assertEquals(Point(1.0, 2.0), box.min)
        assertEquals(Point(5.0, 4.0), box.max)
        assertEquals(Point(3.0, 3.0), box.center)
        assertEquals(Dimensions(4.0, 2.0), box.size)
    }

    @Test
    fun `contains covers interior boundary and exterior`() {
        val box = CenteredBox(Point(0.0, 0.0), Dimensions(2.0, 2.0))
        assertTrue(box contains Point(0.0, 0.0))
        assertTrue(box contains Point(2.0, 0.0))
        assertFalse(box contains Point(3.0, 0.0))
    }

    @Test
    fun `fromCorners is order-independent`() {
        val a = CenteredBox.fromCorners(Point(1.0, 2.0), Point(5.0, 4.0))
        val b = CenteredBox.fromCorners(Point(5.0, 4.0), Point(1.0, 2.0))
        assertEquals(a, b)
        assertEquals(Point(1.0, 2.0), a.min)
        assertEquals(Point(5.0, 4.0), a.max)
    }

    @Test
    fun `toCenteredBox keeps the same footprint`() {
        val box = Square(Point(0.0, 0.0), Dimensions(4.0, 2.0)).toCenteredBox()
        assertEquals(Point(2.0, 1.0), box.center)
        assertEquals(Dimensions(2.0, 1.0), box.halfExtents)
    }

    @Test
    fun `toCenteredBox then toSquare round-trips the footprint`() {
        val original = Square(Point(1.0, 2.0), Dimensions(4.0, 2.0))
        val roundTripped = original.toCenteredBox().toSquare()
        assertEquals(original.min, roundTripped.min)
        assertEquals(original.max, roundTripped.max)
    }

    @Test
    fun `a zero half-extent box contains only its centre`() {
        val box = CenteredBox(Point(1.0, 1.0), Dimensions(0.0, 0.0))
        assertTrue(box contains Point(1.0, 1.0))
        assertFalse(box contains Point(1.0001, 1.0))
    }

    @Test
    fun `equal boxes have equal hashCode`() {
        val a = CenteredBox(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val b = CenteredBox(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
