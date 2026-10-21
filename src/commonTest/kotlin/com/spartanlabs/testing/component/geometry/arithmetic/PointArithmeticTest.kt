package com.spartanlabs.testing.component.geometry.arithmetic

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.arithmetic.div
import com.spartanlabs.geometry.arithmetic.minus
import com.spartanlabs.geometry.arithmetic.plus
import com.spartanlabs.geometry.arithmetic.times
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the `Point` operators of the import-gated arithmetic
 * package: each returns a new Point and leaves its operands unchanged, and `div` follows the `/=` zero-divisor
 * policy. Every holder in this file is a `val`: with the operators imported, a compound assignment on a `var`
 * Point would be ambiguous.
 */
class PointArithmeticTest {
    @Test
    fun `plus adds two Points component-wise`() {
        assertEquals(Point(4.0, 6.0), Point(1.0, 2.0) + Point(3.0, 4.0))
    }

    @Test
    fun `plus offsets a Point by Dimensions`() {
        val location = Point(1.5, 2.5)
        val size = Dimensions(3.0, 4.0)
        assertEquals(Point(location.x + size.width, location.y + size.height), location + size)
    }

    @Test
    fun `minus subtracts a Point component-wise`() {
        assertEquals(Point(-2.0, -2.0), Point(1.0, 2.0) - Point(3.0, 4.0))
    }

    @Test
    fun `minus subtracts Dimensions from a Point`() {
        assertEquals(Point(2.0, 3.0), Point(5.0, 7.0) - Dimensions(3.0, 4.0))
    }

    @Test
    fun `times scales both coordinates`() {
        assertEquals(Point(3.0, -6.0), Point(1.0, -2.0) * 3.0)
    }

    @Test
    fun `Double times Point equals Point times Double`() {
        val point = Point(1.5, -2.5)
        for (factor in listOf(2.0, -3.0, 0.0)) {
            assertEquals(point * factor, factor * point, "factor $factor")
        }
        assertEquals(Point(Double.NaN, 1.0) * 2.0, 2.0 * Point(Double.NaN, 1.0))
    }

    @Test
    fun `div divides each coordinate directly`() {
        assertEquals(Point(2.0, 3.0), Point(4.0, 6.0) / 2.0)
    }

    @Test
    fun `div by 49 gives exactly one for 49`() {
        val result = Point(49.0, 98.0) / 49.0
        assertEquals(1.0, result.x)
        assertEquals(2.0, result.y)
    }

    @Test
    fun `div equals what divAssign leaves`() {
        for (divisor in listOf(2.0, 3.0, 49.0, -4.0, 1000.0)) {
            val a = Point(7.3, -11.9)
            val b = Point(7.3, -11.9)
            // A val holder compiles with div imported: only the member divAssign applies.
            b /= divisor
            assertEquals(b, a / divisor, "divisor $divisor")
        }
    }

    @Test
    fun `div by zero throws IllegalArgumentException with the divideBy message`() {
        val exception = assertFailsWith<IllegalArgumentException> { Point(4.0, 6.0) / 0.0 }
        assertEquals("Cannot divide by zero", exception.message)
    }

    @Test
    fun `div by negative zero throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> { Point(4.0, 6.0) / -0.0 }
    }

    @Test
    fun `a failed div leaves the operand unchanged`() {
        val point = Point(4.0, 6.0)
        assertFailsWith<IllegalArgumentException> { point / 0.0 }
        assertEquals(Point(4.0, 6.0), point)
    }

    @Test
    fun `no operator changes its operands`() {
        val p = Point(1.0, 2.0)
        val q = Point(3.0, 4.0)
        val d = Dimensions(5.0, 6.0)
        p + q
        p + d
        p - q
        p - d
        p * 2.0
        2.0 * p
        p / 2.0
        assertEquals(Point(1.0, 2.0), p)
        assertEquals(Point(3.0, 4.0), q)
        assertEquals(Dimensions(5.0, 6.0), d)
    }

    @Test
    fun `every operator returns a new instance`() {
        val p = Point(1.0, 2.0)
        assertNotSame(p, p + Point())
        assertNotSame(p, p + Dimensions())
        assertNotSame(p, p - Point())
        assertNotSame(p, p - Dimensions())
        assertNotSame(p, p * 1.0)
        assertNotSame(p, 1.0 * p)
        assertNotSame(p, p / 1.0)
    }

    @Test
    fun `a result can be mutated without affecting the operands`() {
        val p = Point(1.0, 2.0)
        val q = Point(3.0, 4.0)
        val sum = p + q
        sum.setTo(9.0, 9.0)
        assertEquals(Point(1.0, 2.0), p)
        assertEquals(Point(3.0, 4.0), q)
    }

    @Test
    fun `NaN propagates through plus and times`() {
        assertTrue((Point(Double.NaN, 1.0) + Point(1.0, 1.0)).containsNaN)
        assertTrue((Point(1.0, 1.0) * Double.NaN).containsNaN)
    }

    @Test
    fun `operators compose with the usual precedence`() {
        val a = Point(1.0, 1.0)
        val b = Point(2.0, 3.0)
        val c = Point(4.0, 8.0)
        // a + (b * 2.0) - (c / 4.0)
        assertEquals(Point(4.0, 5.0), a + b * 2.0 - c / 4.0)
    }
}
