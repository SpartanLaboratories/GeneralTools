package com.spartanlabs.testing.component.generaltools

import com.spartanlabs.generaltools.failure
import io.github.oshai.kotlinlogging.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the internal `KLogger.failure` helper. The logging
 * backend is replaced by [RecordingKLogger].
 */
class FailureTest {

    @Test
    fun `failure returns a failed Result holding an IllegalArgumentException with the message`() {
        val result: Result<Double> = RecordingKLogger().failure("m")
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()!!
        assertEquals(IllegalArgumentException::class, exception::class)
        assertEquals("m", exception.message)
    }

    @Test
    fun `failure logs exactly one WARN event on the receiver logger`() {
        val log = RecordingKLogger()
        log.failure<Double>("m") { "d" }
        assertEquals(1, log.events.size)
        assertEquals(Level.WARN, log.events.single().level)
        assertNull(log.events.single().marker)
    }

    @Test
    fun `failure logs the message alone when no detail is given`() {
        val log = RecordingKLogger()
        log.failure<Double>("m")
        assertEquals("m", log.events.single().message)
    }

    @Test
    fun `failure logs the message, a colon and a space, then the detail text`() {
        val log = RecordingKLogger()
        log.failure<Double>("m") { "d" }
        assertEquals("m: d", log.events.single().message)
    }

    @Test
    fun `failure renders a detail that returns null as the text null`() {
        val log = RecordingKLogger()
        log.failure<Double>("m") { null }
        assertEquals("m: null", log.events.single().message)
    }

    @Test
    fun `failure attaches no throwable to the log event`() {
        val log = RecordingKLogger()
        log.failure<Double>("m") { "d" }
        assertNull(log.events.single().cause)
    }

    @Test
    fun `failure does not evaluate detail when WARN is disabled`() {
        val log = RecordingKLogger(enabledLevels = Level.entries.toSet() - Level.WARN)
        var evaluations = 0
        val result: Result<Double> = log.failure("m") { evaluations++; "d" }
        assertEquals(0, evaluations)
        assertTrue(log.events.isEmpty())
        assertTrue(result.isFailure)
        assertEquals("m", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `failure evaluates detail exactly once when WARN is enabled`() {
        val log = RecordingKLogger()
        var evaluations = 0
        log.failure<Double>("m") { evaluations++; "d" }
        assertEquals(1, evaluations)
    }

    @Test
    fun `failure keeps the detail out of the exception`() {
        val log = RecordingKLogger()
        val exception = log.failure<Double>("m") { "detail text" }.exceptionOrNull()!!
        assertEquals("m", exception.message)
        assertFalse(exception.message!!.contains("detail text"))
        assertNull(exception.cause)
    }

    @Test
    fun `a throwing detail does not turn the failure into an exception`() {
        val log = RecordingKLogger()
        val result: Result<Double> = log.failure("m") { throw IllegalStateException("boom") }
        assertTrue(result.isFailure)
        assertEquals("m", result.exceptionOrNull()!!.message)
        // The event's text is kotlin-logging's wording for a failed message lambda, so it is not asserted.
        assertEquals(1, log.events.size)
    }

    @Test
    fun `failure infers its success type from the expected type`() {
        val log = RecordingKLogger()
        val a: Result<Int> = log.failure("x")
        val b: Result<List<String>> = log.failure("y")
        assertEquals("x", a.exceptionOrNull()!!.message)
        assertEquals("y", b.exceptionOrNull()!!.message)
    }
}
