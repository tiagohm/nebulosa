package nebulosa.desktop.cameras

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import nebulosa.imaging.Image
import kotlin.math.max

class HistogramView : Canvas() {

    override fun isResizable() = true

    override fun maxHeight(width: Double) = Double.POSITIVE_INFINITY

    override fun maxWidth(height: Double) = Double.POSITIVE_INFINITY

    override fun minWidth(height: Double) = 1.0

    override fun minHeight(width: Double) = 1.0

    override fun prefWidth(height: Double) = width

    override fun prefHeight(width: Double) = height

    override fun resize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }

    @Synchronized
    fun draw(fits: Image) {
        val gc = graphicsContext2D

        gc.fill = Color.BLACK
        gc.fillRect(0.0, 0.0, width, height)

        var maxHeight = 0

        val histogramData = IntArray(256)

        for (y in 0 until fits.height) {
            for (x in 0 until fits.width) {
                val index = y * fits.stride + x * fits.pixelStride

                maxHeight = if (fits.mono) {
                    val i = (fits.data[index] * 255f).toInt()
                    histogramData[i]++
                    max(maxHeight, histogramData[i])
                } else {
                    val a = (fits.data[index] * 255f).toInt()
                    val b = (fits.data[index + 1] * 255f).toInt()
                    val c = (fits.data[index + 2] * 255f).toInt()
                    val i = (a + b + c) / 3
                    histogramData[i]++
                    max(maxHeight, histogramData[i])
                }
            }
        }

        val lineWidth = width / 256.0
        gc.lineWidth = lineWidth

        gc.stroke = Color.WHITE

        for (k in 0..255) {
            val x = k * lineWidth + lineWidth / 2
            gc.strokeLine(x, height - histogramData[k] * height / maxHeight, x, height)
        }
    }
}
