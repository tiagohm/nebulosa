package nebulosa.desktop.gui.guider

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import nebulosa.desktop.view.image.Drawable
import nebulosa.guiding.GuidePoint

class StarProfileIndicator : Drawable {

    var regionSize = 0.0
    lateinit var lockPosition: GuidePoint
    lateinit var primaryStar: GuidePoint

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = LINE_WIDTH
        graphics.stroke = Color.YELLOW

        val centerX = width / 2.0
        val centerY = height / 2.0

        graphics.setLineDashes(*DASH_PATTERN)
        graphics.strokeLine(0.0, centerY, width, centerY)
        graphics.strokeLine(centerX, 0.0, centerX, height)

        if (lockPosition.valid && primaryStar.valid) {
            graphics.fill = Color.RED
            val dotSize = 1.0
            val offsetX = centerX + primaryStar.x - lockPosition.x
            val offsetY = centerY + primaryStar.y - lockPosition.y
            graphics.fillOval(offsetX - dotSize / 2.0, offsetY - dotSize / 2.0, dotSize, dotSize)
        }
    }

    companion object {

        private const val LINE_WIDTH = 1.0
        @JvmStatic private val DASH_PATTERN = doubleArrayOf(4.0, 4.0)
    }
}
