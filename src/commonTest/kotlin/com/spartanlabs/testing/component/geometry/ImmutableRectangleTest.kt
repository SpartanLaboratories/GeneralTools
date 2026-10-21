package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.CenteredBox
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.ImmutableRectangle
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Rectangle
import com.spartanlabs.geometry.toRectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [ImmutableRectangle] and its conversions to and from
 * [Rectangle]. Pure value type - nothing to mock.
 */
class ImmutableRectangleTest {
    private val rect = ImmutableRectangle(1.0, 2.0, 4.0, 6.0)

    @Test
    fun `stores x y width and height as given`() {
        assertEquals(1.0, rect.x)
        assertEquals(2.0, rect.y)
        assertEquals(4.0, rect.width)
        assertEquals(6.0, rect.height)
    }

    @Test
    fun `min max center and size are derived from the four values`() {
        assertEquals(Point(1.0, 2.0), rect.min)
        assertEquals(Point(5.0, 8.0), rect.max)
        assertEquals(Point(3.0, 5.0), rect.center)
        assertEquals(Dimensions(4.0, 6.0), rect.size)
    }

    @Test
    fun `min max center and size return fresh values on every read`() {
        assertNotSame(rect.min, rect.min)
        assertNotSame(rect.max, rect.max)
        assertNotSame(rect.center, rect.center)
        assertNotSame(rect.size, rect.size)

        rect.min.setTo(100.0, 100.0)
        rect.max.setTo(100.0, 100.0)
        rect.center.setTo(100.0, 100.0)
        rect.size.setTo(100.0, 100.0)
        assertEquals(ImmutableRectangle(1.0, 2.0, 4.0, 6.0), rect)
        assertEquals(Point(1.0, 2.0), rect.min)
        assertEquals(Point(5.0, 8.0), rect.max)
        assertEquals(Point(3.0, 5.0), rect.center)
        assertEquals(Dimensions(4.0, 6.0), rect.size)
    }

    @Test
    fun `equal components give equal rectangles with equal hash codes`() {
        val a = ImmutableRectangle(1.5, 2.5, 3.5, 4.5)
        val b = ImmutableRectangle(1.5, 2.5, 3.5, 4.5)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `a difference in any one component makes rectangles unequal`() {
        val base = ImmutableRectangle(1.0, 2.0, 3.0, 4.0)
        assertNotEquals(base, ImmutableRectangle(9.0, 2.0, 3.0, 4.0))
        assertNotEquals(base, ImmutableRectangle(1.0, 9.0, 3.0, 4.0))
        assertNotEquals(base, ImmutableRectangle(1.0, 2.0, 9.0, 4.0))
        assertNotEquals(base, ImmutableRectangle(1.0, 2.0, 3.0, 9.0))
    }

    @Test
    fun `positive and negative zero components are equal and hash alike`() {
        val positive = ImmutableRectangle(0.0, 0.0, 0.0, 0.0)
        val negative = ImmutableRectangle(-0.0, -0.0, -0.0, -0.0)
        assertEquals(positive, negative)
        assertEquals(positive.hashCode(), negative.hashCode())
    }

    @Test
    fun `NaN components are equal and hash alike`() {
        val a = ImmutableRectangle(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
        val zero = 0.0   // a local, so 0.0 / 0.0 is computed at run time rather than flagged as a constant division
        val b = ImmutableRectangle(zero / zero, zero / zero, zero / zero, zero / zero)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `an ImmutableRectangle is never equal to a Rectangle with the same numbers`() {
        val immutable = ImmutableRectangle(1.0, 2.0, 3.0, 4.0)
        val mutable = Rectangle(Point(1.0, 2.0), Dimensions(3.0, 4.0))
        assertFalse(immutable.equals(mutable))
        assertFalse(mutable.equals(immutable))
    }

    @Test
    fun `an ImmutableRectangle is never equal to a CenteredBox over the same footprint`() {
        val immutable = ImmutableRectangle(1.0, 2.0, 4.0, 2.0)
        val centered = CenteredBox(Point(3.0, 3.0), Dimensions(2.0, 1.0))
        assertFalse(immutable.equals(centered))
        assertFalse(centered.equals(immutable))
    }

    @Test
    fun `equals is false for null and for unrelated types`() {
        assertFalse(rect.equals(null))
        assertFalse(rect.equals("ImmutableRectangle(x=1.0, y=2.0, width=4.0, height=6.0)"))
        assertFalse(rect.equals(Point(1.0, 2.0)))
    }

    @Test
    fun `toString prints the class name and the four components`() {
        assertEquals(
            "ImmutableRectangle(x=1.5, y=2.5, width=3.5, height=4.5)",
            ImmutableRectangle(1.5, 2.5, 3.5, 4.5).toString(),
        )
    }

    @Test
    fun `copy with no arguments returns an equal but distinct instance`() {
        val copy = rect.copy()
        assertEquals(rect, copy)
        assertNotSame(rect, copy)
    }

    @Test
    fun `copy replaces only the components that are named`() {
        assertEquals(ImmutableRectangle(9.0, 2.0, 4.0, 6.0), rect.copy(x = 9.0))
        assertEquals(ImmutableRectangle(1.0, 9.0, 4.0, 6.0), rect.copy(y = 9.0))
        assertEquals(ImmutableRectangle(1.0, 2.0, 9.0, 6.0), rect.copy(width = 9.0))
        assertEquals(ImmutableRectangle(1.0, 2.0, 4.0, 9.0), rect.copy(height = 9.0))
    }

    @Test
    fun `usable as a hash key`() {
        val map = HashMap<ImmutableRectangle, String>()
        map[ImmutableRectangle(1.0, 2.0, 3.0, 4.0)] = "zone"
        assertEquals("zone", map[ImmutableRectangle(1.0, 2.0, 3.0, 4.0)])

        val set = HashSet<ImmutableRectangle>()
        set.add(ImmutableRectangle(0.0, 0.0, 1.0, 1.0))
        set.add(ImmutableRectangle(-0.0, -0.0, 1.0, 1.0))
        assertEquals(1, set.size)
    }

    @Test
    fun `construction does not validate its components`() {
        val negative = ImmutableRectangle(-1.0, -2.0, -3.0, -4.0)
        val nan = ImmutableRectangle(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
        val infinite = ImmutableRectangle(
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
        )
        assertEquals(-3.0, negative.width)
        assertTrue(nan.x.isNaN())
        assertEquals(Double.POSITIVE_INFINITY, infinite.width)
    }

    @Test
    fun `toRectangle returns an equal mutable rectangle`() {
        val first = rect.toRectangle()
        val second = rect.toRectangle()
        assertEquals(Rectangle(Point(1.0, 2.0), Dimensions(4.0, 6.0)), first)
        assertNotSame(first.location, second.location)
        assertNotSame(first.dimensions, second.dimensions)
    }

    @Test
    fun `a Rectangle survives toImmutable then toRectangle`() {
        val original = Rectangle(Point(1.5, -2.5), Dimensions(3.5, 4.5))
        assertEquals(original, original.toImmutable().toRectangle())
    }

    @Test
    fun `an ImmutableRectangle survives toRectangle then toImmutable`() {
        listOf(
            ImmutableRectangle(1.5, -2.5, 3.5, 4.5),
            ImmutableRectangle(Double.NaN, 0.0, Double.NaN, 1.0),
            ImmutableRectangle(-0.0, -0.0, -0.0, -0.0),
        ).forEach { original ->
            assertEquals(original, original.toRectangle().toImmutable(), "$original")
        }
    }
}
