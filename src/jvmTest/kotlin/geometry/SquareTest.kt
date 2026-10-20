package com.spartanlabs.geometry

import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SquareTest {
    private val log = LoggerFactory.getLogger(SquareTest::class.java)

    @Test
    fun `default square has zeroed location and dimensions`() {
        log.info("Running default-square test")
        val square = Square()
        assertEquals(Point(0.0, 0.0), square.location)
        assertEquals(Dimensions(0.0, 0.0), square.dimensions)
    }

    @Test
    fun `squares with the same location and dimensions are equal`() {
        log.info("Running square-equality test")
        val first = Square(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val second = Square(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        assertEquals(first, second)
    }

    @Test
    fun `squares with different dimensions are not equal`() {
        log.info("Running square-inequality test")
        val first = Square(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        val second = Square(Point(1.0, 2.0), Dimensions(5.0, 6.0))
        assertNotEquals(first, second)
    }

    @Test
    fun `mutating a square's location updates its properties`() {
        log.info("Running square-mutation test")
        val square = Square()
        square.location = Point(9.0, 9.0)
        assertEquals(9.0, square.location.x)
        assertEquals(9.0, square.location.y)
    }
}
