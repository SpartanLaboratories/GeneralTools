package com.spartanlabs.testing.integration.geometry

import ch.qos.logback.classic.Level
import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Ray
import com.spartanlabs.geometry.Segment
import com.spartanlabs.geometry.normalized
import com.spartanlabs.geometry.rayIntersectsBox
import com.spartanlabs.geometry.segmentIntersectsBox
import com.spartanlabs.geometry.segmentIntersectsSegment
import com.spartanlabs.testing.integration.generaltools.LogCapture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Level 3 (Integration) coverage for the eleven expected-failure sites of the geometry package, through the
 * real kotlin-logging -> slf4j -> logback path: each failure keeps its 2.x exception message and logs exactly
 * one WARN line, `<message>: <detail>`, without a throwable, on its file's 2.x logger name.
 */
class FailureSitesLoggingTest {
    private val pointLogger = "com.spartanlabs.geometry.Point"
    private val segmentLogger = "com.spartanlabs.geometry.Segment"
    private val vectorsLogger = "com.spartanlabs.geometry.Vectors"
    private val intersectionsLogger = "com.spartanlabs.geometry.Intersections"
    private val twoDoublesLogger = "com.spartanlabs.geometry.TwoDoubles"

    private val validBox = CenteredBox(Point(0.0, 0.0), Dimensions(1.0, 1.0))

    /** Asserts [result] failed with [message] and [capture] holds the single WARN event the failure logged. */
    private fun assertLoggedFailure(
        result: Result<*>,
        capture: LogCapture,
        loggerName: String,
        message: String,
        line: String,
    ) {
        assertTrue(result.isFailure)
        assertEquals(message, result.exceptionOrNull()!!.message)
        assertEquals(1, capture.events.size)
        val event = capture.events.single()
        assertEquals(Level.WARN, event.level)
        assertEquals(loggerName, event.loggerName)
        assertEquals(line, event.formattedMessage)
        assertNull(event.throwableProxy)
    }

    @Test
    fun `distanceFrom with a NaN point fails and logs one WARN on the Point logger`() {
        LogCapture.attach(pointLogger).use { capture ->
            val result = Point(Double.NaN, 1.0) distanceFrom Point(2.0, 3.0)
            assertLoggedFailure(
                result, capture, pointLogger,
                "Point values must be numbers",
                "Point values must be numbers: Point.distanceFrom: NaN coordinate in (NaN, 1.0) or (2.0, 3.0)",
            )
        }
    }

    @Test
    fun `asRay of a degenerate segment fails and logs one WARN on the Segment logger`() {
        LogCapture.attach(segmentLogger).use { capture ->
            val result = Segment(Point(1.0, 1.0), Point(1.0, 1.0)).asRay()
            assertLoggedFailure(
                result, capture, segmentLogger,
                "Segment must be finite and longer than EPSILON",
                "Segment must be finite and longer than EPSILON: " +
                    "Segment.asRay: degenerate or non-finite segment Segment(a=1.0, 1.0, b=1.0, 1.0)",
            )
        }
    }

    @Test
    fun `normalized of a zero vector fails and logs one WARN on the Vectors logger`() {
        LogCapture.attach(vectorsLogger).use { capture ->
            val result = Point(0.0, 0.0).normalized()
            assertLoggedFailure(
                result, capture, vectorsLogger,
                "Vector must be finite and longer than EPSILON to be normalized",
                "Vector must be finite and longer than EPSILON to be normalized: " +
                    "Point.normalized: zero-length or non-finite vector (0.0, 0.0)",
            )
        }
    }

    @Test
    fun `unit of a zero-direction ray logs exactly one WARN, from normalized`() {
        LogCapture.attach(vectorsLogger).use { capture ->
            val result = Ray(Point(0.0, 0.0), Point(0.0, 0.0)).unit()
            assertTrue(result.isFailure)
            assertEquals(1, capture.events.size)
            assertEquals(Level.WARN, capture.events.single().level)
            assertEquals(vectorsLogger, capture.events.single().loggerName)
        }
    }

    @Test
    fun `segmentIntersectsSegment with a NaN coordinate fails and logs one WARN`() {
        val s1 = Segment(Point(Double.NaN, 0.0), Point(1.0, 0.0))
        val s2 = Segment(Point(0.0, 1.0), Point(1.0, 1.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                segmentIntersectsSegment(s1, s2), capture, intersectionsLogger,
                "Segments must have finite coordinates",
                "Segments must have finite coordinates: segmentIntersectsSegment: NaN coordinate in " +
                    "Segment(a=NaN, 0.0, b=1.0, 0.0) or Segment(a=0.0, 1.0, b=1.0, 1.0)",
            )
        }
    }

    @Test
    fun `segmentIntersectsSegment with a degenerate segment fails and logs one WARN`() {
        val s1 = Segment(Point(1.0, 1.0), Point(1.0, 1.0))
        val s2 = Segment(Point(0.0, 0.0), Point(1.0, 0.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                segmentIntersectsSegment(s1, s2), capture, intersectionsLogger,
                "Segments must be longer than EPSILON",
                "Segments must be longer than EPSILON: segmentIntersectsSegment: degenerate segment ($s1 or $s2)",
            )
        }
    }

    @Test
    fun `segmentIntersectsBox with a negative-size box fails and logs one WARN`() {
        val box = CenteredBox(Point(0.0, 0.0), Dimensions(-1.0, 1.0))
        val s = Segment(Point(0.0, 0.0), Point(1.0, 0.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                segmentIntersectsBox(s, box), capture, intersectionsLogger,
                "Box size must be non-negative",
                "Box size must be non-negative: segmentIntersectsBox: invalid box $box",
            )
        }
    }

    @Test
    fun `segmentIntersectsBox with a degenerate segment fails and logs one WARN`() {
        val s = Segment(Point(5.0, 5.0), Point(5.0, 5.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                segmentIntersectsBox(s, validBox), capture, intersectionsLogger,
                "Segment must be finite and longer than EPSILON",
                "Segment must be finite and longer than EPSILON: " +
                    "segmentIntersectsBox: degenerate or non-finite segment $s",
            )
        }
    }

    @Test
    fun `rayIntersectsBox with a NaN-size box fails and logs one WARN`() {
        val box = CenteredBox(Point(0.0, 0.0), Dimensions(Double.NaN, 1.0))
        val r = Ray(Point(-5.0, 0.0), Point(1.0, 0.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                rayIntersectsBox(r, box), capture, intersectionsLogger,
                "Box size must be finite",
                "Box size must be finite: rayIntersectsBox: invalid box $box",
            )
        }
    }

    @Test
    fun `rayIntersectsBox with a non-finite box corner fails and logs one WARN`() {
        val box = CenteredBox(Point(Double.POSITIVE_INFINITY, 0.0), Dimensions(1.0, 1.0))
        val r = Ray(Point(-5.0, 0.0), Point(1.0, 0.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                rayIntersectsBox(r, box), capture, intersectionsLogger,
                "Box corners must be finite",
                "Box corners must be finite: rayIntersectsBox: non-finite box corner (${box.min}) and (${box.max})",
            )
        }
    }

    @Test
    fun `rayIntersectsBox with a non-finite ray fails and logs one WARN`() {
        val r = Ray(Point(Double.NaN, 0.0), Point(1.0, 0.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                rayIntersectsBox(r, validBox), capture, intersectionsLogger,
                "Ray must have finite coordinates",
                "Ray must have finite coordinates: rayIntersectsBox: non-finite ray $r",
            )
        }
    }

    @Test
    fun `rayIntersectsBox with a zero-direction ray fails and logs one WARN`() {
        val r = Ray(Point(0.0, 0.0), Point(0.0, 0.0))
        LogCapture.attach(intersectionsLogger).use { capture ->
            assertLoggedFailure(
                rayIntersectsBox(r, validBox), capture, intersectionsLogger,
                "Ray direction must be longer than EPSILON",
                "Ray direction must be longer than EPSILON: rayIntersectsBox: zero-direction ray $r",
            )
        }
    }

    @Test
    fun `successful geometry calls log nothing`() {
        val captures = listOf(pointLogger, segmentLogger, vectorsLogger, intersectionsLogger, twoDoublesLogger)
            .map(LogCapture::attach)
        try {
            assertTrue((Point(0.0, 0.0) distanceFrom Point(3.0, 4.0)).isSuccess)
            assertTrue(Point(3.0, 4.0).normalized().isSuccess)
            assertTrue(Segment(Point(0.0, 0.0), Point(1.0, 0.0)).asRay().isSuccess)
            assertTrue(
                segmentIntersectsSegment(
                    Segment(Point(0.0, 0.0), Point(2.0, 0.0)),
                    Segment(Point(1.0, -1.0), Point(1.0, 1.0)),
                ).isSuccess
            )
            assertTrue(segmentIntersectsBox(Segment(Point(-2.0, 0.0), Point(2.0, 0.0)), validBox).isSuccess)
            assertTrue(rayIntersectsBox(Ray(Point(-5.0, 0.0), Point(1.0, 0.0)), validBox).isSuccess)
            captures.forEach { assertEquals(0, it.events.size) }
        } finally {
            captures.forEach(LogCapture::close)
        }
    }
}
