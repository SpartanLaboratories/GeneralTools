package com.spartanlabs.geometry

import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TwoDoubles is sealed and abstract, so its behavior is exercised here through
 * its concrete subclass Point. Point/Dimensions-specific behavior (x/y, width/height,
 * distanceFrom, etc.) is covered separately in PointTest.
 */
class TwoDoublesTest {
    private val log = LoggerFactory.getLogger(TwoDoublesTest::class.java)

    @Test
    fun `containsNaN is true when either value is NaN`() {
        log.info("Running containsNaN true-case test")
        assertTrue(Point(Double.NaN, 0.0).containsNaN)
        assertTrue(Point(0.0, Double.NaN).containsNaN)
    }

    @Test
    fun `containsNaN is false for regular numbers`() {
        log.info("Running containsNaN false-case test")
        assertFalse(Point(1.0, 2.0).containsNaN)
    }

    @Test
    fun `infix setTo copies another instance's values`() {
        log.info("Running infix setTo test")
        val point = Point()
        point setTo Point(4.0, 5.0)
        assertEquals(Point(4.0, 5.0), point)
    }

    @Test
    fun `infix modBy adjusts by another instance's values`() {
        log.info("Running infix modBy test")
        val point = Point(1.0, 1.0)
        point modBy Point(2.0, 3.0)
        assertEquals(Point(3.0, 4.0), point)
    }

    @Test
    fun `amplifyBy multiplies both values`() {
        log.info("Running amplifyBy test")
        val point = Point(2.0, 3.0)
        point amplifyBy 3.0
        assertEquals(Point(6.0, 9.0), point)
    }

    @Test
    fun `equals is false when compared to null`() {
        log.info("Running equals-with-null test")
        assertFalse(Point(1.0, 1.0).equals(null))
    }

    @Test
    fun `equals is true for the same values and type`() {
        log.info("Running equals-same-type test")
        assertTrue(Point(1.0, 1.0) == Point(1.0, 1.0))
    }
}
