package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import nebulosa.desktop.view.image.Drawable
import kotlin.math.min

object Crosshair : Drawable {

    private const val LINE_WIDTH = 2.0

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        val centerX = width / 2.0
        val centerY = height / 2.0

        graphics.stroke = Color.RED
        graphics.lineWidth = LINE_WIDTH

        // Horizontal line.
        graphics.strokeLine(0.0, centerY - LINE_WIDTH / 2.0, width, centerY - LINE_WIDTH / 2.0)

        // Vertical line.
        graphics.strokeLine(centerX - LINE_WIDTH / 2.0, 0.0, centerX - LINE_WIDTH / 2.0, height)

        // Circles.
        var size = min(width, height) / 2.0

        repeat(4) {
            val nextSize = size / 2.0
            graphics.strokeOval(centerX - nextSize, centerY - nextSize, size, size)
            size = nextSize
        }
    }
}
