package com.spartanlabs.generaltools

import org.apache.commons.io.FileUtils
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

fun cropImage(image: BufferedImage, x: Int, y: Int, width: Int, height: Int) =
    BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB).apply{
        graphics.drawImage(image,0,0,width,height,x,y,x+width,y+height,null)
    }
private fun saveImage(image: BufferedImage, filePath:String) = File("$filePath.png").apply{
    ImageIO.write(image, "png", this)
}
infix fun BufferedImage.to(fileName:String) = saveImage(this, fileName)
infix fun File.saveTo(fileName:String) = FileUtils.copyFile(this, File("$fileName.png"))
fun screenshotBrowser(address:String): BufferedImage {
    openInBrowser(address)
    Thread.sleep(1600)
    return screenshotArea(x = 40, y = 100, width = 2520, height = 1280)
}
fun openInBrowser(address:String) = Desktop.getDesktop().browse(URL(address).toURI())
fun screenshotArea(x:Int, y:Int, width:Int, height:Int) =
    screenshot(Rectangle().apply { this.x = x;this.y = y;this.width = width;this.height = height })
fun screenshotFull() =
    screenshot(Rectangle(Toolkit.getDefaultToolkit().screenSize))
private fun screenshot(area: Rectangle) = Robot().createScreenCapture(area)!!
private fun click(x:Int, y:Int){
    Robot().apply {
        mouseMove(x,y)
        mousePress(InputEvent.BUTTON1_DOWN_MASK)
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }
}
private fun typeKey(key:Int){
    Robot().apply{
        keyPress(key)
        keyRelease(key)
    }
}
private fun shiftType(key:Int){
    Robot().apply {
        keyPress(KeyEvent.VK_SHIFT)
        typeKey(key)
        keyRelease(KeyEvent.VK_SHIFT)
    }
}
private fun typeText(text:String){
    for(c in text){
        typeKey(c.code - 32)
    }
}
private fun typeCharacters(text:String){
    for(c in text){
        typeKey(c.code)
    }
}
private fun typeRandomKey() = typeKey((Math.random() * 26).toInt() + 65)
private fun typeRandomKeys(amount:Int){
    for (i in 1..amount)
        typeRandomKey()
}