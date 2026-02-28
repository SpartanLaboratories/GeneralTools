package com.spartanlabs.geometry



class Point(x:Double = 0.0,y:Double = 0.0) : TwoDoubles(x,y){
    constructor(point: Point) : this(point.x, point.y)
    var x = first
    var y = second
}
class Dimensions(width:Double = 0.0,height: Double = 0.0) : TwoDoubles(width,height){
    constructor(dimensions:Dimensions) : this(dimensions.width,dimensions.height)
    var width = first
    var height = second
}