package com.spartanlabs.logging

fun main(){
    Logger() info "test"
}
class Logger{
    var printToConsole = true
    val info = MessageBuilder(color = IJColor.INFO)
    infix fun info(message : String) {
        if(printToConsole) println(info modify message)
    }
}