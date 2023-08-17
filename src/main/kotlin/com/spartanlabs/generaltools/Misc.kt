package com.spartanlabs.generaltools

import java.util.function.Predicate
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Will capitalize the first letter of each word in this String
 */
fun String.capitalizeEveryWord() =
    split(' ').map{it.capitalize()} .toString().let {
        it.substring(1,it.length-1).replace(",","")
    }

/**
 * Takes in a list of objects of a specified type as well as
 * a predicate that takes one of those objects and return a boolean
 * <br>
 * This function will then test each item in the list against the predicate
 * If any item returns true then the function will return true
 * If no items return true then the function will return false
 */
fun <T>evaluateList(list:Iterable<T>, validator: Predicate<T>):Boolean{
    for(t in list)if(validator.test(t))return true
    return false
}

/**
 * Takes in a string that describes the action that is being taken
 * and a labda that is the action
 * <br>
 * returns the time that it took for the labda to execute
 */
@OptIn(ExperimentalTime::class)
fun profile(nameOfAction:String, action:()->Unit) = "$nameOfAction took ${measureTime(action).inWholeMilliseconds}ms"