package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [AxisAlignedBox.contains], in both spellings
 * (`p in box` and `box contains p`), over every implementor in [BoxFixtures]. Pure value types - nothing to mock.
 */
class AxisAlignedBoxContainsTest {
    // The box with corners (1, 2) .. (5, 4).
    private val boxes = BoxFixtures.forFootprint(1.0, 2.0, 4.0, 2.0)

    /** Points from (-1, 0) to (7, 6) in steps of 0.5: inside, on every edge and corner, and outside on all sides. */
    private val grid: List<Point> = (-2..14).flatMap { i -> (0..12).map { j -> Point(i * 0.5, j * 0.5) } }

    @Test
    fun `p in box and box contains p agree for every implementor`() {
        boxes.forEach { (name, box) ->
            grid.forEach { p ->
                val infix = box contains p
                assertEquals(infix, p in box, "$name, $p")
                assertEquals(!infix, p !in box, "$name, $p")
            }
        }
    }

    @Test
    fun `a point on the boundary is contained`() {
        val boundary = listOf(
            Point(3.0, 2.0), Point(3.0, 4.0), Point(1.0, 3.0), Point(5.0, 3.0),   // each edge
            Point(1.0, 2.0), Point(5.0, 2.0), Point(1.0, 4.0), Point(5.0, 4.0),   // each corner
        )
        boxes.forEach { (name, box) -> boundary.forEach { p -> assertTrue(p in box, "$name, $p") } }
    }

    @Test
    fun `a point outside on any side is not contained`() {
        val outside = listOf(Point(0.5, 3.0), Point(5.5, 3.0), Point(3.0, 1.5), Point(3.0, 4.5))
        boxes.forEach { (name, box) -> outside.forEach { p -> assertFalse(p in box, "$name, $p") } }
    }

    @Test
    fun `a point with a NaN coordinate is never contained`() {
        val nanPoints = listOf(Point(Double.NaN, 3.0), Point(3.0, Double.NaN))
        boxes.forEach { (name, box) -> nanPoints.forEach { p -> assertFalse(p in box, "$name, $p") } }
    }

    @Test
    fun `a box with a NaN part contains no point`() {
        BoxFixtures.forFootprint(1.0, 2.0, Double.NaN, 2.0).forEach { (name, box) ->
            grid.forEach { p -> assertFalse(p in box, "$name, $p") }
        }
    }

    @Test
    fun `a box with negative size contains no point`() {
        BoxFixtures.forFootprint(0.0, 0.0, -2.0, -2.0).forEach { (name, box) ->
            grid.forEach { p -> assertFalse(p in box, "$name, $p") }
            listOf(Point(0.0, 0.0), Point(-1.0, -1.0), Point(-2.0, -2.0)).forEach { p ->
                assertFalse(p in box, "$name, $p")
            }
        }
    }

    @Test
    fun `contains gives the same answers as comparing against min and max`() {
        boxes.forEach { (name, box) ->
            grid.forEach { p ->
                val expected = p.x >= box.min.x && p.x <= box.max.x && p.y >= box.min.y && p.y <= box.max.y
                assertEquals(expected, p in box, "$name, $p")
            }
        }
    }
}
