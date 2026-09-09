package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square
import org.junit.jupiter.api.Tag
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the [AxisAlignedBox]
 * contract, checked against both implementors ([Square], [CenteredBox]).
 */
@Tag("component")
class AxisAlignedBoxTest {
    private val log = LoggerFactory.getLogger(AxisAlignedBoxTest::class.java)

    private val square: AxisAlignedBox = Square(Point(1.0, 2.0), Dimensions(4.0, 2.0))
    private val centered: AxisAlignedBox = CenteredBox(Point(3.0, 3.0), Dimensions(2.0, 1.0))

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
    fun `Square satisfies the AxisAlignedBox contract`() {
        log.info("Running AxisAlignedBox Square-conformance test")
        assertBoxShape(square)
    }

    @Test
    fun `CenteredBox satisfies the AxisAlignedBox contract`() {
        log.info("Running AxisAlignedBox CenteredBox-conformance test")
        assertBoxShape(centered)
    }

    @Test
    fun `both implementors describe the same box`() {
        log.info("Running AxisAlignedBox equivalence test")
        assertEquals(square.min, centered.min)
        assertEquals(square.max, centered.max)
        assertEquals(square.center, centered.center)
        assertEquals(square.size, centered.size)
    }

    @Test
    fun `a helper taking AxisAlignedBox behaves the same for either implementor`() {
        log.info("Running AxisAlignedBox polymorphic-helper test")
        fun area(box: AxisAlignedBox) = box.size.width * box.size.height
        assertEquals(area(square), area(centered))
    }
}
