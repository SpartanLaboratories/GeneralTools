fun main(){
    val log = Logger()
    log info "test"
}
class Logger{
    var printToConsole = true
    val info = MessageBuilder()
    infix fun info(message : String) {
        if(printToConsole) println(info modify message)
    }
}