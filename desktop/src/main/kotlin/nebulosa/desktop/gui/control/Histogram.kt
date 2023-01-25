package nebulosa.desktop.gui.control

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import nebulosa.imaging.Image
import kotlin.math.max

class Histogram : Canvas() {

    private val histogramData = IntArray(256)
    private var maxHeight = 1

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

    fun draw(fits: Image) {
        maxHeight = 1
        histogramData.fill(0)

        for (y in 0 until fits.height) {
            for (x in 0 until fits.width) {
                val index = y * fits.stride + x * fits.pixelStride

                maxHeight = if (fits.mono) {
                    val i = (fits.data[index] * 255f).toInt()
                    histogramData[i]++
                    max(maxHeight, histogramData[i])
                } else {
                    val i = ((fits.data[index] + fits.data[index + 1] + fits.data[index + 2]) * 85f).toInt()
                    histogramData[i]++
                    max(maxHeight, histogramData[i])
                }
            }
        }

        draw()
    }

    fun draw() {
        val gc = graphicsContext2D

        gc.fill = BACKGROUND_COLOR
        gc.fillRect(0.0, 0.0, width, height)

        gc.lineWidth = 1.0
        gc.stroke = Color.BLACK

        val factor = 255f / width

        for (k in 0 until width.toInt()) {
            val x = k.toDouble() - 0.5
            val i = (k * factor).toInt()
            gc.strokeLine(x, height - histogramData[i] * height / maxHeight, x, height)
        }
    }

    companion object {

        @JvmStatic private val BACKGROUND_COLOR = Color(0.957, 0.957, 0.957, 1.0) // #F4F4F4
    }
}
