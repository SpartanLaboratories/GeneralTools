package com.spartanlabs.geometry

import io.github.oshai.kotlinlogging.KotlinLogging

/** Logger used for diagnostic output from this file's functions. */
private val log = KotlinLogging.logger("com.spartanlabs.geometry.TwoDoubles")

/**
 * Base class for any type represented by a pair of Doubles, such as a 2D
 * point or a width/height pair. Provides shared arithmetic and comparison
 * operations for its subclasses.
 */
public sealed class TwoDoubles(public var first: Double = 0.0, public var second:Double = 0.0){
    /** Creates a new instance with the same values as [doubles] */
    protected constructor(doubles: TwoDoubles): this(doubles.first, doubles.second)
    /** True if either [first] or [second] is NaN */
    public val containsNaN: Boolean get() = first.isNaN() || second.isNaN()
    /** Sets [first] and [second] to the given values */
    public fun setTo(first: Double, second: Double): Unit = with(this){
        this.first = first
        this.second = second
    }
    /** Adds the given amounts to [first] and [second] */
    public fun modBy(first: Double, second: Double): Unit = with(this){
        this.first  += first
        this.second += second
    }
    /** Sets [first] and [second] to match [new]'s values */
    public infix fun setTo(new: TwoDoubles): Unit =    setTo(new.first, new.second)
    /** Adds [change]'s values to [first] and [second] */
    public infix fun modBy(change: TwoDoubles): Unit = modBy(change.first, change.second)
    /** Multiplies both [first] and [second] by [magnitude] */
    public infix fun amplifyBy(magnitude: Double) {
        first *= magnitude
        second *= magnitude
    }
    /**
     * Divides both [first] and [second] by [magnitude]
     * <br>
     * Throws an [IllegalArgumentException] if [magnitude] is zero
     */
    public infix fun divideBy(magnitude: Double): Unit =
        if (magnitude == 0.0) {
            log.error { "Attempted to divide a ${this::class.simpleName} by zero" }
            throw IllegalArgumentException("Cannot divide by zero")
        }
        else amplifyBy(1/magnitude)
    /** Adds [doubles]'s values to this instance */
    public operator fun plusAssign(doubles: TwoDoubles): Unit = modBy(doubles)
    /** Subtracts [doubles]'s values from this instance */
    public operator fun minusAssign(doubles: TwoDoubles): Unit = modBy(-doubles.first, -doubles.second)
    /** Multiplies this instance's values by [magnitude] */
    public operator fun timesAssign(magnitude: Double): Unit = amplifyBy(magnitude)
    /** Divides this instance's values by [magnitude] */
    public operator fun divAssign(magnitude: Double): Unit = divideBy(magnitude)
    /**
     * Two [TwoDoubles] are equal if they are the same concrete type and their
     * [first] and [second] values match. Paired with [hashCode], which folds in
     * the same concrete type.
     */
    override operator fun equals(other: Any?):Boolean {
        if(other==null || other::class != this::class) return false
        other as TwoDoubles
        return first == other.first && second == other.second
    }
    /**
     * Hash code consistent with [equals]: folds in the concrete class (so a
     * [Point] and a [Dimensions] holding the same values hash differently)
     * along with [first] and [second]. Makes subclasses usable as keys in
     * hash-based collections.
     */
    override fun hashCode(): Int =
        31 * (31 * this::class.hashCode() + first.hashCode()) + second.hashCode()
    /** Returns "first, second" */
    override fun toString(): String {
        return "$first, $second"
    }
}