package com.spartanlabs.generaltools

import kotlin.math.roundToInt

/**
 * A Red-Green-Blue-Alpha colour, each channel `0..255`.
 *
 * Immutable value type. Channels are a documented `0..255` contract — the
 * primary constructor does not validate them; use [Color.of] to clamp
 * untrusted input. Transform methods ([lightened], [darkened], [lerp],
 * [inverted], [grayscale], [withAlpha]) return a new instance.
 *
 * Hex and packed-int forms use ARGB byte order: [toHex] with alpha emits
 * `#AARRGGBB` and round-trips through [fromHex]; [toPackedInt] /
 * [fromPackedInt] likewise.
 *
 * @property red the red channel, `0..255`, not validated by the constructor.
 * @property green the green channel, `0..255`, not validated by the constructor.
 * @property blue the blue channel, `0..255`, not validated by the constructor.
 * @property alpha the alpha channel, `0..255`, not validated by the constructor;
 *   defaults to `255` (opaque).
 */
data class Color(val red: Int, val green: Int, val blue: Int, val alpha: Int = 255) {

    /**
     * `[r, g, b, a]` with every channel mapped from `0..255` to `0f..1f`.
     *
     * @return a new 4-element `[r, g, b, a]` array, each channel `/ 255f`.
     */
    fun normalized(): FloatArray =
        floatArrayOf(red / 255f, green / 255f, blue / 255f, alpha / 255f)


    // --- Color Transformations & Variations ---

    /**
     * This colour moved [fraction] of the way toward white, [alpha] untouched.
     *
     * @param fraction how far toward white to move, clamped to `0.0..1.0`
     *   (default `0.35`); a non-finite value (`NaN`, `±Infinity`) is treated as
     *   `0.0` — this colour unchanged.
     * @return a new [Color]; the receiver is not modified.
     */
    fun lightened(fraction: Double = 0.35): Color {
        // A non-finite fraction would propagate through the channel maths and,
        // after coerceIn(0, 255), collapse every channel to 0 (black).
        val f = fraction.takeIf { it.isFinite() }?.coerceIn(0.0, 1.0) ?: 0.0
        fun towardWhite(channel: Int) =
            (channel + (255 - channel) * f).roundToInt().coerceIn(0, 255)
        return Color(towardWhite(red), towardWhite(green), towardWhite(blue), alpha)
    }

    /**
     * This colour moved [fraction] of the way toward black, [alpha] untouched.
     *
     * @param fraction how far toward black to move, clamped to `0.0..1.0`
     *   (default `0.35`); a non-finite value (`NaN`, `±Infinity`) is treated as
     *   `0.0` — this colour unchanged.
     * @return a new [Color]; the receiver is not modified.
     */
    fun darkened(fraction: Double = 0.35): Color {
        // A non-finite fraction would propagate through the channel maths and,
        // after coerceIn(0, 255), collapse every channel to 0 (black).
        val f = fraction.takeIf { it.isFinite() }?.coerceIn(0.0, 1.0) ?: 0.0
        fun towardBlack(channel: Int) =
            (channel * (1.0 - f)).roundToInt().coerceIn(0, 255)
        return Color(towardBlack(red), towardBlack(green), towardBlack(blue), alpha)
    }

    /**
     * Returns a copy of this colour with a modified alpha channel.
     *
     * @param newAlpha the replacement alpha, coerced into `0..255`.
     * @return a copy with red/green/blue unchanged.
     */
    fun withAlpha(newAlpha: Int): Color = copy(alpha = newAlpha.coerceIn(0, 255))

    /**
     * Inverts the RGB components while keeping [alpha] untouched.
     *
     * @return a new [Color] with `255 - ` each RGB channel; alpha unchanged.
     */
    fun inverted(): Color = Color(255 - red, 255 - green, 255 - blue, alpha)

    /**
     * Converts the colour to grayscale using standard ITU-R BT.601 luma weights.
     *
     * @return a new [Color] with `R = G = B` set to the BT.601 luma, alpha
     *   preserved.
     */
    fun grayscale(): Color {
        val gray = (0.299 * red + 0.587 * green + 0.114 * blue).roundToInt().coerceIn(0, 255)
        return Color(gray, gray, gray, alpha)
    }

    /**
     * Linearly interpolates (blends) between this colour and [target] by
     * [fraction], alpha included.
     *
     * @param target the colour to blend toward; not modified.
     * @param fraction blend factor, clamped to `0f..1f` (`0f` → this colour,
     *   `1f` → [target]); a non-finite value (`NaN`, `±Infinity`) is treated as
     *   `0f` — this colour unchanged, *not* clamped to [target].
     * @return a new [Color]; neither operand is modified.
     */
    fun lerp(target: Color, fraction: Float): Color {
        // A non-finite fraction would propagate through the channel maths and,
        // after coerceIn(0, 255), collapse every channel to 0; takeIf keeps the
        // non-finite case as "identity" rather than letting coerceIn pass NaN.
        val f = fraction.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
        fun interpolate(start: Int, end: Int) =
            (start + (end - start) * f).roundToInt().coerceIn(0, 255)
        return Color(
            interpolate(red, target.red),
            interpolate(green, target.green),
            interpolate(blue, target.blue),
            interpolate(alpha, target.alpha)
        )
    }

    /**
     * Perceived luma (BT.601 weights, not gamma-corrected — not WCAG relative
     * luminance).
     *
     * @return a value in `0.0f..1.0f`; alpha is ignored.
     */
    fun luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f

    /**
     * Encodes the colour channels into a single 32-bit ARGB `Int`.
     *
     * @return the packed value: alpha in bits 31..24, red 23..16, green 15..8,
     *   blue 7..0. The result is a signed `Int`, so opaque white packs to `-1`.
     *   Channels are assumed to be in `0..255` — the caller's responsibility.
     */
    fun toPackedInt(): Int = (alpha shl 24) or (red shl 16) or (green shl 8) or blue

    /**
     * Upper-case, `#`-prefixed hex.
     *
     * @param includeAlpha when true, emits `#AARRGGBB` (alpha first — the same
     *   ARGB byte order as [toPackedInt] and [fromHex], so
     *   `Color.fromHex(toHex(includeAlpha = true))` round-trips); otherwise
     *   `#RRGGBB`.
     * @return the hex string.
     */
    fun toHex(includeAlpha: Boolean = false): String {
        return if (includeAlpha) {
            String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
        } else {
            String.format("#%02X%02X%02X", red, green, blue)
        }
    }

    companion object {
        val WHITE = Color(255, 255, 255)
        val BLACK = Color(0, 0, 0)
        val RED = Color(255, 0, 0)
        val GREEN = Color(0, 255, 0)
        val BLUE = Color(0, 0, 255)
        val YELLOW = Color(255, 255, 0)
        val CYAN = Color(0, 255, 255)
        val MAGENTA = Color(255, 0, 255)
        val ORANGE = Color(255, 165, 0)
        val PURPLE = Color(128, 0, 128)
        val GRAY = Color(128, 128, 128)
        val LIGHT_GRAY = Color(192, 192, 192)
        val DARK_GRAY = Color(64, 64, 64)
        /** Fully transparent - a panel/label with no visible background of its own. */
        val TRANSPARENT = Color(0, 0, 0, 0)

        /**
         * Builds a [Color] with every channel clamped into `0..255`. Prefer this
         * over the primary constructor for values that may be out of range
         * (parsed input, arithmetic results) — the constructor performs no
         * validation.
         *
         * @param red the red channel, coerced into `0..255`.
         * @param green the green channel, coerced into `0..255`.
         * @param blue the blue channel, coerced into `0..255`.
         * @param alpha the alpha channel, coerced into `0..255`; defaults to
         *   `255` (opaque).
         * @return a new [Color] with each channel coerced into `0..255`.
         */
        fun of(red: Int, green: Int, blue: Int, alpha: Int = 255): Color =
            Color(
                red.coerceIn(0, 255),
                green.coerceIn(0, 255),
                blue.coerceIn(0, 255),
                alpha.coerceIn(0, 255),
            )

        /**
         * Constructs a [Color] from float values in the range `0f..1f`.
         *
         * @param r the red channel, clamped to `0f..1f`; a non-finite value is
         *   treated as `0f`.
         * @param g the green channel, clamped to `0f..1f`; a non-finite value is
         *   treated as `0f`.
         * @param b the blue channel, clamped to `0f..1f`; a non-finite value is
         *   treated as `0f`.
         * @param a the alpha channel, clamped to `0f..1f`; a non-finite value is
         *   treated as `0f`; defaults to `1f` (opaque).
         * @return a new [Color].
         */
        fun fromNormalized(r: Float, g: Float, b: Float, a: Float = 1f): Color {
            // A non-finite channel would survive coerceIn (every comparison with
            // NaN is false) and then roundToInt to 0; takeIf maps it to 0f up
            // front so the clamp behaves.
            fun channel(value: Float) =
                ((value.takeIf { it.isFinite() } ?: 0f).coerceIn(0f, 1f) * 255).roundToInt()
            return Color(channel(r), channel(g), channel(b), channel(a))
        }

        /**
         * Decodes a 32-bit ARGB packed `Int` into a [Color]. The exact inverse
         * of [toPackedInt].
         *
         * @param argb an ARGB-packed value, alpha in the high byte; negative
         *   values are valid, e.g. `-1` → opaque white.
         * @return the decoded [Color].
         */
        fun fromPackedInt(argb: Int): Color {
            val a = (argb shr 24) and 0xFF
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            return Color(r, g, b, a)
        }

        /**
         * Parses a hex colour: `#RGB`, `#RRGGBB`, or `#AARRGGBB` (alpha first).
         * The leading `#` is optional; a `0x` prefix is also accepted. A 3-digit
         * `#RGB` input is expanded by nibble-doubling (e.g. `"F53"` →
         * `255, 85, 51`). Alpha defaults to `255` for 3- and 6-digit input.
         *
         * @param hex the string to parse.
         * @return the decoded [Color].
         * @throws IllegalArgumentException if [hex] is not one of the accepted
         *   lengths or contains non-hex digits (`NumberFormatException`, an
         *   `IllegalArgumentException`).
         */
        fun fromHex(hex: String): Color {
            val cleanHex = hex.removePrefix("#").removePrefix("0x")
            return when (cleanHex.length) {
                3 -> { // RGB
                    val r = cleanHex[0].toString().repeat(2).toInt(16)
                    val g = cleanHex[1].toString().repeat(2).toInt(16)
                    val b = cleanHex[2].toString().repeat(2).toInt(16)
                    Color(r, g, b)
                }
                6 -> { // RRGGBB
                    val r = cleanHex.substring(0, 2).toInt(16)
                    val g = cleanHex.substring(2, 4).toInt(16)
                    val b = cleanHex.substring(4, 6).toInt(16)
                    Color(r, g, b)
                }
                8 -> { // AARRGGBB
                    val a = cleanHex.substring(0, 2).toInt(16)
                    val r = cleanHex.substring(2, 4).toInt(16)
                    val g = cleanHex.substring(4, 6).toInt(16)
                    val b = cleanHex.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                }
                else -> throw IllegalArgumentException("Invalid hex color format: $hex")
            }
        }

    }
}
