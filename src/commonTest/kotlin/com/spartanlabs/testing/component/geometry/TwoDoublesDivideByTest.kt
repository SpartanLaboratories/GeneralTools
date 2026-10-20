package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for `TwoDoubles.divideBy`: it divides each component in
 * place and returns `Result<Unit>`, reporting a zero divisor as a failure that leaves the value unchanged.
 * The WARN line a failure logs is covered on the JVM by TwoDoublesDivisionLoggingTest.
 */
class TwoDoublesDivideByTest {
    @Test
    fun `divideBy divides both components in place`() {
        val point = Point(4.0, 6.0)
        point divideBy 2.0
        assertEquals(Point(2.0, 3.0), point)
    }

    @Test
    fun `divideBy reports success with Unit`() {
        val result = Point(4.0, 6.0) divideBy 2.0
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrThrow())
    }

    @Test
    fun `divideBy gives exactly one for 49 divided by 49`() {
        val point = Point(49.0, 98.0)
        point divideBy 49.0
        assertEquals(1.0, point.x)
        assertEquals(2.0, point.y)
        // Why the arithmetic changed: multiplying by the reciprocal, as 2.x did, misses 1.0 by one ulp.
        val divisor = listOf(49.0).single()
        assertNotEquals(1.0, 49.0 * (1.0 / divisor))
    }

    @Test
    fun `divideBy does not overflow for a subnormal divisor`() {
        val point = Point(Double.MIN_VALUE, Double.MIN_VALUE)
        point divideBy Double.MIN_VALUE
        assertEquals(1.0, point.x)
        assertEquals(1.0, point.y)
    }

    @Test
    fun `divideBy by zero fails with IllegalArgumentException and leaves the value unchanged`() {
        val point = Point(4.0, 6.0)
        val result = point divideBy 0.0
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        assertEquals(Point(4.0, 6.0), point)
    }

    @Test
    fun `divideBy by negative zero fails with IllegalArgumentException and leaves the value unchanged`() {
        val point = Point(4.0, 6.0)
        val result = point divideBy -0.0
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
        assertEquals(Point(4.0, 6.0), point)
    }

    @Test
    fun `the failure of divideBy carries the message Cannot divide by zero`() {
        val result = Point(4.0, 6.0) divideBy 0.0
        assertEquals("Cannot divide by zero", result.exceptionOrNull()?.message)
    }

    @Test
    fun `divideBy by NaN succeeds and produces NaN components`() {
        val point = Point(4.0, 6.0)
        val result = point divideBy Double.NaN
        assertTrue(result.isSuccess)
        assertTrue(point.containsNaN)
    }

    @Test
    fun `divideBy by infinity succeeds and produces zero components`() {
        val point = Point(1.0, 2.0)
        val result = point divideBy Double.POSITIVE_INFINITY
        assertTrue(result.isSuccess)
        assertEquals(Point(0.0, 0.0), point)
    }

    @Test
    fun `a discarded divideBy result still divides in place`() {
        val point = Point(4.0, 6.0)
        point divideBy 2.0
        assertEquals(Point(2.0, 3.0), point)
    }

    @Test
    fun `divideBy works on Dimensions`() {
        val dimensions = Dimensions(8.0, 4.0)
        val result = dimensions divideBy 4.0
        assertTrue(result.isSuccess)
        assertEquals(Dimensions(2.0, 1.0), dimensions)
    }
}
