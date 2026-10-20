package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Level 2 (Isolated Component Behaviour) coverage for `TwoDoubles.divAssign` (`/=`): it throws for a zero
 * divisor, as in 2.x, and otherwise leaves exactly what `divideBy` leaves. PointTest keeps its own
 * divAssign tests unchanged.
 */
class TwoDoublesDivAssignTest {
    @Test
    fun `divAssign by zero throws IllegalArgumentException with the divideBy message`() {
        val point = Point(4.0, 6.0)
        val exception = assertFailsWith<IllegalArgumentException> { point /= 0.0 }
        assertEquals("Cannot divide by zero", exception.message)
    }

    @Test
    fun `divAssign by negative zero throws IllegalArgumentException`() {
        val point = Point(4.0, 6.0)
        assertFailsWith<IllegalArgumentException> { point /= -0.0 }
    }

    @Test
    fun `divAssign by zero leaves the value unchanged`() {
        val point = Point(4.0, 6.0)
        assertFailsWith<IllegalArgumentException> { point /= 0.0 }
        assertEquals(Point(4.0, 6.0), point)
    }

    @Test
    fun `divAssign divides each component directly`() {
        val point = Point(49.0, 98.0)
        point /= 49.0
        assertEquals(1.0, point.x)
        assertEquals(2.0, point.y)
    }

    @Test
    fun `divAssign leaves the same value as divideBy`() {
        for (divisor in listOf(2.0, 3.0, 49.0, -4.0, 1000.0)) {
            val a = Point(7.3, -11.9)
            val b = Point(7.3, -11.9)
            a /= divisor
            b divideBy divisor
            assertEquals(b, a, "divisor $divisor")
        }
    }
}
