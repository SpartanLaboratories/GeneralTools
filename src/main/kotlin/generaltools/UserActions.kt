package com.spartanlabs.generaltools

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/** Logger used for diagnostic output from this file's functions. */
private val log = LoggerFactory.getLogger("com.spartanlabs.generaltools.UserActions")

/**
 * Crops the specified image to the desired dimensions
 */
fun cropImage(image: BufferedImage, x: Int, y: Int, width: Int, height: Int): BufferedImage {
    log.debug("Cropping image to x={}, y={}, width={}, height={}", x, y, width, height)
    return BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB).apply{
        graphics.drawImage(image,0,0,width,height,x,y,x+width,y+height,null)
    }
}
/**
 * Writes [image] to disk as a PNG file at `$filePath.png` and returns the resulting [File]
 */
private fun saveImage(image: BufferedImage, filePath:String): File {
    log.debug("Saving image to '{}.png'", filePath)
    return File("$filePath.png").apply{
        ImageIO.write(image, "png", this)
    }
}

/**
 * Saves the receiver image to the specified file location
 */
infix fun BufferedImage.to(fileName:String) = saveImage(this, fileName)
/**
 * Copies the receiver file to `$fileName.png`
 */
infix fun File.saveTo(fileName:String) {
    log.debug("Copying file '{}' to '{}.png'", this.path, fileName)
    FileUtils.copyFile(this, File("$fileName.png"))
}
/**
 * Opens [address] in the system's default browser, waits for the page to load,
 * then returns a screenshot of the browser window's content area
 */
fun screenshotBrowser(address:String): BufferedImage {
    log.info("Screenshotting browser at address '{}'", address)
    openInBrowser(address)
    Thread.sleep(1600)
    return screenshotArea(x = 40, y = 100, width = 2520, height = 1280)
}
/**
 * Opens [address] in the system's default browser
 */
fun openInBrowser(address:String) {
    log.debug("Opening browser at address '{}'", address)
    Desktop.getDesktop().browse(URL(address).toURI())
}
/**
 * Takes a screenshot of the rectangular screen region described by the given
 * coordinates and dimensions
 */
fun screenshotArea(x:Int, y:Int, width:Int, height:Int): BufferedImage {
    log.debug("Taking screenshot of area x={}, y={}, width={}, height={}", x, y, width, height)
    return screenshot(Rectangle().apply { this.x = x;this.y = y;this.width = width;this.height = height })
}
/**
 * Takes a screenshot of the entire screen
 */
fun screenshotFull(): BufferedImage {
    log.debug("Taking full screen screenshot")
    return screenshot(Rectangle(Toolkit.getDefaultToolkit().screenSize))
}
/**
 * Captures the given screen [area] using a [Robot]
 */
private fun screenshot(area: Rectangle) = Robot().createScreenCapture(area)!!
/**
 * Moves the mouse to the given coordinates and performs a left-click
 */
private fun click(x:Int, y:Int){
    log.trace("Clicking at x={}, y={}", x, y)
    Robot().apply {
        mouseMove(x,y)
        mousePress(InputEvent.BUTTON1_DOWN_MASK)
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }
}
/**
 * Presses and releases the given key code
 */
private fun typeKey(key:Int){
    Robot().apply{
        keyPress(key)
        keyRelease(key)
    }
}
/**
 * Presses and releases the given key code while holding Shift
 */
private fun shiftType(key:Int){
    Robot().apply {
        keyPress(KeyEvent.VK_SHIFT)
        typeKey(key)
        keyRelease(KeyEvent.VK_SHIFT)
    }
}
/**
 * Types [text] as key presses, shifting each character's key code down by 32.
 * <br>
 * Note: this only produces correct results for uppercase input, since the
 * shift is intended to map lowercase ASCII down to its key code
 */
private fun typeText(text:String){
    log.trace("Typing text of length {}", text.length)
    for(c in text){
        typeKey(c.code - 32)
    }
}
/**
 * Types each character of [text] using its raw character code as the key code
 */
private fun typeCharacters(text:String){
    for(c in text){
        typeKey(c.code)
    }
}
/**
 * Types a single random uppercase letter key
 */
private fun typeRandomKey() = typeKey((Math.random() * 26).toInt() + 65)
/**
 * Types the given number of random uppercase letter keys
 */
private fun typeRandomKeys(amount:Int){
    log.trace("Typing {} random key(s)", amount)
    for (i in 1..amount)
        typeRandomKey()
}