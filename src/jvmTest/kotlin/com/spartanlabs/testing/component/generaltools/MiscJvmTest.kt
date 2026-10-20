package com.spartanlabs.testing.component.generaltools

import com.spartanlabs.generaltools.capitalizeEveryWord
import com.spartanlabs.generaltools.evaluateList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the helpers whose implementations are still JVM-only
 * (Predicate and the deprecated capitalize()). Interim class: each test moves into the common MiscTest when its
 * subject becomes common, and this class is then deleted.
 */
class MiscJvmTest {
    @Test
    fun `capitalizeEveryWord capitalizes each word`() {
        assertEquals("Hello World Foo", "hello world foo".capitalizeEveryWord())
    }

    @Test
    fun `evaluateList returns true when a predicate matches`() {
        assertTrue(evaluateList(listOf(1, 2, 3)) { it == 2 })
    }

    @Test
    fun `evaluateList returns false when no predicate matches`() {
        assertFalse(evaluateList(listOf(1, 2, 3)) { it == 5 })
    }
}
