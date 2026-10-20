package com.spartanlabs.testing.integration.geometry

import ch.qos.logback.classic.Level
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.testing.integration.generaltools.LogCapture
import org.slf4j.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Level 3 (Integration) coverage for the logging of `TwoDoubles` division through the real
 * kotlin-logging -> slf4j -> logback path: a zero `divideBy` logs exactly one WARN line, without a throwable,
 * on the `com.spartanlabs.geometry.TwoDoubles` logger; `/=` logs nothing, its exception being the report.
 */
class TwoDoublesDivisionLoggingTest {
    private val twoDoublesLogger = "com.spartanlabs.geometry.TwoDoubles"

    @Test
    fun `divideBy by zero logs exactly one WARN line naming the receiver class`() {
        LogCapture.attach(twoDoublesLogger).use { capture ->
            assertTrue((Point(4.0, 6.0) divideBy 0.0).isFailure)
            assertEquals(1, capture.events.size)
            val event = capture.events.single()
            assertEquals(Level.WARN, event.level)
            assertEquals(twoDoublesLogger, event.loggerName)
            assertEquals("Cannot divide by zero: Point.divideBy: (4.0, 6.0) by 0.0", event.formattedMessage)
        }
    }

    @Test
    fun `divideBy by negative zero logs exactly one WARN line`() {
        LogCapture.attach(twoDoublesLogger).use { capture ->
            assertTrue((Point(4.0, 6.0) divideBy -0.0).isFailure)
            assertEquals(1, capture.events.size)
            val event = capture.events.single()
            assertEquals(Level.WARN, event.level)
            assertTrue(event.formattedMessage.endsWith("by -0.0"), event.formattedMessage)
        }
    }

    @Test
    fun `divideBy on Dimensions names Dimensions in the WARN line`() {
        LogCapture.attach(twoDoublesLogger).use { capture ->
            assertTrue((Dimensions(8.0, 4.0) divideBy 0.0).isFailure)
            assertEquals(
                "Cannot divide by zero: Dimensions.divideBy: (8.0, 4.0) by 0.0",
                capture.events.single().formattedMessage,
            )
        }
    }

    @Test
    fun `the WARN line of divideBy carries no throwable`() {
        LogCapture.attach(twoDoublesLogger).use { capture ->
            Point(4.0, 6.0) divideBy 0.0
            assertNull(capture.events.single().throwableProxy)
        }
    }

    @Test
    fun `divideBy by a non-zero divisor logs nothing`() {
        LogCapture.attach(twoDoublesLogger).use { capture ->
            assertTrue((Point(4.0, 6.0) divideBy 2.0).isSuccess)
            assertEquals(0, capture.events.size)
        }
    }

    @Test
    fun `divAssign by zero throws and logs nothing`() {
        // The root logger sees every logger's events, so nothing logged anywhere slips past this capture.
        LogCapture.attach(Logger.ROOT_LOGGER_NAME).use { capture ->
            val point = Point(4.0, 6.0)
            assertFailsWith<IllegalArgumentException> { point /= 0.0 }
            assertEquals(0, capture.events.size)
        }
    }

    @Test
    fun `divAssign by a non-zero divisor logs nothing`() {
        LogCapture.attach(Logger.ROOT_LOGGER_NAME).use { capture ->
            val point = Point(4.0, 6.0)
            point /= 2.0
            assertEquals(0, capture.events.size)
        }
    }
}
