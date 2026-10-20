package com.spartanlabs.testing.integration.geometry

import ch.qos.logback.classic.Level
import com.spartanlabs.geometry.Point
import com.spartanlabs.testing.integration.generaltools.LogCapture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Level 3 (Integration) coverage pinning the interim `TwoDoubles.divideBy` behaviour through the real
 * kotlin-logging -> slf4j -> logback path: dividing by zero throws and logs one ERROR line.
 */
class DivideByLoggingTest {
    private val twoDoublesLogger = "com.spartanlabs.geometry.TwoDoubles"

    /** Asserts [capture] holds exactly the one ERROR event `divideBy(0.0)` logs for a [Point]. */
    private fun assertSingleDivideByZeroError(capture: LogCapture) {
        assertEquals(1, capture.events.size)
        val event = capture.events.single()
        assertEquals(Level.ERROR, event.level)
        assertEquals(twoDoublesLogger, event.loggerName)
        assertEquals("Attempted to divide a Point by zero", event.formattedMessage)
    }

    @Test
    fun `divideBy zero still throws and logs one ERROR on the TwoDoubles logger`() {
        LogCapture.attach(twoDoublesLogger).use { capture ->
            val exception = assertFailsWith<IllegalArgumentException> { Point(1.0, 1.0).divideBy(0.0) }
            assertEquals("Cannot divide by zero", exception.message)
            assertSingleDivideByZeroError(capture)
        }
    }

    @Test
    fun `divAssign by zero reaches the same ERROR line`() {
        LogCapture.attach(twoDoublesLogger).use { capture ->
            var p = Point(1.0, 1.0)
            val exception = assertFailsWith<IllegalArgumentException> { p /= 0.0 }
            assertEquals("Cannot divide by zero", exception.message)
            assertSingleDivideByZeroError(capture)
        }
    }
}
