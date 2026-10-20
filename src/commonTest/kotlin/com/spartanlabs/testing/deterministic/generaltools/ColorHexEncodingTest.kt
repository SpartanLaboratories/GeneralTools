package com.spartanlabs.testing.deterministic.generaltools

import com.spartanlabs.generaltools.Color
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Level 4a (Deterministic Logic / Input-to-Output Mapping) coverage for Color.toHex: every channel value maps to
 * exactly two upper-case hex digits, identically on every target.
 */
class ColorHexEncodingTest {
    // Oracle that shares no code with toHex: a digit table indexed by the two nibbles.
    private val digits = "0123456789ABCDEF"
    private fun pair(channel: Int): String = "${digits[channel / 16]}${digits[channel % 16]}"

    @Test
    fun `toHex renders every channel value as two upper-case hex digits`() {
        val expected = (0..255).map { c -> "#" + pair(c) + pair(255 - c) + pair((c * 7) % 256) }
        val actual = (0..255).map { c -> Color(c, 255 - c, (c * 7) % 256).toHex() }
        assertEquals(expected, actual)
    }

    @Test
    fun `toHex with alpha renders every alpha value first`() {
        val expected = (0..255).map { c ->
            "#" + pair((c * 13) % 256) + pair(255 - c) + pair(c) + pair((c * 7) % 256)
        }
        val actual = (0..255).map { c ->
            Color(255 - c, c, (c * 7) % 256, (c * 13) % 256).toHex(includeAlpha = true)
        }
        assertEquals(expected, actual)
    }
}
