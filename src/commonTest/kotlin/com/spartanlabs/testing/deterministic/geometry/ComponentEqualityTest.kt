package com.spartanlabs.testing.deterministic.geometry

import com.spartanlabs.geometry.componentEquals
import com.spartanlabs.geometry.componentHash
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Level 4a (Deterministic Logic / Input-to-Output Mapping) coverage for the internal pure functions
 * componentEquals and componentHash, which define the TwoDoubles equality and hash contract per component.
 */
class ComponentEqualityTest {
    private val nonCanonicalNaN = Double.fromBits(0x7ff8000000000001)

    @Test
    fun `componentEquals maps a table of inputs to the contract's answers`() {
        val cases = listOf(
            Triple(1.0, 1.0, true),
            Triple(0.0, -0.0, true),
            Triple(Double.NaN, Double.NaN, true),
            Triple(nonCanonicalNaN, Double.NaN, true),
            Triple(Double.NaN, 1.0, false),
            Triple(1.0, Double.NaN, false),
            Triple(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true),
            Triple(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, false),
            Triple(Double.MIN_VALUE, 0.0, false),
        )
        for ((a, b, expected) in cases) {
            assertEquals(expected, componentEquals(a, b), "componentEquals($a, $b)")
        }
    }

    @Test
    fun `componentHash gives zero and negative zero the same hash`() {
        assertEquals(componentHash(0.0), componentHash(-0.0))
    }

    @Test
    fun `componentHash gives every NaN the hash of the canonical NaN`() {
        assertEquals(Double.NaN.hashCode(), componentHash(Double.NaN))
        assertEquals(Double.NaN.hashCode(), componentHash(nonCanonicalNaN))
    }

    @Test
    fun `componentHash of a finite non-zero value is its own hashCode`() {
        for (v in listOf(1.0, -2.5, 1e300, Double.MIN_VALUE)) {
            assertEquals(v.hashCode(), componentHash(v), "componentHash($v)")
        }
    }

    @Test
    fun `componentHash separates one and two`() {
        assertNotEquals(componentHash(1.0), componentHash(2.0))
    }
}
