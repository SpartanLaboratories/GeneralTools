package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.SegmentIntersection
import com.spartanlabs.geometry.segmentIntersectsSegment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [segmentIntersectsSegment].
 * Pure — nothing to mock. The cases mirror the plan §6 truth table.
 */
class SegmentSegmentIntersectionTest {
    private val delta = 1e-9

    private fun intersect(s1: Segment, s2: Segment) =
        segmentIntersectsSegment(s1, s2).getOrThrow()

    private fun assertPoint(expected: Point, actual: Point) {
        assertEquals(expected.x, actual.x, delta)
        assertEquals(expected.y, actual.y, delta)
    }

    @Test
    fun `proper crossing yields a touching point`() {
        val result = intersect(
            Segment(Point(-1.0, -1.0), Point(1.0, 1.0)),
            Segment(Point(-1.0, 1.0), Point(1.0, -1.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(0.0, 0.0), result.point)
    }

    @Test
    fun `disjoint non-parallel segments do not meet`() {
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(2.0, 1.0), Point(2.0, 5.0)),
        )
        assertIs<SegmentIntersection.None>(result)
    }

    @Test
    fun `parallel non-collinear segments do not meet`() {
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
            Segment(Point(0.0, 1.0), Point(2.0, 1.0)),
        )
        assertIs<SegmentIntersection.None>(result)
    }

    @Test
    fun `collinear overlapping segments yield the shared sub-segment`() {
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
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(2.0, 0.0), Point(3.0, 0.0)),
        )
        assertIs<SegmentIntersection.None>(result)
    }

    @Test
    fun `collinear end-to-end segments touch at the shared point`() {
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(1.0, 0.0), Point(2.0, 0.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(1.0, 0.0), result.point)
    }

    @Test
    fun `shared endpoint non-collinear segments touch`() {
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 1.0)),
            Segment(Point(0.0, 0.0), Point(1.0, -1.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(0.0, 0.0), result.point)
    }

    @Test
    fun `T-junction touches where an endpoint meets an interior`() {
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
            Segment(Point(1.0, 0.0), Point(1.0, 1.0)),
        )
        assertIs<SegmentIntersection.Touching>(result)
        assertPoint(Point(1.0, 0.0), result.point)
    }

    @Test
    fun `a zero-length segment fails`() {
        val result = segmentIntersectsSegment(
            Segment(Point(1.0, 1.0), Point(1.0, 1.0)),
            Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `a NaN coordinate fails`() {
        val result = segmentIntersectsSegment(
            Segment(Point(Double.NaN, 0.0), Point(1.0, 0.0)),
            Segment(Point(0.0, 1.0), Point(0.0, -1.0)),
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `near-parallel within EPSILON is treated as collinear deterministically`() {
        val result = intersect(
            Segment(Point(0.0, 0.0), Point(1.0, 0.0)),
            Segment(Point(0.0, 1e-11), Point(1.0, 1e-11)),
        )
        assertIs<SegmentIntersection.Overlapping>(result)
    }
}
