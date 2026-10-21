// This file must not import com.spartanlabs.geometry.arithmetic. With the import, += -= *= /= on a var Point or
// Dimensions become ambiguous and the file stops compiling. Its compiling at all is the check.
package com.spartanlabs.testing.component.geometry

import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Level 2 (Isolated Component Behaviour) check of assumption A12: in a file that does not import the
 * arithmetic package, `+=`, `-=`, `*=` and `/=` on a `var` Point or Dimensions still resolve to the in-place
 * member operators, so the holder keeps pointing at the same, mutated instance.
 */
class CompoundAssignmentWithoutArithmeticImportTest {
    @Test
    fun `compound assignments on a var Point resolve to the in-place members`() {
        var p = Point(1.0, 2.0)
        val alias = p
        p += Point(3.0, 4.0)
        assertEquals(Point(4.0, 6.0), p)
        p -= Point(1.0, 1.0)
        assertEquals(Point(3.0, 5.0), p)
        p *= 2.0
        assertEquals(Point(6.0, 10.0), p)
        p /= 2.0
        assertEquals(Point(3.0, 5.0), p)
        assertSame(alias, p)
    }

    @Test
    fun `compound assignments on a var Dimensions resolve to the in-place members`() {
        var d = Dimensions(1.0, 2.0)
        val alias = d
        d += Dimensions(3.0, 4.0)
        assertEquals(Dimensions(4.0, 6.0), d)
        d -= Dimensions(1.0, 1.0)
        assertEquals(Dimensions(3.0, 5.0), d)
        d *= 2.0
        assertEquals(Dimensions(6.0, 10.0), d)
        d /= 2.0
        assertEquals(Dimensions(3.0, 5.0), d)
        assertSame(alias, d)
    }
}
