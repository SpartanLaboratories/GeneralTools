package com.spartanlabs.testing.component.generaltools

import com.spartanlabs.generaltools.time
import org.slf4j.LoggerFactory
import kotlin.test.Test

/**
 * Level 2 (Isolated Component Behaviour) coverage for the slf4j `Logger.time` extension. The [org.slf4j.Logger]
 * is the receiver under test, so the test builds one locally.
 */
class LoggerTimeTest {
    @Test
    fun `Logger time logs the profiled action without throwing`() {
        val logger = LoggerFactory.getLogger(LoggerTimeTest::class.java)
        logger.time("sample action") { }
    }
}
