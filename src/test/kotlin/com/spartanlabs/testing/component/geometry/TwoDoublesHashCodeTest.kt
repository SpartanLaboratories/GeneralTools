package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Segment
import org.junit.jupiter.api.Tag
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Level 2 (Isolated Component Behaviour) coverage for the `TwoDoubles.hashCode`
 * contract fix (plan §1.3 / §3.6): the hash must be consistent with `equals`
 * and fold in the concrete type, so composing types work as hash-map keys.
 */
@Tag("component")
class TwoDoublesHashCodeTest {
    private val log = LoggerFactory.getLogger(TwoDoublesHashCodeTest::class.java)

    @Test
    fun `equal Points share a hashCode`() {
        log.info("Running TwoDoubles equal-Point hashCode test")
        assertEquals(Point(1.0, 2.0).hashCode(), Point(1.0, 2.0).hashCode())
    }

    @Test
    fun `equal Dimensions share a hashCode`() {
        log.info("Running TwoDoubles equal-Dimensions hashCode test")
        assertEquals(Dimensions(3.0, 4.0).hashCode(), Dimensions(3.0, 4.0).hashCode())
    }

    @Test
    fun `Point and Dimensions with the same values hash differently`() {
        log.info("Running TwoDoubles type-folded hashCode test")
        assertNotEquals(Point(1.0, 2.0).hashCode(), Dimensions(1.0, 2.0).hashCode())
    }

    @Test
    fun `a Segment round-trips as a HashMap key`() {
        log.info("Running TwoDoubles Segment map-key test")
        val map = hashMapOf(Segment(Point(0.0, 0.0), Point(1.0, 1.0)) to "diagonal")
        assertEquals("diagonal", map[Segment(Point(0.0, 0.0), Point(1.0, 1.0))])
    }

    @Test
    fun `a CenteredBox round-trips as a HashMap key`() {
        log.info("Running TwoDoubles CenteredBox map-key test")
        val map = hashMapOf(CenteredBox(Point(2.0, 2.0), Dimensions(1.0, 1.0)) to "tile")
        assertEquals("tile", map[CenteredBox(Point(2.0, 2.0), Dimensions(1.0, 1.0))])
    }

    @Test
    fun `a HashSet collapses equal CenteredBoxes`() {
        log.info("Running TwoDoubles CenteredBox hash-set test")
        val set = hashSetOf(
            CenteredBox(Point(0.0, 0.0), Dimensions(1.0, 1.0)),
            CenteredBox(Point(0.0, 0.0), Dimensions(1.0, 1.0)),
        )
        assertEquals(1, set.size)
    }
}
