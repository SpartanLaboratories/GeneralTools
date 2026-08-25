package com.spartanlabs.generaltools

import org.slf4j.LoggerFactory
import java.io.File
import java.net.MalformedURLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiscTest {
    private val log = LoggerFactory.getLogger(MiscTest::class.java)

    @Test
    fun `capitalizeEveryWord capitalizes each word`() {
        log.info("Running capitalizeEveryWord test")
        assertEquals("Hello World Foo", "hello world foo".capitalizeEveryWord())
    }

    @Test
    fun `asURL parses a valid URL`() {
        log.info("Running asURL valid-input test")
        val url = "https://www.example.com".asURL()
        assertEquals("https://www.example.com", url.toString())
    }

    @Test
    fun `asURL throws on an invalid URL`() {
        log.info("Running asURL invalid-input test")
        assertFailsWith<MalformedURLException> { "not a valid url".asURL() }
    }

    @Test
    fun `read returns each line of the file`() {
        log.info("Running read test")
        val tempFile = File.createTempFile("misc-test", ".txt")
        tempFile.deleteOnExit()
        tempFile.writeText("line one\nline two\nline three")

        val lines = read(tempFile.absolutePath)

        assertEquals(listOf("line one", "line two", "line three"), lines)
    }

    @Test
    fun `evaluateList returns true when a predicate matches`() {
        log.info("Running evaluateList true-case test")
        assertTrue(evaluateList(listOf(1, 2, 3)) { it == 2 })
    }

    @Test
    fun `evaluateList returns false when no predicate matches`() {
        log.info("Running evaluateList false-case test")
        assertFalse(evaluateList(listOf(1, 2, 3)) { it == 5 })
    }

    @Test
    fun `profile reports the action name and a duration`() {
        log.info("Running profile test")
        val result = profile("sample action") { Thread.sleep(1) }
        assertTrue(result.startsWith("sample action took"))
        assertTrue(result.endsWith("ms"))
    }

    @Test
    fun `Logger time logs the profiled action without throwing`() {
        log.info("Running Logger#time test")
        log.time("sample action") { Thread.sleep(1) }
    }

    @Test
    fun `onException transforms a matching exception`() {
        log.info("Running onException matching-type test")
        val result = runCatching<Int> { throw IllegalStateException("boom") }
            .onException(IllegalStateException::class) { -1 }
        assertEquals(Result.success(-1), result)
    }

    @Test
    fun `onException rethrows an unmatched exception`() {
        log.info("Running onException unmatched-type test")
        val result = runCatching<Int> { throw IllegalStateException("boom") }
            .onException(NumberFormatException::class) { -1 }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `forEveryIndexed provides the index and item for each element`() {
        log.info("Running forEveryIndexed test")
        val collected = mutableListOf<Pair<Int, String>>()
        listOf("a", "b", "c") forEveryIndexed { index, item -> collected.add(index to item) }
        assertEquals(listOf(0 to "a", 1 to "b", 2 to "c"), collected)
    }

    @Test
    fun `forEvery visits every item`() {
        log.info("Running forEvery test")
        val collected = mutableListOf<Int>()
        listOf(1, 2, 3) forEvery { collected.add(it) }
        assertEquals(listOf(1, 2, 3), collected)
    }

    @Test
    fun `trimIfLongerThan trims strings longer than the given length`() {
        log.info("Running trimIfLongerThan trims-case test")
        assertEquals("hello", "hello world" trimIfLongerThan 5)
    }

    @Test
    fun `trimIfLongerThan leaves shorter strings unchanged`() {
        log.info("Running trimIfLongerThan unchanged-case test")
        assertEquals("hi", "hi" trimIfLongerThan 5)
    }
}
