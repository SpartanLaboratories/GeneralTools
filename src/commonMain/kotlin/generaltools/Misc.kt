@file:JvmMultifileClass
@file:JvmName("MiscKt")
// Shares the JVM facade class MiscKt with MiscJvm.kt; keep the two file annotations in step.

package com.spartanlabs.generaltools

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/** Logger used for diagnostic output from this file's functions; the name is unchanged from 2.x. */
private val log = KotlinLogging.logger("com.spartanlabs.generaltools.Misc")

/**
 * Returns `true` if [validator] returns `true` for any item of [list]; it stops at the first match.
 *
 * Logging (Boundary Ring): one TRACE event per call on the logger `com.spartanlabs.generaltools.Misc`, the
 * same name as in 2.x, so existing backend rules still match. A match logs `Item '<item>' satisfied the
 * predicate`; no match logs `No items satisfied the predicate`. The item's `toString()` is rendered only when
 * TRACE is enabled.
 *
 * In 2.x the [validator] parameter was a `java.util.function.Predicate<T>`.
 *
 * @param T the type of the items in [list].
 * @param list the items to test. It is iterated at most once; a one-shot `Iterable` is consumed up to and
 *   including the first match.
 * @param validator the test applied to each item. It is called at most once per item and never after a match.
 * @return `true` as soon as [validator] returns `true` for an item; `false` if it returns `true` for none,
 *   including when [list] is empty.
 * @throws Throwable any exception thrown by [validator] or by [list]'s iterator propagates unchanged; nothing is
 *   caught or wrapped.
 */
public fun <T> evaluateList(list: Iterable<T>, validator: (T) -> Boolean): Boolean {
    // A loop with an early return rather than any/firstOrNull: any loses the matching item for the log, and
    // firstOrNull cannot tell a null item from no match.
    for (item in list) {
        if (validator(item)) {
            log.trace { "Item '$item' satisfied the predicate" }
            return true
        }
    }
    log.trace { "No items satisfied the predicate" }
    return false
}

/**
 * Takes in a string that describes the action that is being taken
 * and a labda that is the action
 * <br>
 * returns the time that it took for the labda to execute
 */
@OptIn(ExperimentalTime::class)
public fun profile(nameOfAction:String, action:()->Unit): String = "$nameOfAction took ${measureTime(action).inWholeMilliseconds}ms"

/**
 * allows catching multiple exceptions following a runCatching
 * <br>
 * Note: this function is inline and public, so it can't reference a
 * file-private logger directly - logging around exception recovery is left
 * to the caller.
 */
public inline fun <R, T : R> Result<T>.onException(
    vararg exceptions: KClass<out Throwable>,
    transform: (exception: Throwable) -> T
): Result<T> = recoverCatching { ex ->
    if (ex::class in exceptions) {
        transform(ex)
    } else throw ex
}
/**
 * Infix version of forEachIndexed
 */
public infix fun <T> Collection<T>.forEveryIndexed(consume:(Int,T)->Unit): Unit =
    forEachIndexed { index,item->
        consume(index,item)
    }
/**
 * Infix version of forEach
 */
public infix fun <T> Collection<T>.forEvery(consume:(T)->Unit): Unit = forEach { consume(it) }
/**
 * Returns the first [length] characters of the receiver when it is longer than [length], otherwise the receiver
 * itself.
 *
 * @param length the maximum length of the result; must be `>= 0`.
 * @return the receiver trimmed to [length] characters, or the receiver unchanged if it is not longer than [length].
 * @throws IndexOutOfBoundsException if [length] is negative, on every platform and for every receiver (the empty
 *   string included). On the JVM this is a plain `IndexOutOfBoundsException`; 2.x threw its subtype
 *   `StringIndexOutOfBoundsException`.
 */
public infix fun String.trimIfLongerThan(length: Int): String {
    // Explicit check: substring(0, negative) throws on the JVM and wasmJs but returns "" on Kotlin/JS,
    // so the contract is enforced here instead of being inherited from the platform.
    if (length < 0) throw IndexOutOfBoundsException("length must be >= 0 but was $length")
    return if (this.length > length) substring(0, length) else this
}
