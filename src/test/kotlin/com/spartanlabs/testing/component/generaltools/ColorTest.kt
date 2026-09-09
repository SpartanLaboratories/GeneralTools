package com.spartanlabs.testing.component.generaltools

import com.spartanlabs.generaltools.Color
import org.junit.jupiter.api.Tag
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Level 2 (Isolated Component Behaviour) coverage for [Color]. `Color` is a pure
 * value type, so there is nothing to mock. Expected values assume the post-fix
 * implementation: `roundToInt` rounding, ARGB hex byte order, and non-finite
 * `fraction` guards.
 */
@Tag("component")
class ColorTest {
    private val log = LoggerFactory.getLogger(ColorTest::class.java)

    // --- normalized() ---

    @Test
    fun `normalized maps channels 0-255 to 0f-1f`() {
        log.info("Running Color#normalized mapping test")
        assertContentEquals(floatArrayOf(1f, 0f, 0f, 1f), Color(255, 0, 0, 255).normalized())
    }

    @Test
    fun `normalized converts a partial channel`() {
        log.info("Running Color#normalized partial-channel test")
        assertEquals(0.50196f, Color(0, 0, 0, 128).normalized()[3], 1e-4f)
    }

    // --- lightened(fraction) ---

    @Test
    fun `lightened default fraction moves toward white`() {
        log.info("Running Color#lightened default-fraction test")
        assertEquals(Color(89, 89, 89), Color(0, 0, 0).lightened())
    }

    @Test
    fun `lightened rounds half up`() {
        log.info("Running Color#lightened round-half-up test")
        assertEquals(Color(128, 128, 128), Color(0, 0, 0).lightened(0.5))
    }

    @Test
    fun `lightened preserves alpha`() {
        log.info("Running Color#lightened alpha-preserved test")
        assertEquals(10, Color(0, 0, 0, 10).lightened().alpha)
    }

    @Test
    fun `lightened with fraction 1 yields white RGB and original alpha`() {
        log.info("Running Color#lightened fraction-1 test")
        assertEquals(Color(255, 255, 255, 50), Color(1, 2, 3, 50).lightened(1.0))
    }

    @Test
    fun `lightened with fraction 0 yields an equal colour`() {
        log.info("Running Color#lightened fraction-0 test")
        assertEquals(Color(10, 20, 30, 40), Color(10, 20, 30, 40).lightened(0.0))
    }

    @Test
    fun `lightened clamps fraction above 1`() {
        log.info("Running Color#lightened clamp-high test")
        assertEquals(Color(10, 20, 30).lightened(1.0), Color(10, 20, 30).lightened(5.0))
    }

    @Test
    fun `lightened clamps fraction below 0`() {
        log.info("Running Color#lightened clamp-low test")
        assertEquals(Color(10, 20, 30), Color(10, 20, 30).lightened(-1.0))
    }

    @Test
    fun `lightened treats NaN as identity`() {
        log.info("Running Color#lightened NaN-guard test")
        assertEquals(Color(10, 20, 30, 40), Color(10, 20, 30, 40).lightened(Double.NaN))
    }

    @Test
    fun `lightened treats infinities as identity`() {
        log.info("Running Color#lightened infinity-guard test")
        assertEquals(Color(10, 20, 30, 40), Color(10, 20, 30, 40).lightened(Double.POSITIVE_INFINITY))
        assertEquals(Color(10, 20, 30, 40), Color(10, 20, 30, 40).lightened(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun `lightened output channels stay in range`() {
        log.info("Running Color#lightened output-range test")
        val result = Color(250, 5, 128).lightened(1.0)
        assertTrue(result.red in 0..255 && result.green in 0..255 && result.blue in 0..255 && result.alpha in 0..255)
    }

    @Test
    fun `lightened does not mutate the receiver`() {
        log.info("Running Color#lightened no-mutation test")
        val c = Color(0, 0, 0)
        c.lightened()
        assertEquals(Color(0, 0, 0), c)
    }

    // --- darkened(fraction) ---

    @Test
    fun `darkened default fraction moves toward black`() {
        log.info("Running Color#darkened default-fraction test")
        assertEquals(Color(130, 130, 130), Color(200, 200, 200).darkened())
    }

    @Test
    fun `darkened rounds half up`() {
        log.info("Running Color#darkened round-half-up test")
        assertEquals(Color(128, 128, 128), Color(255, 255, 255).darkened(0.5))
    }

    @Test
    fun `darkened preserves alpha`() {
        log.info("Running Color#darkened alpha-preserved test")
        assertEquals(10, Color(255, 255, 255, 10).darkened().alpha)
    }

    @Test
    fun `darkened with fraction 1 yields black RGB and original alpha`() {
        log.info("Running Color#darkened fraction-1 test")
        assertEquals(Color(0, 0, 0, 50), Color(1, 2, 3, 50).darkened(1.0))
    }

    @Test
    fun `darkened with fraction 0 yields an equal colour`() {
        log.info("Running Color#darkened fraction-0 test")
        assertEquals(Color(10, 20, 30), Color(10, 20, 30).darkened(0.0))
    }

    @Test
    fun `darkened clamps out-of-range fraction`() {
        log.info("Running Color#darkened clamp test")
        assertEquals(Color(10, 20, 30).darkened(1.0), Color(10, 20, 30).darkened(5.0))
        assertEquals(Color(10, 20, 30), Color(10, 20, 30).darkened(-1.0))
    }

    @Test
    fun `darkened treats NaN and infinities as identity`() {
        log.info("Running Color#darkened non-finite-guard test")
        assertEquals(Color(10, 20, 30, 40), Color(10, 20, 30, 40).darkened(Double.NaN))
        assertEquals(Color(10, 20, 30, 40), Color(10, 20, 30, 40).darkened(Double.POSITIVE_INFINITY))
        assertEquals(Color(10, 20, 30, 40), Color(10, 20, 30, 40).darkened(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun `darkened output channels stay in range`() {
        log.info("Running Color#darkened output-range test")
        val result = Color(250, 5, 128).darkened(1.0)
        assertTrue(result.red in 0..255 && result.green in 0..255 && result.blue in 0..255 && result.alpha in 0..255)
    }

    @Test
    fun `darkened does not mutate the receiver`() {
        log.info("Running Color#darkened no-mutation test")
        val c = Color(200, 200, 200)
        c.darkened()
        assertEquals(Color(200, 200, 200), c)
    }

    // --- withAlpha(newAlpha) ---

    @Test
    fun `withAlpha replaces alpha and keeps RGB`() {
        log.info("Running Color#withAlpha replace test")
        assertEquals(Color(1, 2, 3, 200), Color(1, 2, 3, 4).withAlpha(200))
    }

    @Test
    fun `withAlpha clamps above 255`() {
        log.info("Running Color#withAlpha clamp-high test")
        assertEquals(255, Color(1, 2, 3).withAlpha(999).alpha)
    }

    @Test
    fun `withAlpha clamps below 0`() {
        log.info("Running Color#withAlpha clamp-low test")
        assertEquals(0, Color(1, 2, 3).withAlpha(-5).alpha)
    }

    // --- inverted() ---

    @Test
    fun `inverted inverts RGB and keeps alpha`() {
        log.info("Running Color#inverted test")
        assertEquals(Color(255, 255, 255, 10), Color(0, 0, 0, 10).inverted())
    }

    @Test
    fun `inverted is an involution for in-range colours`() {
        log.info("Running Color#inverted involution test")
        assertEquals(Color(12, 34, 56, 78), Color(12, 34, 56, 78).inverted().inverted())
    }

    // --- grayscale() ---

    @Test
    fun `grayscale applies BT601 weight to pure red`() {
        log.info("Running Color#grayscale red test")
        assertEquals(Color(76, 76, 76), Color(255, 0, 0).grayscale())
    }

    @Test
    fun `grayscale applies BT601 weight to pure green`() {
        log.info("Running Color#grayscale green test")
        assertEquals(Color(150, 150, 150), Color(0, 255, 0).grayscale())
    }

    @Test
    fun `grayscale leaves white unchanged and preserves alpha`() {
        log.info("Running Color#grayscale white test")
        assertEquals(Color(255, 255, 255, 42), Color(255, 255, 255, 42).grayscale())
    }

    // --- lerp(target, fraction) ---

    private val lerpA = Color(10, 20, 30, 40)
    private val lerpB = Color(200, 150, 100, 50)

    @Test
    fun `lerp with fraction 0 yields this`() {
        log.info("Running Color#lerp fraction-0 test")
        assertEquals(lerpA, lerpA.lerp(lerpB, 0f))
    }

    @Test
    fun `lerp with fraction 1 yields target`() {
        log.info("Running Color#lerp fraction-1 test")
        assertEquals(lerpB, lerpA.lerp(lerpB, 1f))
    }

    @Test
    fun `lerp midpoint black to white`() {
        log.info("Running Color#lerp midpoint test")
        assertEquals(Color(128, 128, 128, 255), Color(0, 0, 0).lerp(Color(255, 255, 255), 0.5f))
    }

    @Test
    fun `lerp interpolates alpha too`() {
        log.info("Running Color#lerp alpha test")
        assertEquals(128, Color(0, 0, 0, 0).lerp(Color(0, 0, 0, 255), 0.5f).alpha)
    }

    @Test
    fun `lerp clamps fraction`() {
        log.info("Running Color#lerp clamp test")
        assertEquals(lerpA.lerp(lerpB, 1f), lerpA.lerp(lerpB, 2f))
        assertEquals(lerpA, lerpA.lerp(lerpB, -1f))
    }

    @Test
    fun `lerp treats NaN as identity`() {
        log.info("Running Color#lerp NaN-guard test")
        assertEquals(Color(1, 2, 3, 4), Color(1, 2, 3, 4).lerp(Color(5, 6, 7, 8), Float.NaN))
    }

    @Test
    fun `lerp treats infinity as identity not target`() {
        log.info("Running Color#lerp infinity-guard test")
        assertEquals(Color(1, 2, 3, 4), Color(1, 2, 3, 4).lerp(Color(5, 6, 7, 8), Float.POSITIVE_INFINITY))
        assertEquals(Color(1, 2, 3, 4), Color(1, 2, 3, 4).lerp(Color(5, 6, 7, 8), Float.NEGATIVE_INFINITY))
    }

    // --- luminance() ---

    @Test
    fun `luminance of white is 1`() {
        log.info("Running Color#luminance white test")
        assertEquals(1.0f, Color.WHITE.luminance(), 1e-4f)
    }

    @Test
    fun `luminance of black is 0`() {
        log.info("Running Color#luminance black test")
        assertEquals(0.0f, Color.BLACK.luminance())
    }

    @Test
    fun `luminance of pure red uses its weight`() {
        log.info("Running Color#luminance red test")
        assertEquals(0.299f, Color(255, 0, 0).luminance(), 1e-4f)
    }

    @Test
    fun `luminance ignores alpha`() {
        log.info("Running Color#luminance alpha-ignored test")
        assertEquals(Color(255, 0, 0, 255).luminance(), Color(255, 0, 0, 0).luminance())
    }

    // --- toPackedInt() / fromPackedInt(argb) ---

    @Test
    fun `toPackedInt of transparent black is 0`() {
        log.info("Running Color#toPackedInt zero test")
        assertEquals(0, Color(0, 0, 0, 0).toPackedInt())
    }

    @Test
    fun `toPackedInt of opaque white is -1`() {
        log.info("Running Color#toPackedInt opaque-white test")
        assertEquals(-1, Color(255, 255, 255, 255).toPackedInt())
    }

    @Test
    fun `toPackedInt places channels in ARGB order`() {
        log.info("Running Color#toPackedInt channel-placement test")
        assertEquals((4 shl 24) or (1 shl 16) or (2 shl 8) or 3, Color(1, 2, 3, 4).toPackedInt())
    }

    @Test
    fun `fromPackedInt inverts toPackedInt`() {
        log.info("Running Color#fromPackedInt round-trip test")
        assertEquals(Color(1, 2, 3, 4), Color.fromPackedInt(Color(1, 2, 3, 4).toPackedInt()))
    }

    @Test
    fun `fromPackedInt of -1 is opaque white`() {
        log.info("Running Color#fromPackedInt -1 test")
        assertEquals(Color(255, 255, 255, 255), Color.fromPackedInt(-1))
    }

    // --- toHex(includeAlpha) ---

    @Test
    fun `toHex emits upper-case RGB`() {
        log.info("Running Color#toHex RGB test")
        assertEquals("#FF5733", Color(255, 87, 51).toHex())
    }

    @Test
    fun `toHex zero-pads`() {
        log.info("Running Color#toHex zero-pad test")
        assertEquals("#010203", Color(1, 2, 3).toHex())
    }

    @Test
    fun `toHex alpha form is AARRGGBB`() {
        log.info("Running Color#toHex ARGB test")
        assertEquals("#80FF5733", Color(255, 87, 51, 128).toHex(includeAlpha = true))
    }

    @Test
    fun `toHex round-trips through fromHex with alpha`() {
        log.info("Running Color#toHex alpha round-trip test")
        val c = Color(255, 87, 51, 128)
        assertEquals(c, Color.fromHex(c.toHex(includeAlpha = true)))
    }

    @Test
    fun `toHex round-trips through fromHex without alpha`() {
        log.info("Running Color#toHex round-trip test")
        assertEquals(Color(255, 87, 51), Color.fromHex(Color(255, 87, 51).toHex()))
    }

    // --- companion constants ---

    @Test
    fun `companion constants have expected channels`() {
        log.info("Running Color companion-constants test")
        assertEquals(Color(255, 255, 255, 255), Color.WHITE)
        assertEquals(Color(0, 0, 0, 0), Color.TRANSPARENT)
        assertEquals(Color(255, 0, 0, 255), Color.RED)
    }

    // --- primary constructor contract ---

    @Test
    fun `primary constructor does not clamp out-of-range channels`() {
        log.info("Running Color constructor no-clamp test")
        assertEquals(300, Color(300, 0, 0).red)
    }

    // --- Color.of(red, green, blue, alpha = 255) ---

    @Test
    fun `of passes through in-range channels`() {
        log.info("Running Color#of in-range test")
        assertEquals(Color(1, 2, 3, 4), Color.of(1, 2, 3, 4))
    }

    @Test
    fun `of defaults alpha to opaque`() {
        log.info("Running Color#of default-alpha test")
        assertEquals(Color(1, 2, 3, 255), Color.of(1, 2, 3))
    }

    @Test
    fun `of clamps channels high and low`() {
        log.info("Running Color#of clamp test")
        assertEquals(Color(255, 0, 128, 255), Color.of(300, -5, 128, 999))
    }

    // --- Color.fromNormalized(r, g, b, a = 1f) ---

    @Test
    fun `fromNormalized defaults alpha to opaque`() {
        log.info("Running Color#fromNormalized default-alpha test")
        assertEquals(Color(255, 0, 0, 255), Color.fromNormalized(1f, 0f, 0f))
    }

    @Test
    fun `fromNormalized honours an explicit zero alpha`() {
        log.info("Running Color#fromNormalized zero-alpha test")
        assertEquals(Color(0, 0, 0, 0), Color.fromNormalized(0f, 0f, 0f, 0f))
    }

    @Test
    fun `fromNormalized clamps out-of-range floats`() {
        log.info("Running Color#fromNormalized clamp test")
        assertEquals(Color(255, 0, 128, 255), Color.fromNormalized(2f, -1f, 0.5f))
    }

    @Test
    fun `fromNormalized treats non-finite channels as zero`() {
        log.info("Running Color#fromNormalized non-finite-guard test")
        assertEquals(Color(0, 0, 0, 255), Color.fromNormalized(Float.NaN, 0f, 0f))
        assertEquals(Color(0, 0, 0, 255), Color.fromNormalized(Float.POSITIVE_INFINITY, 0f, 0f))
        assertEquals(Color(0, 0, 0, 255), Color.fromNormalized(Float.NEGATIVE_INFINITY, 0f, 0f))
    }

    // --- Color.fromHex(hex) ---

    @Test
    fun `fromHex parses RRGGBB`() {
        log.info("Running Color#fromHex RRGGBB test")
        assertEquals(Color(255, 87, 51), Color.fromHex("#FF5733"))
    }

    @Test
    fun `fromHex expands RGB shorthand`() {
        log.info("Running Color#fromHex shorthand test")
        assertEquals(Color(255, 85, 51), Color.fromHex("F53"))
    }

    @Test
    fun `fromHex parses AARRGGBB with alpha first`() {
        log.info("Running Color#fromHex AARRGGBB test")
        assertEquals(Color(255, 87, 51, 128), Color.fromHex("#80FF5733"))
    }

    @Test
    fun `fromHex accepts a 0x prefix`() {
        log.info("Running Color#fromHex 0x-prefix test")
        assertEquals(Color(255, 87, 51), Color.fromHex("0xFF5733"))
    }

    @Test
    fun `fromHex accepts no prefix`() {
        log.info("Running Color#fromHex no-prefix test")
        assertEquals(Color(255, 87, 51), Color.fromHex("FF5733"))
    }

    @Test
    fun `fromHex rejects an invalid length`() {
        log.info("Running Color#fromHex invalid-length test")
        assertFailsWith<IllegalArgumentException> { Color.fromHex("#FFFF") }
    }

    @Test
    fun `fromHex rejects non-hex digits`() {
        log.info("Running Color#fromHex non-hex-digit test")
        assertFailsWith<IllegalArgumentException> { Color.fromHex("#GGGGGG") }
    }
}
