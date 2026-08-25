package com.spartanlabs.geometry

import org.slf4j.LoggerFactory

/** Logger used for diagnostic output from this file's functions. */
private val log = LoggerFactory.getLogger("com.spartanlabs.geometry.TwoDoubles")

/**
 * Base class for any type represented by a pair of Doubles, such as a 2D
 * point or a width/height pair. Provides shared arithmetic and comparison
 * operations for its subclasses.
 */
sealed class TwoDoubles(var first: Double = 0.0, var second:Double = 0.0){
    /** Creates a new instance with the same values as [doubles] */
    constructor(doubles: TwoDoubles): this(doubles.first, doubles.second)
    /** True if either [first] or [second] is NaN */
    val containsNaN get() = first.isNaN() || second.isNaN()
    /** Sets [first] and [second] to the given values */
    fun setTo(first: Double, second: Double) = with(this){
        this.first = first
        this.second = second
    }
    /** Adds the given amounts to [first] and [second] */
    fun modBy(first: Double, second: Double) = with(this){
        this.first  += first
        this.second += second
    }
    /** Sets [first] and [second] to match [new]'s values */
    infix fun setTo(new: TwoDoubles) =    setTo(new.first, new.second)
    /** Adds [change]'s values to [first] and [second] */
    infix fun modBy(change: TwoDoubles) = modBy(change.first, change.second)
    /** Multiplies both [first] and [second] by [magnitude] */
    infix fun amplifyBy(magnitude: Double) {
        first *= magnitude
        second *= magnitude
    }
    /**
     * Divides both [first] and [second] by [magnitude]
     * <br>
     * Throws an [IllegalArgumentException] if [magnitude] is zero
     */
    infix fun divideBy(magnitude: Double) =
        if (magnitude == 0.0) {
            log.error("Attempted to divide a {} by zero", this::class.simpleName)
            throw IllegalArgumentException("Cannot divide by zero")
        }
        else amplifyBy(1/magnitude)
    /** Adds [doubles]'s values to this instance */
    operator fun plusAssign(doubles: TwoDoubles) = modBy(doubles)
    /** Subtracts [doubles]'s values from this instance */
    operator fun minusAssign(doubles: TwoDoubles) = modBy(-doubles.first, -doubles.second)
    /** Multiplies this instance's values by [magnitude] */
    operator fun timesAssign(magnitude: Double) = amplifyBy(magnitude)
    /** Divides this instance's values by [magnitude] */
    operator fun divAssign(magnitude: Double) = divideBy(magnitude)
    /**
     * Two [TwoDoubles] are equal if they are the same concrete type and their
     * [first] and [second] values match
     */
    override operator fun equals(other: Any?):Boolean {
        if(other==null || other.javaClass != this.javaClass) return false
        other as TwoDoubles
        return first == other.first && second == other.second
    }
    /** Returns "first, second" */
    override fun toString(): String {
        return "$first, $second"
    }
}