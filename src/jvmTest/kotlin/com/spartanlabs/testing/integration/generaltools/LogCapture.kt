package com.spartanlabs.testing.integration.generaltools

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory

/**
 * Records the events logback receives on one named logger, for level-3 tests that cross the real
 * kotlin-logging -> slf4j -> logback boundary. Intended for `use { }`: closing detaches and stops the appender.
 */
internal class LogCapture private constructor(
    private val logger: Logger,
    private val appender: ListAppender<ILoggingEvent>,
) : AutoCloseable {
    /** The events recorded since [attach], oldest first. */
    val events: List<ILoggingEvent> get() = appender.list

    override fun close() {
        logger.detachAppender(appender)
        appender.stop()
    }

    companion object {
        /**
         * Starts a [ListAppender] on the logback logger named [loggerName].
         *
         * @throws ClassCastException if the test classpath binds slf4j to a backend other than logback
         *   (a programmer error, not an expected failure).
         */
        fun attach(loggerName: String): LogCapture {
            val logger = LoggerFactory.getLogger(loggerName) as Logger
            val appender = ListAppender<ILoggingEvent>().apply {
                context = logger.loggerContext
                start()
            }
            logger.addAppender(appender)
            return LogCapture(logger, appender)
        }
    }
}
