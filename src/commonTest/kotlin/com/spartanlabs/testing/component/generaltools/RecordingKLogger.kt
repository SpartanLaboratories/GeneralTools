package com.spartanlabs.testing.component.generaltools

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker

/** A [KLogger] that records the events it would emit. Not thread-safe; for single-threaded tests. */
internal class RecordingKLogger(
    override val name: String = "com.spartanlabs.testing.Recording",
    private val enabledLevels: Set<Level> = Level.entries.toSet(),
) : KLogger {
    data class Event(val level: Level, val marker: Marker?, val message: String?, val cause: Throwable?)

    private val recorded = mutableListOf<Event>()
    val events: List<Event> get() = recorded

    override fun isLoggingEnabledFor(level: Level, marker: Marker?): Boolean = level in enabledLevels

    // Mirrors a real backend: the enabled check precedes running the builder block, which is
    // where kotlin-logging evaluates the lazy message lambda.
    override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
        if (!isLoggingEnabledFor(level, marker)) return
        val builder = KLoggingEventBuilder().apply(block)
        recorded += Event(level, marker, builder.message, builder.cause)
    }
}
