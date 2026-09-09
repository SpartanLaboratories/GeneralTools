package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Segment
import org.junit.jupiter.api.Tag
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [Segment]. Pure value
 * type — nothing to mock.
 */
@Tag("component")
class SegmentTest {
    private val log = LoggerFactory.getLogger(SegmentTest::class.java)

    private val delta = 1e-9

    @Test
    fun `delta length and lengthSquared`() {
        log.info("Running Segment#delta test")
        val s = Segment(Point(1.0, 1.0), Point(4.0, 5.0))
        assertEquals(Point(3.0, 4.0), s.delta)
        assertEquals(5.0, s.length, delta)
        assertEquals(25.0, s.lengthSquared, delta)
    }

    @Test
    fun `pointAt endpoints and midpoint`() {
        log.info("Running Segment#pointAt test")
        val s = Segment(Point(0.0, 0.0), Point(2.0, 4.0))
        assertEquals(Point(0.0, 0.0), s.pointAt(0.0))
        assertEquals(Point(2.0, 4.0), s.pointAt(1.0))
        assertEquals(Point(1.0, 2.0), s.pointAt(0.5))
    }

    @Test
    fun `asRay of a proper segment succeeds`() {
        log.info("Running Segment#asRay success test")
        val ray = Segment(Point(1.0, 2.0), Point(4.0, 6.0)).asRay().getOrThrow()
        assertEquals(Point(1.0, 2.0), ray.origin)
        assertEquals(Point(3.0, 4.0), ray.direction)
    }

    @Test
    fun `asRay of a zero-length segment fails`() {
        log.info("Running Segment#asRay degenerate test")
        val result = Segment(Point(1.0, 1.0), Point(1.0, 1.0)).asRay()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `equal segments have equal hashCode`() {
        log.info("Running Segment#equals-hashCode test")
        val a = Segment(Point(1.0, 2.0), Point(3.0, 4.0))
        val b = Segment(Point(1.0, 2.0), Point(3.0, 4.0))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `copy replaces one endpoint`() {
        log.info("Running Segment#copy test")
        val s = Segment(Point(0.0, 0.0), Point(1.0, 1.0))
        assertEquals(Segment(Point(5.0, 5.0), Point(1.0, 1.0)), s.copy(a = Point(5.0, 5.0)))
    }
}
