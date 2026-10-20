package com.spartanlabs.logging

class MessageBuilder(showClass:Boolean = true, showTime:Boolean = false, showThread:Boolean = false, showStackTrace:Boolean = false, val color:IJColor){
    infix fun modify(message:String): String {
        return color + message
    }
}
enum class IJColor(val colorCode:String){
    INFO("\u001b[92m");
    val reset = "\u001b[0m"
    operator fun plus(restOfString:String) = colorCode + restOfString + reset
}