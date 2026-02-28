package geometry

import kotlin.test.Test

import com.spartanlabs.geometry.Point
import kotlin.test.assertEquals

class Point {
    @Test
    fun testCompareTo() {
        val point1 = Point()
        val point2 = Point()
        val point3 = Point(1.0, 0.0)
        assert(point1 == point2 && point1 != point3)
    }
    @Test
    fun testModBy(){
        val point = Point()
        assertEquals(point.x, 0.0)
        assertEquals(point.y, 0.0)
        val change = Point(3.0, 2.0)
        assertEquals(change.x, 3.0)
        assertEquals(change.y, 2.0)
        point.modBy(change)
        assertEquals(point.x, 3.0)
        assertEquals(point.y, 2.0)
    }
    @Test
    fun testSetTo(){
        val point = Point()
        assertEquals(point.x, 0.0)
        assertEquals(point.y, 0.0)
        val new = Point(1.0, 4.0)
        point.setTo(new)
        assertEquals(point.x, new.x)
        assertEquals(point.y, new.y)
    }
}