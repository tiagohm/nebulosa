package nebulosa.desktop.gui.control

import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import nebulosa.desktop.view.atlas.Twilight

class AltitudeGraph : ResizableCanvas() {

    private val points = arrayListOf<Point2D>()
    private var now = 0.0
    private var civilTwilight = Twilight.EMPTY
    private var nauticalTwilight = Twilight.EMPTY
    private var astronomicalTwilight = Twilight.EMPTY

    override fun resize(width: Double, height: Double) {
        super.resize(width, height)
        draw()
    }

    fun draw(
        points: List<Point2D> = this.points,
        now: Double = this.now,
        civilTwilight: Twilight = this.civilTwilight,
        nauticalTwilight: Twilight = this.nauticalTwilight,
        astronomicalTwilight: Twilight = this.astronomicalTwilight,
    ) {
        graphicsContext2D.clearRect(0.0, 0.0, width, height)

        if (points !== this.points) {
            this.points.clear()
            this.points.addAll(points)
        }

        this.civilTwilight = civilTwilight
        this.nauticalTwilight = nauticalTwilight
        this.astronomicalTwilight = astronomicalTwilight

        drawTicks()
        drawGraph()
    }

    private fun drawGraph() {
        drawTwilights()
        drawNow()
        drawPoints()
    }

    private fun drawPoints() {
        if (points.size > 1) {
            val gc = graphicsContext2D

            val realWidth = width - PADDING - HALF_PADDING
            val realHeight = height - PADDING - HALF_PADDING

            gc.lineWidth = 1.0
            gc.stroke = Color.BLACK

            val it = points.iterator()
            var first = it.next()

            var maxY = first.y
            var maxX = first.x

            while (it.hasNext()) {
                val second = it.next()

                val x0 = PADDING + realWidth * first.x
                val y0 = height - realHeight * first.y - PADDING
                val x1 = PADDING + realWidth * second.x
                val y1 = height - realHeight * second.y - PADDING
                gc.strokeLine(x0, y0, x1, y1)

                if (second.y > maxY) {
                    maxY = (second.y + first.y) / 2.0
                    maxX = (second.x + first.x) / 2.0
                }

                first = second
            }

            // Max Altitude.
            if (maxY > 0.0) {
                val x = PADDING + realWidth * maxX
                val y = height - realHeight * maxY - PADDING
                gc.fillOval(x - 4.0, y - 4.0, 8.0, 8.0)

                gc.textAlign = TextAlignment.CENTER
                gc.textBaseline = VPos.CENTER
                gc.fillText("%02d°".format((maxY * 90.0).toInt()), x, y - 12.0)
            }
        }
    }

    private fun drawNow() {

    }

    private fun drawTwilights() {

    }

    private fun drawTicks() {
        val gc = graphicsContext2D

        val horTickStepSize = (width - PADDING - HALF_PADDING) / 24.0
        val horTickLabelStepSize = (width - PADDING - HALF_PADDING) / 8.0
        val verTickStepSize = (height - PADDING - HALF_PADDING) / 9.0
        val verTickLabelStepSize = (height - PADDING - HALF_PADDING) / 3.0

        gc.lineWidth = 1.0
        gc.stroke = Color.BLACK
        gc.fill = Color.BLACK

        // Horizontal.

        gc.strokeLine(PADDING, height - PADDING, width - HALF_PADDING, height - PADDING)

        // Ticks.
        for (i in 0..24) {
            val x = PADDING + horTickStepSize * i
            val y = height - PADDING
            val tickHeight = if (i % 3 == 0) 8.0 else 4.0
            gc.strokeLine(x, y, x, y + tickHeight)
        }

        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.CENTER

        // Tick labels.
        for (i in 0..8) {
            val tickText = (12 + i * 3) % 24
            val x = PADDING + horTickLabelStepSize * i
            gc.fillText("$tickText", x, height - 12.0)
        }

        // Vertical.

        gc.strokeLine(PADDING, HALF_PADDING, PADDING, height - PADDING)

        // Ticks.
        for (i in 0..9) {
            val y = height - verTickStepSize * i - PADDING
            val tickWidth = if (i % 3 == 0) 8.0 else 4.0
            gc.strokeLine(PADDING - tickWidth, y, PADDING, y)
        }

        gc.textAlign = TextAlignment.RIGHT
        gc.textBaseline = VPos.CENTER

        // Tick labels.
        for (i in 0..3) {
            val x = PADDING - HALF_PADDING / 2.0
            val y = height - verTickLabelStepSize * i - PADDING
            gc.fillText("${i * 30}", x, y)
        }
    }

    companion object {

        private const val PADDING = 32.0
        private const val HALF_PADDING = PADDING / 2.0
    }
}
