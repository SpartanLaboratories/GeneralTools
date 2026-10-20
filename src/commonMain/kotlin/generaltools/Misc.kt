@file:JvmMultifileClass
@file:JvmName("MiscKt")
// Shares the JVM facade class MiscKt with MiscJvm.kt; keep the two file annotations in step.

package com.spartanlabs.generaltools

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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
 * Checks if the receiver's length is greater than the given length,
 * if it is then it trims the receiver to the given length,
 * if it is not then it returns the receiver
 */
public infix fun String.trimIfLongerThan(length:Int): String = if(this.length > length) substring(0,length) else this
