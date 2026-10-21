@file:JvmMultifileClass
@file:JvmName("MiscKt")
// Shares the JVM facade class MiscKt with Misc.kt; keep the two file annotations in step.

package com.spartanlabs.generaltools

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.MalformedURLException
import java.net.URL
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Logger used for diagnostic output from this file's functions. */
private val log: Logger = LoggerFactory.getLogger("com.spartanlabs.generaltools.Misc")

/**
 * Takes a String that is supposed to be a valid URL and returns a URL
 * Throws a MalformedURLException if a String that is not a valid URL is given
 */
@Throws(MalformedURLException::class) public fun String.asURL(): URL {
    log.debug("Parsing '{}' as a URL", this)
    return URL(this)
}
/**
 * Reads the text file at the specified location and returns a list of
 * Strings designating each line in the file which was read
 */
public fun read(textFile:String): List<String> {
    log.debug("Reading file at path '{}'", textFile)
    val lines = BufferedReader(FileReader(File(textFile))).readLines()
    log.debug("Read {} line(s) from '{}'", lines.size, textFile)
    return lines
}
/** Logs the action name and how long it took to execute it*/
public fun Logger.time(actionName:String,action:()->Unit): Unit = info(profile(actionName,action))
