package com.spartanlabs.geometry

/**
 * Represents an axis-aligned square (or rectangle) defined by a top-left
 * [location] and its [dimensions]
 */
data class Square (
    /** The top-left corner of the square */
    var location:Point = Point(x = 0.0,y = 0.0),
    /** The width and height of the square */
    var dimensions: Dimensions = Dimensions(width = 0.0,height = 0.0)
)