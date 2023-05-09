package nebulosa.desktop.gui.control

import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import java.awt.image.BufferedImage
import java.nio.IntBuffer
import kotlin.math.hypot
import kotlin.math.min

class SunImageView : Canvas() {

    @Volatile private var image: WritableImage? = null

    suspend fun draw(sunImage: BufferedImage) = withIO {
        val data = IntArray(sunImage.width * sunImage.height)
        val centerX = sunImage.width / 2.0
        val centerY = sunImage.height / 2.0

        val sunRadius = (centerX * 0.92).toInt()
        val pixels = IntArray(5)

        for (y in 0 until sunImage.height) {
            for (x in 0 until sunImage.width) {
                val distance = hypot(x - centerX, y - centerY)
                val color = sunImage.getRGB(x, y)
                val index = y * sunImage.width + x

                if (distance > sunRadius) {
                    val gray = ((color shr 16 and 0xff) + (color shr 8 and 0xff) + (color and 0xff)) / 3

                    if (gray >= 170) {
                        data[index] = color
                    } else if (x > 1 && y > 1 && x < sunImage.width - 1 && y < sunImage.height - 1) {
                        pixels[1] = sunImage.getRGB(x - 1, y - 1)
                        pixels[2] = sunImage.getRGB(x + 1, y - 1)
                        pixels[3] = sunImage.getRGB(x - 1, y + 1)
                        pixels[4] = sunImage.getRGB(x + 1, y + 1)

                        // Blur (Anti-aliasing) the Sun edge.
                        val red = pixels.sumOf { it shr 16 and 0xff } / pixels.size
                        val green = pixels.sumOf { it shr 8 and 0xff } / pixels.size
                        val blue = pixels.sumOf { it and 0xff } / pixels.size

                        if (red >= 50) {
                            data[index] = 0xFF000000.toInt() + (red and 0xff shl 16) + (green and 0xff shl 8) + (blue and 0xff)
                        }
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
