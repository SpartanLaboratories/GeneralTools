package com.spartanlabs.logging

public fun main(){
    Logger() info "test"
}
public class Logger{
    public var printToConsole: Boolean = true
    public val info: MessageBuilder = MessageBuilder(color = IJColor.INFO)
    public infix fun info(message : String) {
        if(printToConsole) println(info modify message)
    }
}