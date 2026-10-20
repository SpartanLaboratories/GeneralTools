package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.SegmentIntersection
import com.spartanlabs.geometry.segmentIntersectsSegment
import org.junit.jupiter.api.Tag
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [segmentIntersectsSegment].
 * Pure — nothing to mock. The cases mirror the plan §6 truth table.
 */
@Tag("component")
class SegmentSegmentIntersectionTest {
    private val log = LoggerFactory.getLogger(SegmentSegmentIntersectionTest::class.java)

    private val delta = 1e-9

    private fun intersect(s1: Segment, s2: Segment) =
        segmentIntersectsSegment(s1, s2).getOrThrow()

    private fun assertPoint(expected: Point, actual: Point) {
        assertEquals(expected.x, actual.x, delta)
        assertEquals(expected.y, actual.y, delta)
    }

    @Test
    fun `proper crossing yields a touching point`() {
        log.info("Running segment/segment proper-cross test")
        val result = intersect(
            Segment(Point(-1.0, -1.0), Point(1.0, 1.0)),
            Segment(Point(-1.0, 1.0), Point(1.0, -1.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(0.0, 0.0), result.point)
    }

    @Test
    fun `disjoint non-parallel segments do not meet`() {
        log.info("Running segment/segment disjoint test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(2.0, 1.0), Point(2.0, 5.0)),
        )
        assertIs<SegmentIntersection.None>(result)
    }

    @Test
    fun `parallel non-collinear segments do not meet`() {
        log.info("Running segment/segment parallel test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
            Segment(Point(0.0, 1.0), Point(2.0, 1.0)),
        )
        assertIs<SegmentIntersection.None>(result)
    }

    @Test
    fun `collinear overlapping segments yield the shared sub-segment`() {
        log.info("Running segment/segment collinear-overlap test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
            Segment(Point(1.0, 0.0), Point(3.0, 0.0)),
        )
        assertIs<SegmentIntersection.Overlapping>(result)
        assertPoint(Point(1.0, 0.0), result.segment.a)
        assertPoint(Point(2.0, 0.0), result.segment.b)
    }

    @Test
    fun `collinear disjoint segments do not meet`() {
        log.info("Running segment/segment collinear-disjoint test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(2.0, 0.0), Point(3.0, 0.0)),
        )
        assertIs<SegmentIntersection.None>(result)
    }

    @Test
    fun `collinear end-to-end segments touch at the shared point`() {
        log.info("Running segment/segment collinear-touch test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(1.0, 0.0), Point(2.0, 0.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(1.0, 0.0), result.point)
    }

    @Test
    fun `shared endpoint non-collinear segments touch`() {
        log.info("Running segment/segment V-junction test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 1.0)),
            Segment(Point(0.0, 0.0), Point(1.0, -1.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(0.0, 0.0), result.point)
    }

    @Test
    fun `T-junction touches where an endpoint meets an interior`() {
        log.info("Running segment/segment T-junction test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
            Segment(Point(1.0, 0.0), Point(1.0, 1.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(1.0, 0.0), result.point)
    }

    @Test
    fun `a zero-length segment fails`() {
        log.info("Running segment/segment degenerate test")
        val result = segmentIntersectsSegment(
            Segment(Point(1.0, 1.0), Point(1.0, 1.0)),
            Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `a NaN coordinate fails`() {
        log.info("Running segment/segment NaN test")
        val result = segmentIntersectsSegment(
            Segment(Point(Double.NaN, 0.0), Point(1.0, 0.0)),
            Segment(Point(0.0, 1.0), Point(0.0, -1.0)),
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `near-parallel within EPSILON is treated as collinear deterministically`() {
        log.info("Running segment/segment near-parallel test")
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(0.0, 1e-11), Point(1.0, 1e-11)),
        )
        assertIs<SegmentIntersection.Overlapping>(result)
    }
}
