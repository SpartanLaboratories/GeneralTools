package com.spartanlabs.testing.component.generaltools

import com.spartanlabs.generaltools.asURL
import java.net.MalformedURLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Level 2 (Isolated Component Behaviour) coverage for `asURL`. It only parses the text and opens no connection,
 * so there is nothing to mock.
 */
class AsUrlTest {
    @Test
    fun `asURL parses a valid URL`() {
        val url = "https://www.example.com".asURL()
        assertEquals("https://www.example.com", url.toString())
    }

    @Test
    fun `asURL throws on an invalid URL`() {
        assertFailsWith<MalformedURLException> { "not a valid url".asURL() }
    }
}
