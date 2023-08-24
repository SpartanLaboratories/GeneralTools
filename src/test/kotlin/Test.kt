
import com.spartanlabs.generaltools.read
import com.spartanlabs.generaltools.time
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Test {
    @Test fun testReadLines(){
        println(read("src/test/resources/keys.txt"))
    }
    @Test fun testLogTime(){
        LoggerFactory.getLogger(Test::class.java).time("print function") { repeat(4){println(it)} }
    }
}