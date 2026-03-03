package com.spartanlabs.geometry

import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt


class Point(x:Double = 0.0,y:Double = 0.0) : TwoDoubles(x,y){
    constructor(point: Point) : this(point.x, point.y)
    var x
        get() = first
        set(value) { first = value }
    var y
        get() = second
        set(value) { second = value }
    infix fun distanceFrom(other: Point): Result<Double> =
        if(containsNaN || other.containsNaN) Result.failure(IllegalArgumentException("Point values must be numbers"))
        else Result.success(sqrt((x - other.x).pow(2.0) + (y - other.y).pow(2.0)))
}
class Dimensions(width:Double = 0.0,height: Double = 0.0) : TwoDoubles(width,height){
    constructor(dimensions:Dimensions) : this(dimensions.width,dimensions.height)
    var width
        get() = first
        set(value) { first = value }
    var height
        get() = second
        set(value) { second = value }
}