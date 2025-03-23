package com.spartanlabs.geometry

data class Location(var x:Long, var y:Long){
    infix fun setTo(new: Location) =     new.let     { x = it.x;  y = it.y;  this }
    infix fun modBy(change: Location) = change.let  { x += it.x; y += it.y; this }
}