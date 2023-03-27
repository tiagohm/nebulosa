package nebulosa.desktop.logic.guider

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import nebulosa.desktop.view.image.Drawable
import nebulosa.guiding.GuidePoint
import nebulosa.guiding.Guider

data class GuiderIndicator(private val guider: Guider) : Drawable {

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = LINE_WIDTH

        val radius = guider.searchRegion
        val boxSize = radius * 2.0

        graphics.drawPrimaryStar(guider.primaryStar, boxSize)

        guider.forEach {
            if (it !== guider.primaryStar) {
                graphics.drawSecondaryStar(it, radius)
            }
        }

        graphics.drawLockPositionCrosshair(guider.lockPosition, width, height)
    }

    companion object {

        private const val LINE_WIDTH = 1.2
        @JvmStatic private val DASH_PATTERN = doubleArrayOf(4.0, 4.0)

        @JvmStatic
        private fun GraphicsContext.drawLockPositionCrosshair(point: GuidePoint, width: Double, height: Double) {
            if (point.valid) {
                stroke = Color.YELLOW
                setLineDashes(*DASH_PATTERN)
                strokeLine(0.0, point.y - LINE_WIDTH / 2, width, point.y - LINE_WIDTH / 2)
                strokeLine(point.x - LINE_WIDTH / 2, 0.0, point.x - LINE_WIDTH / 2, height)
            }
        }

        @JvmStatic
        private fun GraphicsContext.drawPrimaryStar(point: GuidePoint, size: Double) {
            stroke = if (point.valid) Color.CYAN
            else if (point.x != 0.0 && point.y != 0.0 && point.x.isFinite() && point.y.isFinite()) Color.INDIANRED
            else return

            setLineDashes()
            strokeRect(point.x - size / 2 - LINE_WIDTH / 2, point.y - size / 2 - LINE_WIDTH / 2, size, size)
        }

        @JvmStatic
        private fun GraphicsContext.drawSecondaryStar(point: GuidePoint, size: Double) {
            stroke = if (point.valid) Color.CYAN
            else if (point.x.isFinite() && point.y.isFinite()) Color.INDIANRED
            else return

            setLineDashes(*DASH_PATTERN)
            strokeOval(point.x - size / 2 - LINE_WIDTH / 2, point.y - size / 2 - LINE_WIDTH / 2, size, size)
        }
    }
}
