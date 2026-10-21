package com.spartanlabs.testing.integration.geometry.arithmetic

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.arithmetic.div
import com.spartanlabs.geometry.arithmetic.minus
import com.spartanlabs.geometry.arithmetic.plus
import com.spartanlabs.geometry.arithmetic.times
import com.spartanlabs.testing.integration.generaltools.LogCapture
import org.slf4j.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Level 3 (Integration) coverage for the arithmetic package through the real kotlin-logging -> slf4j -> logback
 * path: `div` by zero throws without logging, as `/=` does, and no successful operator logs. Each test captures
 * the root logger, which sees every logger's events.
 */
class ArithmeticDivisionLoggingTest {
    @Test
    fun `Point div by zero throws and logs nothing`() {
        LogCapture.attach(Logger.ROOT_LOGGER_NAME).use { capture ->
            assertFailsWith<IllegalArgumentException> { Point(4.0, 6.0) / 0.0 }
            assertEquals(0, capture.events.size)
        }
    }

    @Test
    fun `Point div by negative zero throws and logs nothing`() {
        LogCapture.attach(Logger.ROOT_LOGGER_NAME).use { capture ->
            assertFailsWith<IllegalArgumentException> { Point(4.0, 6.0) / -0.0 }
            assertEquals(0, capture.events.size)
        }
    }

    @Test
    fun `Dimensions div by zero throws and logs nothing`() {
        LogCapture.attach(Logger.ROOT_LOGGER_NAME).use { capture ->
            assertFailsWith<IllegalArgumentException> { Dimensions(4.0, 6.0) / 0.0 }
            assertEquals(0, capture.events.size)
        }
    }

    @Test
    fun `successful operators log nothing`() {
        LogCapture.attach(Logger.ROOT_LOGGER_NAME).use { capture ->
            val p = Point(1.0, 2.0)
            val d = Dimensions(3.0, 4.0)
            p + p
            p + d
            p - p
            p - d
            p * 2.0
            2.0 * p
            p / 2.0
            d + d
            d - d
            d * 2.0
            2.0 * d
            d / 2.0
            assertEquals(0, capture.events.size)
        }
    }
}
