package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [Point] and [Dimensions]. Pure value types - nothing to mock.
 */
class PointTest {
    @Test
    fun `constructor sets x and y`() {
        val point = Point(3.0, 4.0)
        assertEquals(3.0, point.x)
        assertEquals(4.0, point.y)
    }

    @Test
    fun `copy constructor copies x and y`() {
        val original = Point(1.5, 2.5)
        val copy = Point(original)
        assertEquals(original, copy)
    }

    @Test
    fun `distanceFrom computes the euclidean distance`() {
        val origin = Point(0.0, 0.0)
        val other = Point(3.0, 4.0)

        val distance = origin distanceFrom other

        assertTrue(distance.isSuccess)
        assertEquals(5.0, distance.getOrThrow())
    }

    @Test
    fun `distanceFrom fails when either point contains NaN`() {
        val valid = Point(0.0, 0.0)
        val invalid = Point(Double.NaN, 1.0)

        val distance = valid distanceFrom invalid

        assertTrue(distance.isFailure)
        assertTrue(distance.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `setTo updates x and y`() {
        val point = Point()
        point.setTo(6.0, 7.0)
        assertEquals(6.0, point.x)
        assertEquals(7.0, point.y)
    }

    @Test
    fun `modBy adjusts x and y by the given amounts`() {
        val point = Point(1.0, 1.0)
        point.modBy(2.0, 3.0)
        assertEquals(3.0, point.x)
        assertEquals(4.0, point.y)
    }

    @Test
    fun `plusAssign adds another point's values`() {
        val point = Point(1.0, 1.0)
        point += Point(2.0, 2.0)
        assertEquals(Point(3.0, 3.0), point)
    }

    @Test
    fun `minusAssign subtracts another point's values`() {
        val point = Point(5.0, 5.0)
        point -= Point(2.0, 1.0)
        assertEquals(Point(3.0, 4.0), point)
    }

    @Test
    fun `timesAssign multiplies x and y by a magnitude`() {
        val point = Point(2.0, 3.0)
        point *= 2.0
        assertEquals(Point(4.0, 6.0), point)
    }

    @Test
    fun `divAssign divides x and y by a magnitude`() {
        val point = Point(4.0, 6.0)
        point /= 2.0
        assertEquals(Point(2.0, 3.0), point)
    }

    @Test
    fun `divAssign throws when dividing by zero`() {
        val point = Point(4.0, 6.0)
        assertFailsWith<IllegalArgumentException> { point /= 0.0 }
    }

    @Test
    fun `equals returns false for a different type with the same values`() {
        val point = Point(1.0, 2.0)
        val dimensions = Dimensions(1.0, 2.0)
        assertFalse(point.equals(dimensions))
    }

    @Test
    fun `toString formats x and y`() {
        // Layout only: Double.toString differs per platform (JS prints 1.0 as 1), so the values below are
        // ones that print identically on every target.
        assertEquals("1.5, -2.25", Point(1.5, -2.25).toString())
    }

    @Test
    fun `Dimensions constructor sets width and height`() {
        val dimensions = Dimensions(10.0, 20.0)
        assertEquals(10.0, dimensions.width)
        assertEquals(20.0, dimensions.height)
    }

    @Test
    fun `Dimensions copy constructor copies width and height`() {
        val original = Dimensions(5.0, 6.0)
        val copy = Dimensions(original)
        assertEquals(original, copy)
    }

    //region Former top-level class geometry.Point
    @Test
    fun testCompareTo() {
        val point1 = Point()
        val point2 = Point()
        val point3 = Point(1.0, 0.0)
        assertTrue(point1 == point2 && point1 != point3)
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
    //endregion
}
