package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.AxisAlignedBox
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Ray
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.rayIntersectsBox
import com.spartanlabs.geometry.segmentIntersectsBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage pinning the exact 2.x failure messages of
 * [segmentIntersectsBox] and [rayIntersectsBox] for an unusable box, over every implementor in [BoxFixtures].
 * Pure - nothing to mock.
 */
class BoxFailureMessagesTest {
    private val segment = Segment(Point(-3.0, 0.5), Point(3.0, 0.5))
    private val ray = Ray(Point(-3.0, 0.5), Point(1.0, 0.0))

    private fun assertFailsWith(expectedMessage: String, result: Result<*>, label: String) {
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalArgumentException, "$label: expected IllegalArgumentException, was $error")
        assertEquals(expectedMessage, error.message, label)
    }

    private fun assertBothFunctionsFailWith(expectedMessage: String, boxes: List<Pair<String, AxisAlignedBox>>) {
        boxes.forEach { (name, box) ->
            assertFailsWith(expectedMessage, segmentIntersectsBox(segment, box), "segmentIntersectsBox, $name")
            assertFailsWith(expectedMessage, rayIntersectsBox(ray, box), "rayIntersectsBox, $name")
        }
    }

    @Test
    fun `a box with a NaN size fails with the finite-size message`() {
        assertBothFunctionsFailWith("Box size must be finite", BoxFixtures.forFootprint(0.0, 0.0, Double.NaN, 1.0))
    }

    @Test
    fun `a box with an infinite size fails with the finite-size message`() {
        assertBothFunctionsFailWith(
            "Box size must be finite",
            BoxFixtures.forFootprint(0.0, 0.0, Double.POSITIVE_INFINITY, 1.0),
        )
    }

    @Test
    fun `a box with a negative size fails with the non-negative message`() {
        assertBothFunctionsFailWith("Box size must be non-negative", BoxFixtures.forFootprint(0.0, 0.0, -1.0, -1.0))
    }

    @Test
    fun `a box with a non-finite corner fails rayIntersectsBox with the corners message`() {
        BoxFixtures.forFootprint(Double.POSITIVE_INFINITY, 0.0, 1.0, 1.0).forEach { (name, box) ->
            assertFailsWith("Box corners must be finite", rayIntersectsBox(ray, box), "rayIntersectsBox, $name")
        }
    }

    @Test
    fun `a bad box is reported before a bad segment`() {
        val degenerate = Segment(Point(0.0, 0.0), Point(0.0, 0.0))
        BoxFixtures.forFootprint(0.0, 0.0, -1.0, -1.0).forEach { (name, box) ->
            assertFailsWith(
                "Box size must be non-negative",
                segmentIntersectsBox(degenerate, box),
                "segmentIntersectsBox, $name",
            )
        }
    }
}
