package nebulosa.desktop.gui.control

import javafx.application.Platform
import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import java.net.URL
import java.nio.IntBuffer
import javax.imageio.ImageIO
import kotlin.math.hypot
import kotlin.math.min

class SunView : Canvas() {

    @Volatile private var image: WritableImage? = null

    @Synchronized
    fun updateImage() {
        val sunImage = ImageIO.read(URL("https://sdo.gsfc.nasa.gov/assets/img/latest/latest_256_HMIIF.jpg"))
        val data = IntArray(sunImage.width * sunImage.height)
        val buffer = IntBuffer.wrap(data)

        for (y in 0 until sunImage.height) {
            for (x in 0 until sunImage.width) {
                val distance = hypot(x - 128.0, y - 128.0)
                val color = sunImage.getRGB(x, y)
                val index = y * sunImage.width + x

                if (distance > 118) {
                    val gray = ((color shr 16 and 0xff) + (color shr 8 and 0xff) + (color and 0xff)) / 3

                    if (gray >= 170) {
                        data[index] = color
                    }
                } else {
                    data[index] = color
                }
            }
        }

        val pixelBuffer = PixelBuffer(sunImage.width, sunImage.height, buffer, PixelFormat.getIntArgbPreInstance())
        image = WritableImage(pixelBuffer)

        Platform.runLater { draw() }
    }

    fun draw() {
        val gc = graphicsContext2D

        val centerX = width / 2
        val centerY = height / 2

        val size = min(width, height)

        gc.drawImage(image ?: return, centerX - size / 2, centerY - size / 2, size, size)
    }
}
