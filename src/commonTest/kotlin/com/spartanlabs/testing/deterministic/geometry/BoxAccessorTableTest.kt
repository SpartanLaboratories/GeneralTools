package com.spartanlabs.testing.deterministic.geometry

import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Level 4a (Deterministic Logic / Input-to-Output Mapping) coverage pinning the literal IEEE results of the box
 * primitive accessors, so the expressions behind them cannot drift without a visible diff.
 */
class BoxAccessorTableTest {
    @Test
    fun `Rectangle accessors give the literal IEEE results for fractional parts`() {
        val rect = Rectangle(Point(0.1, 0.2), Dimensions(0.2, 0.4))
        assertEquals(0.1, rect.minX)
        assertEquals(0.2, rect.minY)
        assertEquals(0.30000000000000004, rect.maxX)
        assertEquals(0.6000000000000001, rect.maxY)
        assertEquals(0.2, rect.width)
        assertEquals(0.4, rect.height)
        assertEquals(Point(0.2, 0.4), rect.center)
    }

    @Test
    fun `CenteredBox accessors give the literal IEEE results for fractional parts`() {
        val box = CenteredBox(Point(0.1, 0.2), Dimensions(0.2, 0.4))
        assertEquals(-0.1, box.minX)
        assertEquals(-0.2, box.minY)
        assertEquals(0.30000000000000004, box.maxX)
        assertEquals(0.6000000000000001, box.maxY)
        assertEquals(0.4, box.width)
        assertEquals(0.8, box.height)
    }
}
