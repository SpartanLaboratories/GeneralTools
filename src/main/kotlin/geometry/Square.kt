package com.spartanlabs.geometry

data class Square (
    var location:Point = Point(x = 0.0,y = 0.0),
    var dimensions: Dimensions = Dimensions(width = 0.0,height = 0.0)
)