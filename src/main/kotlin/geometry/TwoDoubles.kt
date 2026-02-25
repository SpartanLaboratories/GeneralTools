package com.spartanlabs.geometry

sealed abstract class TwoDoubles(var first: Double = 0.0, var second:Double = 0.0){
    infix fun setTo(new: TwoDoubles) =    new.let     { first  = it.first; second  = it.second}
    infix fun modBy(change: TwoDoubles) = change.let  { first += it.first; second += it.second}
}