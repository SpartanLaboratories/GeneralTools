package com.spartanlabs.generaltools

import kotlin.math.roundToInt

/**
 * A Red-Green-Blue-Alpha color, each channel `0..255`.
 *
 */
data class Color(val red: Int, val green: Int, val blue: Int, val alpha: Int = 255) {

    /** `[r, g, b, a]` with every channel mapped from `0..255` to `0f..1f`. */
    fun normalized(): FloatArray =
        floatArrayOf(red / 255f, green / 255f, blue / 255f, alpha / 255f)


    // --- Color Transformations & Variations ---

    /**
     * This colour moved [fraction] (`0.0..1.0`) of the way toward white, with
     * [alpha] left untouched.
     */
    fun lightened(fraction: Double = 0.35): Color {
        val f = fraction.coerceIn(0.0, 1.0)
        fun towardWhite(channel: Int) = (channel + (255 - channel) * f).toInt().coerceIn(0, 255)
        return Color(towardWhite(red), towardWhite(green), towardWhite(blue), alpha)
    }

    /**
     * This colour moved [fraction] (`0.0..1.0`) of the way toward black, with
     * [alpha] left untouched.
     */
    fun darkened(fraction: Double = 0.35): Color {
        val f = fraction.coerceIn(0.0, 1.0)
        fun towardBlack(channel: Int) = (channel * (1.0 - f)).toInt().coerceIn(0, 255)
        return Color(towardBlack(red), towardBlack(green), towardBlack(blue), alpha)
    }

    /** Returns a copy of this color with a modified [newAlpha] channel (`0..255`). */
    fun withAlpha(newAlpha: Int): Color = copy(alpha = newAlpha.coerceIn(0, 255))

    /** Inverts the RGB components while keeping [alpha] untouched. */
    fun inverted(): Color = Color(255 - red, 255 - green, 255 - blue, alpha)

    /** Converts the color to grayscale using standard ITU-R BT.601 luma weights. */
    fun grayscale(): Color {
        val gray = (0.299 * red + 0.587 * green + 0.114 * blue).roundToInt().coerceIn(0, 255)
        return Color(gray, gray, gray, alpha)
    }

    /** Linearly interpolates (blends) between this color and [target] by [fraction] (`0.0..1.0`). */
    fun lerp(target: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        fun interpolate(start: Int, end: Int) = (start + (end - start) * f).roundToInt().coerceIn(0, 255)
        return Color(
            interpolate(red, target.red),
            interpolate(green, target.green),
            interpolate(blue, target.blue),
            interpolate(alpha, target.alpha)
        )
    }

    /** Returns relative luminance value (`0.0f..1.0f`) for contrast calculations. */
    fun luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f

    /** Encodes color channels into a single 32-bit ARGB `Int`. */
    fun toPackedInt(): Int = (alpha shl 24) or (red shl 16) or (green shl 8) or blue

    /** Returns a hex string representation (e.g., `#FF5733` or `#FF573380`). */
    fun toHex(includeAlpha: Boolean = false): String {
        return if (includeAlpha) {
            String.format("#%02X%02X%02X%02X", red, green, blue, alpha)
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

        /** Constructs a [Color] from float values in the range `0f..1f`. */
        fun fromNormalized(r: Float, g: Float, b: Float, a: Float = 1f): Color {
            return Color(
                (r.coerceIn(0f, 1f) * 255).roundToInt(),
                (g.coerceIn(0f, 1f) * 255).roundToInt(),
                (b.coerceIn(0f, 1f) * 255).roundToInt(),
                (a.coerceIn(0f, 1f) * 255).roundToInt()
            )
        }

        /** Decodes a 32-bit ARGB packed `Int` into a [Color]. */
        fun fromPackedInt(argb: Int): Color {
            val a = (argb shr 24) and 0xFF
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            return Color(r, g, b, a)
        }

        /** Parses a hex string formatted as `#RGB`, `#RRGGBB`, or `#AARRGGBB` / `#RRGGBBAA`. */
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