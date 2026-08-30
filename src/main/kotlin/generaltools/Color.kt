package com.spartanlabs.generaltools

/**
 * A Red-Green-Blue-Alpha color, each channel `0..255`.
 *
 * The renderer works in `0f..1f` floats, so [normalized] does that conversion
 * once per draw rather than every call site doing it by hand.
 */
data class Color(val red: Int, val green: Int, val blue: Int, val alpha: Int = 255) {

    /** `[r, g, b, a]` with every channel mapped from `0..255` to `0f..1f`. */
    fun normalized(): FloatArray =
        floatArrayOf(red / 255f, green / 255f, blue / 255f, alpha / 255f)

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
    }
}