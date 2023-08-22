package com.spartanlabs.generaltools

import org.slf4j.Logger
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.MalformedURLException
import java.net.URL
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
 * Takes a String that is supposed to be a valid URL and returns a URL
 * Throws a MalformedURLException if a String that is not a valid URL is given
 */
@Throws(MalformedURLException::class) fun String.asURL(): URL = URL(this)
/**
 * Reads the text file at the specified location and returns a list of
 * Strings designating each line in the file which was read
 */
fun read(textFile:String) = BufferedReader(FileReader(File(textFile))).readLines()
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
/** Logs the action name and how long it took to execute it*/
fun Logger.time(actionName:String,action:()->Unit) = info(actionName,action)