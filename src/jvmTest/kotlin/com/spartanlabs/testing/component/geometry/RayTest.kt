package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Ray
import com.spartanlabs.geometry.cross
import com.spartanlabs.geometry.length
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [Ray]. Pure value type —
 * nothing to mock.
 */
class RayTest {
    private val delta = 1e-9

    @Test
    fun `pointAt the origin`() {
        assertEquals(Point(2.0, 3.0), Ray(Point(2.0, 3.0), Point(1.0, 0.0)).pointAt(0.0))
    }

    @Test
    fun `pointAt scales a non-unit direction`() {
        val r = Ray(Point(1.0, 1.0), Point(3.0, 4.0))
        assertEquals(Point(7.0, 9.0), r.pointAt(2.0))
    }

    @Test
    fun `unit normalises the direction and preserves the line`() {
        val r = Ray(Point(1.0, 1.0), Point(3.0, 4.0))
        val u = r.unit().getOrThrow()
        assertEquals(1.0, u.direction.length, delta)
        assertEquals(Point(1.0, 1.0), u.origin)
        // Same supporting line: original direction is parallel to the unit one.
        assertEquals(0.0, r.direction cross u.direction, delta)
    }

    @Test
    fun `unit of a zero-direction ray fails`() {
        val result = Ray(Point(0.0, 0.0), Point(0.0, 0.0)).unit()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `equal rays have equal hashCode`() {
        val a = Ray(Point(1.0, 2.0), Point(3.0, 4.0))
        val b = Ray(Point(1.0, 2.0), Point(3.0, 4.0))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
