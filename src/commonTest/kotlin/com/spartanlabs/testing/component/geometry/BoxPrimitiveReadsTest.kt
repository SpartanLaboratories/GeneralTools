package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Ray
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.rayIntersectsBox
import com.spartanlabs.geometry.segmentIntersectsBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage proving that [AxisAlignedBox.contains], [segmentIntersectsBox]
 * and [rayIntersectsBox] read only the primitive accessors of a box that overrides them, never its
 * allocating `min`/`max`/`center`/`size`. Pure - nothing to mock.
 */
class BoxPrimitiveReadsTest {
    /** Overrides the six accessors from stored numbers; every read of a derived member is counted. */
    private class CountingBox(
        private val x: Double,
        private val y: Double,
        private val w: Double,
        private val h: Double,
    ) : AxisAlignedBox {
        var derivedReads = 0

        override val minX: Double get() = x
        override val minY: Double get() = y
        override val maxX: Double get() = x + w
        override val maxY: Double get() = y + h
        override val width: Double get() = w
        override val height: Double get() = h

        override val min: Point get() = Point(x, y).also { derivedReads++ }
        override val max: Point get() = Point(x + w, y + h).also { derivedReads++ }
        override val center: Point get() = Point(x + w / 2.0, y + h / 2.0).also { derivedReads++ }
        override val size: Dimensions get() = Dimensions(w, h).also { derivedReads++ }
    }

    // The box with corners (-1, -1) .. (1, 1).
    private fun countingBox() = CountingBox(-1.0, -1.0, 2.0, 2.0)

    @Test
    fun `contains reads only the primitive accessors`() {
        val box = countingBox()
        assertTrue(Point(0.0, 0.0) in box)
        assertFalse(Point(3.0, 0.0) in box)
        assertEquals(0, box.derivedReads)
    }

    @Test
    fun `segmentIntersectsBox reads only the primitive accessors`() {
        val contained = Segment(Point(-0.5, 0.0), Point(0.5, 0.0))
        val crossing = Segment(Point(-3.0, 0.0), Point(3.0, 0.0))
        val miss = Segment(Point(2.0, 2.0), Point(3.0, 3.0))
        val box = countingBox()
        assertTrue(segmentIntersectsBox(contained, box).getOrThrow())
        assertTrue(segmentIntersectsBox(crossing, box).getOrThrow())
        assertFalse(segmentIntersectsBox(miss, box).getOrThrow())
        assertEquals(0, box.derivedReads)
    }

    @Test
    fun `rayIntersectsBox reads only the primitive accessors`() {
        val hit = Ray(Point(-3.0, 0.0), Point(1.0, 0.0))
        val miss = Ray(Point(-3.0, 3.0), Point(1.0, 0.0))
        val originInside = Ray(Point(0.0, 0.0), Point(1.0, 0.0))
        val box = countingBox()
        assertEquals(2.0, rayIntersectsBox(hit, box).getOrThrow())
        assertEquals(null, rayIntersectsBox(miss, box).getOrThrow())
        assertEquals(0.0, rayIntersectsBox(originInside, box).getOrThrow())
        assertEquals(0, box.derivedReads)
    }
}
