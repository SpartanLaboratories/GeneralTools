package com.spartanlabs.logging

public class MessageBuilder(showClass:Boolean = true, showTime:Boolean = false, showThread:Boolean = false, showStackTrace:Boolean = false, public val color:IJColor){
    public infix fun modify(message:String): String {
        return color + message
    }
}
public enum class IJColor(public val colorCode:String){
    INFO("\u001b[92m");
    public val reset: String = "\u001b[0m"
    public operator fun plus(restOfString:String): String = colorCode + restOfString + reset
}