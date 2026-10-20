package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.cross
import com.spartanlabs.geometry.dot
import com.spartanlabs.geometry.length
import com.spartanlabs.geometry.lengthSquared
import com.spartanlabs.geometry.normalized
import com.spartanlabs.geometry.projectedOnto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the vector algebra
 * extensions in `Vectors.kt`. All pure — nothing to mock.
 */
class PointVectorOpsTest {
    private val delta = 1e-9

    @Test
    fun `dot of two vectors`() {
        assertEquals(11.0, Point(1.0, 2.0) dot Point(3.0, 4.0), delta)
    }

    @Test
    fun `dot of orthogonal vectors is zero`() {
        assertEquals(0.0, Point(1.0, 0.0) dot Point(0.0, 1.0), delta)
    }

    @Test
    fun `cross sign and magnitude`() {
        assertEquals(1.0, Point(1.0, 0.0) cross Point(0.0, 1.0), delta)
        assertEquals(-1.0, Point(0.0, 1.0) cross Point(1.0, 0.0), delta)
    }

    @Test
    fun `length and lengthSquared`() {
        assertEquals(5.0, Point(3.0, 4.0).length, delta)
        assertEquals(25.0, Point(3.0, 4.0).lengthSquared, delta)
    }

    @Test
    fun `normalized yields a unit vector`() {
        val n = Point(0.0, 5.0).normalized().getOrThrow()
        assertEquals(0.0, n.x, delta)
        assertEquals(1.0, n.y, delta)
        assertEquals(1.0, n.length, delta)
    }

    @Test
    fun `normalized of the zero vector fails`() {
        val result = Point(0.0, 0.0).normalized()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `normalized of a sub-EPSILON vector fails`() {
        assertTrue(Point(1e-12, 0.0).normalized().isFailure)
    }

    @Test
    fun `normalized of a NaN vector fails`() {
        assertTrue(Point(Double.NaN, 1.0).normalized().isFailure)
    }

    @Test
    fun `projectedOnto an axis`() {
        assertEquals(Point(2.0, 0.0), Point(2.0, 3.0) projectedOnto Point(1.0, 0.0))
    }

    @Test
    fun `projectedOnto a zero axis is the zero vector`() {
        assertEquals(Point(0.0, 0.0), Point(2.0, 3.0) projectedOnto Point(0.0, 0.0))
    }

    @Test
    fun `dot and cross propagate NaN`() {
        assertTrue((Point(Double.NaN, 1.0) dot Point(1.0, 1.0)).isNaN())
        assertTrue((Point(Double.NaN, 1.0) cross Point(1.0, 1.0)).isNaN())
    }
}
