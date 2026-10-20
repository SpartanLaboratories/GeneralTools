package com.spartanlabs.testing.component.geometry

import com.spartanlabs.generaltools.ExperimentalForInheritanceGeneralToolsApi
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.TwoDoubles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the TwoDoubles subclassing seam.
 * The class opts in to ExperimentalForInheritanceGeneralToolsApi, as a consumer would, and declares
 * its own subclasses of TwoDoubles to check what a subclass inherits from the base.
 */
@OptIn(ExperimentalForInheritanceGeneralToolsApi::class)
class TwoDoublesSubclassTest {
    private class Velocity(dx: Double, dy: Double) : TwoDoubles(dx, dy)

    private class Heading : TwoDoubles {
        constructor(d: TwoDoubles) : super(d)
    }

    private class Acceleration(a: Double, b: Double) : TwoDoubles(a, b)

    @Test
    fun `a subclass with the opt-in inherits the in-place verbs`() {
        val velocity = Velocity(1.0, 2.0)
        val point = Point(1.0, 2.0)

        velocity.setTo(3.0, 4.0)
        point.setTo(3.0, 4.0)
        assertEquals(point.first, velocity.first)
        assertEquals(point.second, velocity.second)

        velocity.modBy(1.5, 2.5)
        point.modBy(1.5, 2.5)
        assertEquals(point.first, velocity.first)
        assertEquals(point.second, velocity.second)

        velocity amplifyBy 2.0
        point amplifyBy 2.0
        assertEquals(point.first, velocity.first)
        assertEquals(point.second, velocity.second)

        velocity += Velocity(1.0, 1.0)
        point += Point(1.0, 1.0)
        assertEquals(point.first, velocity.first)
        assertEquals(point.second, velocity.second)

        velocity -= Velocity(0.5, 0.25)
        point -= Point(0.5, 0.25)
        assertEquals(point.first, velocity.first)
        assertEquals(point.second, velocity.second)

        velocity *= 3.0
        point *= 3.0
        assertEquals(point.first, velocity.first)
        assertEquals(point.second, velocity.second)

        assertEquals(28.5, velocity.first)
        assertEquals(41.25, velocity.second)
    }

    @Test
    fun `a subclass built from the protected copy constructor copies the values`() {
        val heading = Heading(Velocity(1.5, 2.5))
        assertEquals(1.5, heading.first)
        assertEquals(2.5, heading.second)
    }

    @Test
    fun `a subclass inherits containsNaN`() {
        assertTrue(Velocity(Double.NaN, 1.0).containsNaN)
        assertTrue(Velocity(1.0, Double.NaN).containsNaN)
        assertFalse(Velocity(1.0, 2.0).containsNaN)
    }

    @Test
    fun `a subclass instance is not equal to a Point with the same values`() {
        val velocity = Velocity(1.0, 2.0)
        val point = Point(1.0, 2.0)
        assertFalse(velocity.equals(point))
        assertFalse(point.equals(velocity))
    }

    @Test
    fun `two instances of one subclass with the same values are equal and hash alike`() {
        val a = Velocity(1.0, 2.0)
        val b = Velocity(1.0, 2.0)
        assertTrue(a == b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `two different subclasses with the same values are not equal`() {
        val velocity = Velocity(1.0, 2.0)
        val acceleration = Acceleration(1.0, 2.0)
        assertFalse(velocity.equals(acceleration))
        assertFalse(acceleration.equals(velocity))
    }
}
