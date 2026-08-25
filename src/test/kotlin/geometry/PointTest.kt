package com.spartanlabs.geometry

import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PointTest {
    private val log = LoggerFactory.getLogger(PointTest::class.java)

    @Test
    fun `constructor sets x and y`() {
        log.info("Running Point constructor test")
        val point = Point(3.0, 4.0)
        assertEquals(3.0, point.x)
        assertEquals(4.0, point.y)
    }

    @Test
    fun `copy constructor copies x and y`() {
        log.info("Running Point copy-constructor test")
        val original = Point(1.5, 2.5)
        val copy = Point(original)
        assertEquals(original, copy)
    }

    @Test
    fun `distanceFrom computes the euclidean distance`() {
        log.info("Running Point#distanceFrom success test")
        val origin = Point(0.0, 0.0)
        val other = Point(3.0, 4.0)

        val distance = origin distanceFrom other

        assertTrue(distance.isSuccess)
        assertEquals(5.0, distance.getOrThrow())
    }

    @Test
    fun `distanceFrom fails when either point contains NaN`() {
        log.info("Running Point#distanceFrom NaN test")
        val valid = Point(0.0, 0.0)
        val invalid = Point(Double.NaN, 1.0)

        val distance = valid distanceFrom invalid

        assertTrue(distance.isFailure)
        assertTrue(distance.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `setTo updates x and y`() {
        log.info("Running Point#setTo test")
        val point = Point()
        point.setTo(6.0, 7.0)
        assertEquals(6.0, point.x)
        assertEquals(7.0, point.y)
    }

    @Test
    fun `modBy adjusts x and y by the given amounts`() {
        log.info("Running Point#modBy test")
        val point = Point(1.0, 1.0)
        point.modBy(2.0, 3.0)
        assertEquals(3.0, point.x)
        assertEquals(4.0, point.y)
    }

    @Test
    fun `plusAssign adds another point's values`() {
        log.info("Running Point plusAssign test")
        val point = Point(1.0, 1.0)
        point += Point(2.0, 2.0)
        assertEquals(Point(3.0, 3.0), point)
    }

    @Test
    fun `minusAssign subtracts another point's values`() {
        log.info("Running Point minusAssign test")
        val point = Point(5.0, 5.0)
        point -= Point(2.0, 1.0)
        assertEquals(Point(3.0, 4.0), point)
    }

    @Test
    fun `timesAssign multiplies x and y by a magnitude`() {
        log.info("Running Point timesAssign test")
        val point = Point(2.0, 3.0)
        point *= 2.0
        assertEquals(Point(4.0, 6.0), point)
    }

    @Test
    fun `divAssign divides x and y by a magnitude`() {
        log.info("Running Point divAssign test")
        val point = Point(4.0, 6.0)
        point /= 2.0
        assertEquals(Point(2.0, 3.0), point)
    }

    @Test
    fun `divAssign throws when dividing by zero`() {
        log.info("Running Point divAssign-by-zero test")
        val point = Point(4.0, 6.0)
        assertFailsWith<IllegalArgumentException> { point /= 0.0 }
    }

    @Test
    fun `equals returns false for a different type with the same values`() {
        log.info("Running Point#equals different-type test")
        val point = Point(1.0, 2.0)
        val dimensions = Dimensions(1.0, 2.0)
        assertFalse(point.equals(dimensions))
    }

    @Test
    fun `toString formats x and y`() {
        log.info("Running Point#toString test")
        assertEquals("1.0, 2.0", Point(1.0, 2.0).toString())
    }

    @Test
    fun `Dimensions constructor sets width and height`() {
        log.info("Running Dimensions constructor test")
        val dimensions = Dimensions(10.0, 20.0)
        assertEquals(10.0, dimensions.width)
        assertEquals(20.0, dimensions.height)
    }

    @Test
    fun `Dimensions copy constructor copies width and height`() {
        log.info("Running Dimensions copy-constructor test")
        val original = Dimensions(5.0, 6.0)
        val copy = Dimensions(original)
        assertEquals(original, copy)
    }
}
