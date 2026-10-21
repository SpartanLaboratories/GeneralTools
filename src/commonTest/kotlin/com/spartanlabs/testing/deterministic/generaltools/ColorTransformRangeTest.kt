package com.spartanlabs.testing.deterministic.generaltools

import com.spartanlabs.generaltools.Color
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Level 4a (Deterministic Logic / Input-to-Output Mapping) coverage that no factory or transform of [Color] can
 * produce an out-of-range channel. Such a channel used to be stored silently and now makes the constructor throw, on
 * every target, including Kotlin/JS, where `lerp` does its arithmetic in double precision.
 */
class ColorTransformRangeTest {
    private val partners = listOf(0, 1, 85, 127, 128, 170, 254, 255)

    private fun assertInRange(color: Color, context: String) {
        listOf(color.red, color.green, color.blue, color.alpha).forEach { channel ->
            assertTrue(channel in 0..255, "$context gave $color")
        }
    }

    @Test
    fun `every transform of an in-range colour yields an in-range colour`() {
        for (a in 0..255) {
            for (b in partners) {
                val color = Color(a, b, 255 - a, 255 - b)
                val target = Color(b, a, b, a)
                listOf(0.0, 0.35, 1.0).forEach { fraction ->
                    assertInRange(color.lightened(fraction), "$color.lightened($fraction)")
                    assertInRange(color.darkened(fraction), "$color.darkened($fraction)")
                }
                assertInRange(color.inverted(), "$color.inverted()")
                assertInRange(color.grayscale(), "$color.grayscale()")
                assertInRange(color.withAlpha(a), "$color.withAlpha($a)")
                listOf(0f, 0.21f, 0.5f, 1f).forEach { fraction ->
                    assertInRange(color.lerp(target, fraction), "$color.lerp($target, $fraction)")
                }
                assertInRange(Color.fromPackedInt(color.toPackedInt()), "Color.fromPackedInt($color.toPackedInt())")
            }
        }
        listOf(-1f, 0f, 0.5f, 1f, 2f, Float.NaN, Float.POSITIVE_INFINITY).forEach { value ->
            assertInRange(Color.fromNormalized(value, value, value, value), "Color.fromNormalized($value)")
        }
    }
}
