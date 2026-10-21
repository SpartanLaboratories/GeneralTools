package com.spartanlabs.testing.component.geometry.arithmetic

import com.spartanlabs.geometry.Dimensions
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
 * Level 2 (Isolated Component Behaviour) coverage for the `Dimensions` operators of the import-gated arithmetic
 * package: each returns a new Dimensions and leaves its operands unchanged, results are not clamped, and `div`
 * follows the `/=` zero-divisor policy. Every holder in this file is a `val`.
 */
class DimensionsArithmeticTest {
    @Test
    fun `plus adds two Dimensions component-wise`() {
        assertEquals(Dimensions(4.0, 6.0), Dimensions(1.0, 2.0) + Dimensions(3.0, 4.0))
    }

    @Test
    fun `minus subtracts Dimensions component-wise`() {
        assertEquals(Dimensions(2.0, 3.0), Dimensions(5.0, 7.0) - Dimensions(3.0, 4.0))
    }

    @Test
    fun `minus may produce a negative size and does not clamp`() {
        assertEquals(Dimensions(-1.0, -2.0), Dimensions(1.0, 1.0) - Dimensions(2.0, 3.0))
    }

    @Test
    fun `times scales both components`() {
        assertEquals(Dimensions(3.0, 6.0), Dimensions(1.0, 2.0) * 3.0)
    }

    @Test
    fun `Double times Dimensions equals Dimensions times Double`() {
        val dimensions = Dimensions(1.5, 2.5)
        for (factor in listOf(2.0, -3.0, 0.0)) {
            assertEquals(dimensions * factor, factor * dimensions, "factor $factor")
        }
        assertEquals(Dimensions(Double.NaN, 1.0) * 2.0, 2.0 * Dimensions(Double.NaN, 1.0))
    }

    @Test
    fun `div divides each component directly`() {
        assertEquals(Dimensions(2.0, 1.0), Dimensions(8.0, 4.0) / 4.0)
    }

    @Test
    fun `div by 49 gives exactly one for 49`() {
        val result = Dimensions(49.0, 98.0) / 49.0
        assertEquals(1.0, result.width)
        assertEquals(2.0, result.height)
    }

    @Test
    fun `div equals what divAssign leaves`() {
        for (divisor in listOf(2.0, 3.0, 49.0, -4.0, 1000.0)) {
            val a = Dimensions(7.3, 11.9)
            val b = Dimensions(7.3, 11.9)
            // A val holder compiles with div imported: only the member divAssign applies.
            b /= divisor
            assertEquals(b, a / divisor, "divisor $divisor")
        }
    }

    @Test
    fun `div by zero throws IllegalArgumentException with the divideBy message`() {
        val exception = assertFailsWith<IllegalArgumentException> { Dimensions(4.0, 6.0) / 0.0 }
        assertEquals("Cannot divide by zero", exception.message)
    }

    @Test
    fun `div by negative zero throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> { Dimensions(4.0, 6.0) / -0.0 }
    }

    @Test
    fun `a failed div leaves the operand unchanged`() {
        val dimensions = Dimensions(4.0, 6.0)
        assertFailsWith<IllegalArgumentException> { dimensions / 0.0 }
        assertEquals(Dimensions(4.0, 6.0), dimensions)
    }

    @Test
    fun `no operator changes its operands`() {
        val a = Dimensions(1.0, 2.0)
        val b = Dimensions(3.0, 4.0)
        a + b
        a - b
        a * 2.0
        2.0 * a
        a / 2.0
        assertEquals(Dimensions(1.0, 2.0), a)
        assertEquals(Dimensions(3.0, 4.0), b)
    }

    @Test
    fun `every operator returns a new instance`() {
        val a = Dimensions(1.0, 2.0)
        assertNotSame(a, a + Dimensions())
        assertNotSame(a, a - Dimensions())
        assertNotSame(a, a * 1.0)
        assertNotSame(a, 1.0 * a)
        assertNotSame(a, a / 1.0)
    }

    @Test
    fun `a result can be mutated without affecting the operands`() {
        val a = Dimensions(1.0, 2.0)
        val b = Dimensions(3.0, 4.0)
        val sum = a + b
        sum.setTo(9.0, 9.0)
        assertEquals(Dimensions(1.0, 2.0), a)
        assertEquals(Dimensions(3.0, 4.0), b)
    }

    @Test
    fun `NaN propagates through plus and times`() {
        assertTrue((Dimensions(Double.NaN, 1.0) + Dimensions(1.0, 1.0)).containsNaN)
        assertTrue((Dimensions(1.0, 1.0) * Double.NaN).containsNaN)
    }

    @Test
    fun `operators compose with the usual precedence`() {
        val a = Dimensions(1.0, 1.0)
        val b = Dimensions(2.0, 3.0)
        val c = Dimensions(4.0, 8.0)
        // a + (b * 2.0) - (c / 4.0)
        assertEquals(Dimensions(4.0, 5.0), a + b * 2.0 - c / 4.0)
    }
}
