package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Level 2 (Isolated Component Behaviour) coverage for the [AxisAlignedBox] accessor contract: every primitive
 * accessor of every implementor equals the matching component of its own `min`/`max`/`size`, bit for bit.
 * Pure value types - nothing to mock.
 */
class AxisAlignedBoxAccessorContractTest {
    private val inf = Double.POSITIVE_INFINITY

    /** Footprints (x, y, w, h): ordinary, negative origin, fractional, signed zero, NaN size, infinite, overflow, negative size. */
    private val footprints = listOf(
        "ordinary" to listOf(1.0, 2.0, 4.0, 2.0),
        "negative origin" to listOf(-3.0, -5.0, 2.0, 1.0),
        "fractional" to listOf(0.1, 0.2, 0.2, 0.4),
        "signed zero" to listOf(-0.0, -0.0, -0.0, 0.0),
        "NaN size" to listOf(1.0, 2.0, Double.NaN, Double.NaN),
        "infinite" to listOf(0.0, -inf, inf, inf),
        "overflow" to listOf(1e308, 1e308, 1e308, 1e308),
        "negative size" to listOf(0.0, 0.0, -2.0, -2.0),
    )

    /** Compares with `toBits()` so `0.0`/`-0.0` are told apart and every NaN matches NaN. */
    private fun assertBitsEqual(expected: Double, actual: Double, message: String) =
        assertEquals(expected.toBits(), actual.toBits(), "$message: expected $expected, was $actual")

    private fun assertAccessorsMatchComponents(box: AxisAlignedBox, label: String) {
        assertBitsEqual(box.min.x, box.minX, "$label minX")
        assertBitsEqual(box.min.y, box.minY, "$label minY")
        assertBitsEqual(box.max.x, box.maxX, "$label maxX")
        assertBitsEqual(box.max.y, box.maxY, "$label maxY")
        assertBitsEqual(box.size.width, box.width, "$label width")
        assertBitsEqual(box.size.height, box.height, "$label height")
    }

    private fun boxesNamed(implementor: String): List<Pair<String, AxisAlignedBox>> =
        footprints.map { (name, f) ->
            name to BoxFixtures.forFootprint(f[0], f[1], f[2], f[3]).single { it.first == implementor }.second
        }

    @Test
    fun `Rectangle accessors equal its min max and size components`() {
        boxesNamed("Rectangle").forEach { (name, box) -> assertAccessorsMatchComponents(box, "Rectangle $name") }
    }

    @Test
    fun `CenteredBox accessors equal its min max and size components`() {
        boxesNamed("CenteredBox").forEach { (name, box) -> assertAccessorsMatchComponents(box, "CenteredBox $name") }
    }

    @Test
    fun `a box that only implements min max center and size derives the accessors from them`() {
        boxesNamed("defaults-only box").forEach { (name, box) ->
            assertAccessorsMatchComponents(box, "defaults-only box $name")
        }
    }

    @Test
    fun `accessors follow in-place changes to the live parts`() {
        val rect = Rectangle(Point(1.0, 2.0), Dimensions(4.0, 2.0))
        val rectMinX = rect.minX
        val rectMaxX = rect.maxX
        rect.location.setTo(10.0, 20.0)
        assertNotEquals(rectMinX, rect.minX)
        assertNotEquals(rectMaxX, rect.maxX)
        assertEquals(10.0, rect.minX)
        assertEquals(14.0, rect.maxX)

        val centered = CenteredBox(Point(3.0, 3.0), Dimensions(2.0, 1.0))
        val centeredMinX = centered.minX
        val centeredMaxX = centered.maxX
        centered.center.setTo(10.0, 10.0)
        assertNotEquals(centeredMinX, centered.minX)
        assertNotEquals(centeredMaxX, centered.maxX)
        assertEquals(8.0, centered.minX)
        assertEquals(12.0, centered.maxX)
    }
}
