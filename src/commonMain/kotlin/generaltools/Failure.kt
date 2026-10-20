package com.spartanlabs.generaltools

import io.github.oshai.kotlinlogging.KLogger

/**
 * Reports an expected failure: logs one WARN line on this logger and returns the failure.
 *
 * The returned [Result] holds `IllegalArgumentException(message)` - the exception carries [message] and
 * nothing else, so the text callers see is stable. The WARN line is `message` alone when [detail] is
 * `null`, otherwise `"message: <detail>"`; it carries no throwable and therefore no stack trace.
 * [detail] is evaluated lazily, only when WARN is enabled on this logger, and never reaches the
 * exception. A [detail] that throws an `Exception` does not turn the failure into a throw.
 *
 * Boundary note: this is the library's only failure-reporting path for `Result`-returning functions.
 * Each call is one log event on the calling file's logger (`com.spartanlabs.<package>.<File>`); on the
 * JVM it reaches the consumer's slf4j backend, on JS/Wasm the console at the single global level.
 *
 * @param T the success type of the `Result` the caller returns; inferred from the call site.
 * @param message the exception message; also the head of the log line. Keep it short and stable.
 * @param detail optional diagnostics (function name and offending values) appended to the log line only.
 * @return `Result.failure(IllegalArgumentException(message))`; never a success.
 */
internal fun <T> KLogger.failure(message: String, detail: (() -> Any?)? = null): Result<T> {
    // Built from message alone: the exception text is documented API and must stay stable.
    val exception = IllegalArgumentException(message)
    // No Throwable is passed to warn: a stack trace here would only ever point at this helper.
    // detail is a lambda so the offending values' toString runs only when WARN is enabled.
    warn { if (detail == null) message else "$message: ${detail()}" }
    return Result.failure(exception)
}
