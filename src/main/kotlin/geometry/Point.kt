package com.spartanlabs.geometry

import org.slf4j.LoggerFactory
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt

/** Logger used for diagnostic output from this file's functions. */
private val log = LoggerFactory.getLogger("com.spartanlabs.geometry.Point")

/**
 * Represents a point in 2D space with an [x] and [y] coordinate
 */
class Point(x:Double = 0.0,y:Double = 0.0) : TwoDoubles(x,y){
    /** Creates a new Point with the same coordinates as [point] */
    constructor(point: Point) : this(point.x, point.y)
    /** The horizontal coordinate of this point */
    var x
        get() = first
        set(value) { first = value }
    /** The vertical coordinate of this point */
    var y
        get() = second
        set(value) { second = value }
    /**
     * Computes the euclidean distance between this point and [other]
     * <br>
     * Returns a failed [Result] if either point contains a NaN value
     */
    infix fun distanceFrom(other: Point): Result<Double> =
        if(containsNaN || other.containsNaN) {
            log.warn("Cannot compute distance: one or both points contain NaN values")
            Result.failure(IllegalArgumentException("Point values must be numbers"))
        }
        else Result.success(sqrt((x - other.x).pow(2.0) + (y - other.y).pow(2.0)))
}
/**
 * Represents a 2D size with a [width] and [height]
 */
class Dimensions(width:Double = 0.0,height: Double = 0.0) : TwoDoubles(width,height){
    /** Creates a new Dimensions with the same width and height as [dimensions] */
    constructor(dimensions:Dimensions) : this(dimensions.width,dimensions.height)
    /** The horizontal size */
    var width
        get() = first
        set(value) { first = value }
    /** The vertical size */
    var height
        get() = second
        set(value) { second = value }
}