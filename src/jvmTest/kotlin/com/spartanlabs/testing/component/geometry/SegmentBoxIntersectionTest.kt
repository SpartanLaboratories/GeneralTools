package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.Square
import com.spartanlabs.geometry.segmentIntersectsBox
import org.junit.jupiter.api.Tag
import org.slf4j.LoggerFactory
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
@Tag("component")
class SegmentBoxIntersectionTest {
    private val log = LoggerFactory.getLogger(SegmentBoxIntersectionTest::class.java)

    // Both describe the box with corners (-1,-1) .. (1,1).
    private val centeredBox: AxisAlignedBox = CenteredBox(Point(0.0, 0.0), Dimensions(1.0, 1.0))
    private val squareBox: AxisAlignedBox = Square(Point(-1.0, -1.0), Dimensions(2.0, 2.0))
    private val boxes = listOf(centeredBox, squareBox)

    private fun hits(s: Segment, box: AxisAlignedBox) = segmentIntersectsBox(s, box).getOrThrow()

    @Test
    fun `a fully contained segment intersects`() {
        log.info("Running segment/box fully-inside test")
        boxes.forEach { assertTrue(hits(Segment(Point(-0.5, 0.0), Point(0.5, 0.0)), it)) }
    }

    @Test
    fun `a segment fully outside with no crossing misses`() {
        log.info("Running segment/box fully-outside test")
        boxes.forEach { assertFalse(hits(Segment(Point(2.0, 2.0), Point(3.0, 3.0)), it)) }
    }

    @Test
    fun `a segment passing straight through both faces intersects`() {
        log.info("Running segment/box through test")
        boxes.forEach { assertTrue(hits(Segment(Point(-3.0, 0.0), Point(3.0, 0.0)), it)) }
    }

    @Test
    fun `a segment crossing one face intersects`() {
        log.info("Running segment/box one-face test")
        boxes.forEach { assertTrue(hits(Segment(Point(0.0, 0.0), Point(3.0, 0.0)), it)) }
    }

    @Test
    fun `an endpoint exactly on a face intersects`() {
        log.info("Running segment/box endpoint-on-face test")
        boxes.forEach { assertTrue(hits(Segment(Point(1.0, 0.0), Point(3.0, 0.0)), it)) }
    }

    @Test
    fun `a segment parallel to and outside a face misses`() {
        log.info("Running segment/box parallel-outside test")
        boxes.forEach { assertFalse(hits(Segment(Point(-3.0, 2.0), Point(3.0, 2.0)), it)) }
    }

    @Test
    fun `a degenerate segment fails`() {
        log.info("Running segment/box degenerate test")
        val result = segmentIntersectsBox(Segment(Point(0.0, 0.0), Point(0.0, 0.0)), centeredBox)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `a box with negative size fails`() {
        log.info("Running segment/box negative-size test")
        val badBox = CenteredBox(Point(0.0, 0.0), Dimensions(-1.0, -1.0))
        val result = segmentIntersectsBox(Segment(Point(-3.0, 0.0), Point(3.0, 0.0)), badBox)
        assertTrue(result.isFailure)
    }

    @Test
    fun `the CenteredBox and the equivalent Square agree`() {
        log.info("Running segment/box CenteredBox-vs-Square test")
        val s = Segment(Point(-3.0, 0.5), Point(3.0, 0.5))
        assertEquals(hits(s, centeredBox), hits(s, squareBox))
    }
}
