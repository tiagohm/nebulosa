package nebulosa.desktop.gui.image

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Histogram

class HistogramView : Canvas() {

    private val histogram = Histogram()

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

    fun draw(image: Image) {
        histogram.compute(image)
        draw()
    }

    fun draw() {
        val gc = graphicsContext2D

        gc.fill = Color.TRANSPARENT
        gc.clearRect(0.0, 0.0, width, height)

        val factor = width / 65536.0
        val maxValue = histogram.peakCount

        gc.lineWidth = 0.2
        gc.stroke = Color.BLACK

        for (k in 0 until 65536) {
            val x = k * factor
            val y = height - histogram[k] * height / maxValue
            if (y > 0) gc.strokeLine(x, y, x, height)
        }
    }
}
