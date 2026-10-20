package com.spartanlabs.generaltools

import org.junit.jupiter.api.Assumptions.assumeFalse
import org.slf4j.LoggerFactory
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserActionsTest {
    private val log = LoggerFactory.getLogger(UserActionsTest::class.java)

    private fun sampleImage(width: Int = 20, height: Int = 20): BufferedImage =
        BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).apply {
            for (x in 0 until width) for (y in 0 until height) {
                setRGB(x, y, 0xFF00FF00.toInt())
            }
        }

    @Test
    fun `cropImage returns an image with the requested dimensions`() {
        log.info("Running cropImage test")
        val original = sampleImage(50, 50)

        val cropped = cropImage(original, 5, 5, 10, 10)

        assertEquals(10, cropped.width)
        assertEquals(10, cropped.height)
    }

    @Test
    fun `BufferedImage to saves a png file at the given path`() {
        log.info("Running BufferedImage#to test")
        val image = sampleImage()
        val tempPath = File.createTempFile("user-actions-test", "").absolutePath

        val saved = image to tempPath
        try {
            assertTrue(saved.exists())
            assertTrue(saved.name.endsWith(".png"))
        } finally {
            saved.delete()
        }
    }

    @Test
    fun `File saveTo copies a file with a png extension`() {
        log.info("Running File#saveTo test")
        val source = sampleImage() to File.createTempFile("user-actions-source", "").absolutePath
        val destinationPath = File.createTempFile("user-actions-dest", "").absolutePath
        val destination = File("$destinationPath.png")

        try {
            source saveTo destinationPath
            assertTrue(destination.exists())
        } finally {
            source.delete()
            destination.delete()
        }
    }

    @Test
    fun `screenshotFull captures an image the size of the screen`() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping screenshot test: no display available")
        log.info("Running screenshotFull test")
        val screenSize = Toolkit.getDefaultToolkit().screenSize

        val screenshot = screenshotFull()

        assertEquals(screenSize.width, screenshot.width)
        assertEquals(screenSize.height, screenshot.height)
    }

    @Test
    fun `screenshotArea captures the requested region`() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping screenshot test: no display available")
        log.info("Running screenshotArea test")

        val screenshot = screenshotArea(0, 0, 10, 10)

        assertEquals(10, screenshot.width)
        assertEquals(10, screenshot.height)
    }
}
