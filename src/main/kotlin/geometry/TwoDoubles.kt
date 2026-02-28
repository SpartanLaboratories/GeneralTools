package com.spartanlabs.geometry

sealed abstract class TwoDoubles(var first: Double = 0.0, var second:Double = 0.0){
    constructor(doubles: TwoDoubles): this(doubles.first, doubles.second)
    fun setTo(first: Double, second: Double) = with(this){
        this.first = first
        this.second = second
    }
    fun modBy(first: Double, second: Double) = with(this){
        this.first  += first
        this.second += second
    }
    infix fun setTo(new: TwoDoubles) =    setTo(new.first, new.second)
    infix fun modBy(change: TwoDoubles) = modBy(change.first, change.second)
    override operator fun equals(other: Any?):Boolean {
        if(other==null || other.javaClass != this.javaClass) return false
        other as TwoDoubles
        return first == other.first && second == other.second
    }
}