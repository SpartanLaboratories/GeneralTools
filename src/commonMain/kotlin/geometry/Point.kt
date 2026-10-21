package com.spartanlabs.geometry

import com.spartanlabs.generaltools.ExperimentalForInheritanceGeneralToolsApi
import com.spartanlabs.generaltools.failure
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.pow
import kotlin.math.sqrt

/** Logger used for diagnostic output from this file's functions. */
private val log = KotlinLogging.logger("com.spartanlabs.geometry.Point")

/**
 * Represents a point in 2D space with an [x] and [y] coordinate
 *
 * Final: equality is keyed on the concrete class, so to define another coordinate pair extend [TwoDoubles]
 * (Experimental) instead of this class.
 */
// Extending TwoDoubles is Experimental; using Point is not.
@OptIn(ExperimentalForInheritanceGeneralToolsApi::class)
public class Point(x:Double = 0.0,y:Double = 0.0) : TwoDoubles(x,y){
    /** Creates a new Point with the same coordinates as [point] */
    public constructor(point: Point) : this(point.x, point.y)
    /** The horizontal coordinate of this point */
    public var x: Double
        get() = first
        set(value) { first = value }
    /** The vertical coordinate of this point */
    public var y: Double
        get() = second
        set(value) { second = value }
    /**
     * Computes the euclidean distance between this point and [other]
     * <br>
     * Returns a failed [Result] if either point contains a NaN value.
     * On failure also logs one WARN line on this file's logger (see the README's Logging section).
     */
    public infix fun distanceFrom(other: Point): Result<Double> =
        if (containsNaN || other.containsNaN)
            log.failure("Point values must be numbers") { "Point.distanceFrom: NaN coordinate in ($this) or ($other)" }
        else Result.success(sqrt((x - other.x).pow(2.0) + (y - other.y).pow(2.0)))
    /**
     * A new [Point] with both coordinates negated; this point is unchanged. `-Point(0.0, 0.0)` is
     * `Point(-0.0, -0.0)`, which equals `Point(0.0, 0.0)` under the equality contract. [Dimensions] has no
     * negation because a negative size is not a valid size.
     *
     * @return the negated point, a new instance.
     */
    public operator fun unaryMinus(): Point = Point(-x, -y)
}
/**
 * Represents a 2D size with a [width] and [height]
 *
 * Final: equality is keyed on the concrete class, so to define another coordinate pair extend [TwoDoubles]
 * (Experimental) instead of this class.
 */
// Extending TwoDoubles is Experimental; using Dimensions is not.
@OptIn(ExperimentalForInheritanceGeneralToolsApi::class)
public class Dimensions(width:Double = 0.0,height: Double = 0.0) : TwoDoubles(width,height){
    /** Creates a new Dimensions with the same width and height as [dimensions] */
    public constructor(dimensions:Dimensions) : this(dimensions.width,dimensions.height)
    /** The horizontal size */
    public var width: Double
        get() = first
        set(value) { first = value }
    /** The vertical size */
    public var height: Double
        get() = second
        set(value) { second = value }
}