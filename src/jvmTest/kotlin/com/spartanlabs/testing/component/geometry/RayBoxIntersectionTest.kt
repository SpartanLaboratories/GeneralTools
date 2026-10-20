package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Ray
import com.spartanlabs.geometry.rayIntersectsBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [rayIntersectsBox]. Pure —
 * nothing to mock. Cases mirror the plan §6 truth table, including the
 * `0 * Infinity` grazing case that must stay a deterministic miss.
 */
class RayBoxIntersectionTest {
    private val delta = 1e-9

    // Corners (-1,-1) .. (1,1).
    private val box = CenteredBox(Point(0.0, 0.0), Dimensions(1.0, 1.0))

    private fun entry(r: Ray) = rayIntersectsBox(r, box).getOrThrow()

    @Test
    fun `a ray hitting from outside returns the entry distance`() {
        val t = entry(Ray(Point(-3.0, 0.0), Point(1.0, 0.0)))
        assertNotNull(t)
        assertEquals(2.0, t, delta)
    }

    @Test
    fun `a ray whose origin is inside the box returns zero`() {
        val t = entry(Ray(Point(0.0, 0.0), Point(1.0, 0.0)))
        assertNotNull(t)
        assertEquals(0.0, t, delta)
    }

    @Test
    fun `a box entirely behind the origin is a miss`() {
        assertNull(entry(Ray(Point(5.0, 0.0), Point(1.0, 0.0))))
    }

    @Test
    fun `a ray parallel to a face and outside the slab misses`() {
        assertNull(entry(Ray(Point(-3.0, 5.0), Point(1.0, 0.0))))
    }

    @Test
    fun `a ray parallel to a face but within the slab hits`() {
        val t = entry(Ray(Point(-3.0, 0.5), Point(1.0, 0.0)))
        assertNotNull(t)
        assertEquals(2.0, t, delta)
    }

    @Test
    fun `origin on a face plane travelling along that face is a deterministic miss`() {
        // origin sits exactly on y = 1 (the top face); direction is along it.
        assertNull(entry(Ray(Point(0.0, 1.0), Point(1.0, 0.0))))
    }

    @Test
    fun `origin on a face travelling into the box returns zero`() {
        val t = entry(Ray(Point(0.0, 1.0), Point(0.0, -1.0)))
        assertNotNull(t)
        assertEquals(0.0, t, delta)
    }

    @Test
    fun `t is measured in units of direction length`() {
        val nonUnit = entry(Ray(Point(-3.0, 0.0), Point(2.0, 0.0)))
        assertNotNull(nonUnit)
        assertEquals(1.0, nonUnit, delta)

        val unit = rayIntersectsBox(
            Ray(Point(-3.0, 0.0), Point(2.0, 0.0)).unit().getOrThrow(),
            box,
        ).getOrThrow()
        assertNotNull(unit)
        assertEquals(2.0, unit, delta)
    }

    @Test
    fun `a zero-direction ray fails`() {
        val result = rayIntersectsBox(Ray(Point(0.0, 0.0), Point(0.0, 0.0)), box)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `a non-finite ray or box fails`() {
        assertTrue(rayIntersectsBox(Ray(Point(Double.NaN, 0.0), Point(1.0, 0.0)), box).isFailure)
        assertTrue(
            rayIntersectsBox(
                Ray(Point(0.0, 0.0), Point(1.0, 0.0)),
                CenteredBox(Point(0.0, 0.0), Dimensions(Double.POSITIVE_INFINITY, 1.0)),
            ).isFailure
        )
    }

    @Test
    fun `a diagonal ray through a corner is deterministic`() {
        val t = entry(Ray(Point(-3.0, -3.0), Point(1.0, 1.0)))
        assertNotNull(t)
        assertEquals(2.0, t, delta)
    }
}
