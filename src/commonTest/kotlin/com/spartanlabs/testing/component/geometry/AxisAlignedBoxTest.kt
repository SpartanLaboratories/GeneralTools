package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.ImmutableRectangle
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the [AxisAlignedBox]
 * contract, checked against all three implementors ([Rectangle], [ImmutableRectangle], [CenteredBox]).
 */
class AxisAlignedBoxTest {
    private val rectangle: AxisAlignedBox = Rectangle(Point(1.0, 2.0), Dimensions(4.0, 2.0))
    private val centered: AxisAlignedBox = CenteredBox(Point(3.0, 3.0), Dimensions(2.0, 1.0))
    private val immutable: AxisAlignedBox = ImmutableRectangle(1.0, 2.0, 4.0, 2.0)

    private fun assertBoxShape(box: AxisAlignedBox) {
        assertEquals(Point(1.0, 2.0), box.min)
        assertEquals(Point(5.0, 4.0), box.max)
        assertEquals(Point(3.0, 3.0), box.center)
        assertEquals(Dimensions(4.0, 2.0), box.size)
        assertTrue(box contains Point(3.0, 3.0))   // interior
        assertTrue(box contains Point(1.0, 2.0))   // boundary corner
        assertFalse(box contains Point(0.0, 0.0))  // exterior
    }

    @Test
    fun `Rectangle satisfies the AxisAlignedBox contract`() {
        assertBoxShape(rectangle)
    }

    @Test
    fun `CenteredBox satisfies the AxisAlignedBox contract`() {
        assertBoxShape(centered)
    }

    @Test
    fun `ImmutableRectangle satisfies the AxisAlignedBox contract`() {
        assertBoxShape(immutable)
    }

    @Test
    fun `both implementors describe the same box`() {
        assertEquals(rectangle.min, centered.min)
        assertEquals(rectangle.max, centered.max)
        assertEquals(rectangle.center, centered.center)
        assertEquals(rectangle.size, centered.size)
    }

    @Test
    fun `all three implementors describe the same box`() {
        listOf(centered, immutable).forEach { other ->
            assertEquals(rectangle.min, other.min)
            assertEquals(rectangle.max, other.max)
            assertEquals(rectangle.center, other.center)
            assertEquals(rectangle.size, other.size)
        }
    }

    @Test
    fun `a helper taking AxisAlignedBox behaves the same for either implementor`() {
        fun area(box: AxisAlignedBox) = box.size.width * box.size.height
        assertEquals(area(rectangle), area(centered))
    }
}
