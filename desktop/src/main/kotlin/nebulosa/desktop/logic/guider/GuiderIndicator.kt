package nebulosa.desktop.logic.guider

import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import nebulosa.desktop.gui.control.overlay.Overlay
import nebulosa.guiding.Guider

data class GuiderIndicator(private val guider: Guider) : Overlay() {

    private val lockPositionCrosshairLineHor = Line()
    private val lockPositionCrosshairLineVer = Line()
    private val primaryStarBox = Rectangle()

    init {
        with(lockPositionCrosshairLineHor) {
            stroke = Color.YELLOW
            strokeWidth = 2.0
            strokeDashArray.addAll(4.0, 4.0)
            add(this)
        }

        with(lockPositionCrosshairLineVer) {
            stroke = Color.YELLOW
            strokeWidth = 2.0
            strokeDashArray.addAll(4.0, 4.0)
            add(this)
        }

        with(primaryStarBox) {
            fill = Color.TRANSPARENT
            stroke = Color.CYAN
            strokeWidth = 2.0
            add(this)
        }
    }

    override fun redraw(width: Double, height: Double) {
        with(guider.lockPosition) {
            lockPositionCrosshairLineHor.isVisible = valid
            lockPositionCrosshairLineVer.isVisible = valid

            if (valid) {
                lockPositionCrosshairLineHor.startX = 0.0
                lockPositionCrosshairLineHor.startY = y
                lockPositionCrosshairLineHor.endX = width
                lockPositionCrosshairLineHor.endY = y

                lockPositionCrosshairLineVer.startX = x
                lockPositionCrosshairLineVer.startY = 0.0
                lockPositionCrosshairLineVer.endX = x
                lockPositionCrosshairLineVer.endY = height
            }
        }

        with(guider.primaryStar) {
            primaryStarBox.stroke = if (valid) Color.CYAN
            else if (x != 0.0 && y != 0.0 && x.isFinite() && y.isFinite()) Color.INDIANRED
            else {
                primaryStarBox.isVisible = false
                return@with
            }

            primaryStarBox.isVisible = true

            val size = guider.searchRegion

            primaryStarBox.x = x - size
            primaryStarBox.y = y - size
            primaryStarBox.width = size * 2
            primaryStarBox.height = primaryStarBox.width
        }
    }
}
