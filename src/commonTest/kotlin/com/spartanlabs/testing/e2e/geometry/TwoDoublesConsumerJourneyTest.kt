package com.spartanlabs.testing.e2e.geometry

import com.spartanlabs.generaltools.ExperimentalForInheritanceGeneralToolsApi
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.TwoDoubles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A consumer's own coordinate pair, opting in to the Experimental subclassing seam. */
@OptIn(ExperimentalForInheritanceGeneralToolsApi::class)
private class Velocity(dx: Double, dy: Double) : TwoDoubles(dx, dy)

/** A consumer's value-returning operator, written in the style of the arithmetic package. */
private operator fun Velocity.plus(other: Velocity): Velocity =
    Velocity(first + other.first, second + other.second)

/**
 * Level 4b (End-to-End) journey of a consumer across the TwoDoubles surfaces: it subclasses TwoDoubles with
 * the opt-in, inherits the in-place verbs, equality, hashing and destructuring, and adds its own extension
 * operator in the style of the arithmetic package.
 */
class TwoDoublesConsumerJourneyTest {
    @Test
    fun `a consumer subclass gets in-place verbs, equality, hashing and destructuring from the base`() {
        val velocity = Velocity(1.0, 2.0)
        velocity += Velocity(1.0, 1.0)
        velocity *= 2.0
        assertTrue((velocity divideBy 4.0).isSuccess)
        val copy = Velocity(1.0, 1.5)
        assertEquals(copy, velocity)
        assertEquals(copy.hashCode(), velocity.hashCode())
        val (dx, dy) = velocity
        assertEquals(1.0, dx)
        assertEquals(1.5, dy)
    }

    @Test
    fun `a consumer extension operator written like the arithmetic package works on the subclass`() {
        val a = Velocity(1.0, 2.0)
        val b = Velocity(3.0, 4.0)
        val (dx, dy) = a + b
        assertEquals(4.0, dx)
        assertEquals(6.0, dy)
        assertEquals(Velocity(1.0, 2.0), a)
        assertEquals(Velocity(3.0, 4.0), b)
    }

    @Test
    fun `a consumer subclass is a distinct hash key from a Point with the same values`() {
        val map = hashMapOf<Any, String>(Velocity(1.0, 2.0) to "velocity", Point(1.0, 2.0) to "point")
        assertEquals(2, map.size)
        assertEquals("velocity", map[Velocity(1.0, 2.0)])
        assertEquals("point", map[Point(1.0, 2.0)])
        assertFalse(Velocity(1.0, 2.0).equals(Point(1.0, 2.0)))
    }
}
