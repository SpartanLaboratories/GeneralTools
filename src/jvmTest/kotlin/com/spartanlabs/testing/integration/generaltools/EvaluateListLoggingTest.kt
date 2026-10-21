package com.spartanlabs.testing.integration.generaltools

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.spartanlabs.generaltools.evaluateList
import org.slf4j.LoggerFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 3 (Integration) coverage for the logging of `evaluateList` through the real
 * kotlin-logging -> slf4j -> logback path: a match and a miss each log one TRACE event on the
 * `com.spartanlabs.generaltools.Misc` logger, the name `evaluateList` logged under in 2.x.
 */
class EvaluateListLoggingTest {
    private val miscLogger = "com.spartanlabs.generaltools.Misc"
    private val logbackLogger = LoggerFactory.getLogger(miscLogger) as Logger
    private var previousLevel: Level? = null
    private lateinit var capture: LogCapture

    @BeforeTest
    fun attachCaptureAtTrace() {
        previousLevel = logbackLogger.level
        logbackLogger.level = Level.TRACE
        capture = LogCapture.attach(miscLogger)
    }

    @AfterTest
    fun closeCaptureAndRestoreLevel() {
        capture.close()
        logbackLogger.level = previousLevel
    }

    @Test
    fun `evaluateList logs the satisfying item at TRACE on the Misc logger`() {
        assertTrue(evaluateList(listOf(1, 2, 3)) { it == 2 })
        val event = capture.events.single()
        assertEquals(Level.TRACE, event.level)
        assertEquals(miscLogger, event.loggerName)
        assertEquals("Item '2' satisfied the predicate", event.formattedMessage)
    }

    @Test
    fun `evaluateList logs a miss at TRACE on the Misc logger`() {
        assertFalse(evaluateList(listOf(1, 2, 3)) { it == 5 })
        val event = capture.events.single()
        assertEquals(Level.TRACE, event.level)
        assertEquals(miscLogger, event.loggerName)
        assertEquals("No items satisfied the predicate", event.formattedMessage)
    }
}
