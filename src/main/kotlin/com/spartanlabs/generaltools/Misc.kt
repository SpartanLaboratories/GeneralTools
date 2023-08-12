package com.spartanlabs.generaltools

/**
 * Will capitalize the first letter of each word in this String
 */
fun String.capitalizeEveryWord() =
    split(' ').map{it.capitalize()} .toString().let {
        it.substring(1,it.length-1).replace(",","")
    }