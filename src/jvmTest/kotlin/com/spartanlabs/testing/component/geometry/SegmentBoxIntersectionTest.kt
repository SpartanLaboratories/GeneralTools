package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.Square
import com.spartanlabs.geometry.segmentIntersectsBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [segmentIntersectsBox].
 * Pure — nothing to mock. Core cases run once against a [CenteredBox] and once
 * against the equivalent [Square] to pin the [AxisAlignedBox]-agnostic
 * behaviour.
 */
class SegmentBoxIntersectionTest {
    // Both describe the box with corners (-1,-1) .. (1,1).
    private val centeredBox: AxisAlignedBox = CenteredBox(Point(0.0, 0.0), Dimensions(1.0, 1.0))
    private val squareBox: AxisAlignedBox = Square(Point(-1.0, -1.0), Dimensions(2.0, 2.0))
    private val boxes = listOf(centeredBox, squareBox)

    private fun hits(s: Segment, box: AxisAlignedBox) = segmentIntersectsBox(s, box).getOrThrow()

    @Test
    fun `a fully contained segment intersects`() {
        boxes.forEach { assertTrue(hits(Segment(Point(-0.5, 0.0), Point(0.5, 0.0)), it)) }
    }

    @Test
    fun `a segment fully outside with no crossing misses`() {
        boxes.forEach { assertFalse(hits(Segment(Point(2.0, 2.0), Point(3.0, 3.0)), it)) }
    }

    @Test
    fun `a segment passing straight through both faces intersects`() {
        boxes.forEach { assertTrue(hits(Segment(Point(-3.0, 0.0), Point(3.0, 0.0)), it)) }
    }

    @Test
    fun `a segment crossing one face intersects`() {
        boxes.forEach { assertTrue(hits(Segment(Point(0.0, 0.0), Point(3.0, 0.0)), it)) }
    }

    @Test
    fun `an endpoint exactly on a face intersects`() {
        boxes.forEach { assertTrue(hits(Segment(Point(1.0, 0.0), Point(3.0, 0.0)), it)) }
    }

    @Test
    fun `a segment parallel to and outside a face misses`() {
        boxes.forEach { assertFalse(hits(Segment(Point(-3.0, 2.0), Point(3.0, 2.0)), it)) }
    }

    @Test
    fun `a degenerate segment fails`() {
        val result = segmentIntersectsBox(Segment(Point(0.0, 0.0), Point(0.0, 0.0)), centeredBox)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `a box with negative size fails`() {
        val badBox = CenteredBox(Point(0.0, 0.0), Dimensions(-1.0, -1.0))
        val result = segmentIntersectsBox(Segment(Point(-3.0, 0.0), Point(3.0, 0.0)), badBox)
        assertTrue(result.isFailure)
    }

    @Test
    fun `the CenteredBox and the equivalent Square agree`() {
        val s = Segment(Point(-3.0, 0.5), Point(3.0, 0.5))
        assertEquals(hits(s, centeredBox), hits(s, squareBox))
    }
}
