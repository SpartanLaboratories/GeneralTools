package com.spartanlabs.testing.component.generaltools

import com.spartanlabs.generaltools.capitalizeEveryWord
import com.spartanlabs.generaltools.evaluateList
import com.spartanlabs.generaltools.forEvery
import com.spartanlabs.generaltools.forEveryIndexed
import com.spartanlabs.generaltools.onException
import com.spartanlabs.generaltools.profile
import com.spartanlabs.generaltools.trimIfLongerThan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for the platform-neutral helpers in Misc.kt. They take
 * lambdas as their only collaborators, so there is nothing to mock.
 */
class MiscTest {
    @Test
    fun `profile reports the action name and a duration`() {
        val result = profile("sample action") { }
        assertTrue(result.startsWith("sample action took"))
        assertTrue(result.endsWith("ms"))
    }

    @Test
    fun `onException transforms a matching exception`() {
        val result = runCatching<Int> { throw IllegalStateException("boom") }
            .onException(IllegalStateException::class) { -1 }
        assertEquals(Result.success(-1), result)
    }

    @Test
    fun `onException rethrows an unmatched exception`() {
        val result = runCatching<Int> { throw IllegalStateException("boom") }
            .onException(NumberFormatException::class) { -1 }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `forEveryIndexed provides the index and item for each element`() {
        val collected = mutableListOf<Pair<Int, String>>()
        listOf("a", "b", "c") forEveryIndexed { index, item -> collected.add(index to item) }
        assertEquals(listOf(0 to "a", 1 to "b", 2 to "c"), collected)
    }

    @Test
    fun `forEvery visits every item`() {
        val collected = mutableListOf<Int>()
        listOf(1, 2, 3) forEvery { collected.add(it) }
        assertEquals(listOf(1, 2, 3), collected)
    }

    @Test
    fun `trimIfLongerThan trims strings longer than the given length`() {
        assertEquals("hello", "hello world" trimIfLongerThan 5)
    }

    @Test
    fun `trimIfLongerThan leaves shorter strings unchanged`() {
        assertEquals("hi", "hi" trimIfLongerThan 5)
    }

    @Test
    fun `trimIfLongerThan throws for a negative length`() {
        assertFailsWith<IndexOutOfBoundsException> { "hello".trimIfLongerThan(-1) }
    }

    @Test
    fun `trimIfLongerThan throws for a negative length on an empty string`() {
        assertFailsWith<IndexOutOfBoundsException> { "".trimIfLongerThan(-1) }
    }

    @Test
    fun `trimIfLongerThan to zero yields the empty string`() {
        assertEquals("", "hello".trimIfLongerThan(0))
    }

    @Test
    fun `evaluateList returns true when a predicate matches`() {
        assertTrue(evaluateList(listOf(1, 2, 3)) { it == 2 })
    }

    @Test
    fun `evaluateList returns false when no predicate matches`() {
        assertFalse(evaluateList(listOf(1, 2, 3)) { it == 5 })
    }

    @Test
    fun `evaluateList returns false for an empty list`() {
        var calls = 0
        assertFalse(evaluateList(emptyList<Int>()) { calls++; true })
        assertEquals(0, calls)
    }

    @Test
    fun `evaluateList stops at the first match`() {
        var calls = 0
        assertTrue(evaluateList(listOf(1, 2, 3)) { calls++; it == 2 })
        assertEquals(2, calls)
    }

    @Test
    fun `capitalizeEveryWord capitalizes each word`() {
        assertEquals("Hello World Foo", "hello world foo".capitalizeEveryWord())
    }

    @Test
    fun `capitalizeEveryWord keeps commas that are part of the input`() {
        assertEquals("Hello, World, Foo", "hello, world, foo".capitalizeEveryWord())
        assertEquals("One,two Three", "one,two three".capitalizeEveryWord())
    }
}
