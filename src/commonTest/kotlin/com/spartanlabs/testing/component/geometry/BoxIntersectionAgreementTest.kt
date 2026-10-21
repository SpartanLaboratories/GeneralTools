package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Ray
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.rayIntersectsBox
import com.spartanlabs.geometry.segmentIntersectsBox
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Level 2 (Isolated Component Behaviour) coverage that every implementor in [BoxFixtures] gives the same
 * [segmentIntersectsBox] and [rayIntersectsBox] result for the same footprint, failures included. Pure - nothing
 * to mock.
 */
class BoxIntersectionAgreementTest {
    // The box with corners (-1, -1) .. (1, 1), once per implementor.
    private val boxes = BoxFixtures.forFootprint(-1.0, -1.0, 2.0, 2.0)

    private fun <T> Result<T>.summary(): String = fold({ "ok($it)" }, { "failure(${it.message})" })

    @Test
    fun `every implementor gives the same segmentIntersectsBox result for the same footprint`() {
        val segments = listOf(
            "contained" to Segment(Point(-0.5, 0.0), Point(0.5, 0.0)),
            "through" to Segment(Point(-3.0, 0.5), Point(3.0, 0.5)),
            "outside" to Segment(Point(2.0, 2.0), Point(3.0, 3.0)),
            "parallel-outside" to Segment(Point(-3.0, 2.0), Point(3.0, 2.0)),
            "corner-touching" to Segment(Point(1.0, 1.0), Point(2.0, 2.0)),
            "degenerate" to Segment(Point(0.0, 0.0), Point(0.0, 0.0)),
            "NaN" to Segment(Point(Double.NaN, 0.0), Point(3.0, 0.0)),
        )
        segments.forEach { (name, s) ->
            val summaries = boxes.map { (boxName, box) -> boxName to segmentIntersectsBox(s, box).summary() }
            assertEquals(1, summaries.map { it.second }.distinct().size, "$name segment: $summaries")
        }
    }

    @Test
    fun `every implementor gives the same rayIntersectsBox result for the same footprint`() {
        val rays = listOf(
            "hit" to Ray(Point(-3.0, 0.0), Point(1.0, 0.0)),
            "origin-inside" to Ray(Point(0.0, 0.0), Point(0.0, 1.0)),
            "behind" to Ray(Point(3.0, 0.0), Point(1.0, 0.0)),
            "parallel-outside" to Ray(Point(-3.0, 2.0), Point(1.0, 0.0)),
            "origin on a face plane" to Ray(Point(-1.0, 3.0), Point(0.0, -1.0)),
            "zero direction" to Ray(Point(-3.0, 0.0), Point(0.0, 0.0)),
            "non-finite" to Ray(Point(Double.POSITIVE_INFINITY, 0.0), Point(1.0, 0.0)),
        )
        rays.forEach { (name, r) ->
            val summaries = boxes.map { (boxName, box) -> boxName to rayIntersectsBox(r, box).summary() }
            assertEquals(1, summaries.map { it.second }.distinct().size, "$name ray: $summaries")
        }
    }
}
