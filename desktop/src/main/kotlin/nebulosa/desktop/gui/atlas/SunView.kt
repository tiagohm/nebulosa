package nebulosa.desktop.gui.atlas

import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import java.awt.image.BufferedImage
import java.net.URL
import java.nio.IntBuffer
import javax.imageio.ImageIO
import kotlin.math.hypot
import kotlin.math.min

class SunView : Canvas() {

    @Volatile private var image: WritableImage? = null

    suspend fun updateImage(sunImage: BufferedImage) = withIO {
        val data = IntArray(sunImage.width * sunImage.height)
        val centerX = sunImage.width / 2.0
        val centerY = sunImage.height / 2.0

        for (y in 0 until sunImage.height) {
            for (x in 0 until sunImage.width) {
                val distance = hypot(x - centerX, y - centerY)
                val color = sunImage.getRGB(x, y)
                val index = y * sunImage.width + x

                if (distance > 117) {
                    val gray = ((color shr 16 and 0xff) + (color shr 8 and 0xff) + (color and 0xff)) / 3

                    if (gray >= 170) {
                        data[index] = color
                    }
                } else {
                    data[index] = color
                }
            }
        }

        val buffer = IntBuffer.wrap(data)
        val pixelBuffer = PixelBuffer(sunImage.width, sunImage.height, buffer, PixelFormat.getIntArgbPreInstance())
        image = WritableImage(pixelBuffer)

        withMain { draw() }
    }

    fun draw() {
        val gc = graphicsContext2D

        val centerX = width / 2
        val centerY = height / 2

        val size = min(width, height)

        gc.drawImage(image ?: return, centerX - size / 2, centerY - size / 2, size, size)
    }
}
