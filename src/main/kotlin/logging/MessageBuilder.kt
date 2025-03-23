package com.spartanlabs.logging

class MessageBuilder(showClass:Boolean = true, showTime:Boolean = false, showThread:Boolean = false, showStackTrace:Boolean = false){
    infix fun modify(message:String): String {
        return message
    }
}