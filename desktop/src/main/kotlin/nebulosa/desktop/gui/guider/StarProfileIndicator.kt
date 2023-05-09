package nebulosa.desktop.gui.guider

import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import nebulosa.desktop.gui.control.overlay.Overlay
import nebulosa.guiding.GuidePoint
import nebulosa.guiding.StarPoint
import nebulosa.guiding.internal.Point
import nebulosa.guiding.internal.Star

class StarProfileIndicator : Overlay() {

    private val crosshairLineHor = Line()
    private val crosshairLineVer = Line()
    private val primaryStarDot = Circle()

    @Volatile private var lockPosition: GuidePoint = Point()
    @Volatile private var primaryStar: StarPoint = Star()
    @Volatile private var boxSize = 0.0

    init {
        with(crosshairLineHor) {
            stroke = Color.YELLOW
            strokeWidth = 1.0
            strokeDashArray.addAll(2.0, 2.0)
            add(this)
        }

        with(crosshairLineVer) {
            stroke = Color.YELLOW
            strokeWidth = 1.0
            strokeDashArray.addAll(2.0, 2.0)
            add(this)
        }

        with(primaryStarDot) {
            fill = Color.RED
            radius = 3.0
            add(this)
        }
    }

    fun draw(lockPosition: GuidePoint, primaryStar: StarPoint, boxSize: Double) {
        this.lockPosition = lockPosition
        this.primaryStar = primaryStar
        this.boxSize = boxSize
        redraw()
    }

    override fun redraw(width: Double, height: Double) {
        val centerX = width / 2.0
        val centerY = height / 2.0

        crosshairLineHor.startX = 0.0
        crosshairLineHor.startY = centerY
        crosshairLineHor.endX = width
        crosshairLineHor.endY = centerY

        crosshairLineVer.startX = centerX
        crosshairLineVer.startY = 0.0
        crosshairLineVer.endX = centerX
        crosshairLineVer.endY = height

        primaryStarDot.isVisible = if (primaryStar.valid) {
            val offsetX = centerX - lockPosition.dX(primaryStar) * (width / boxSize)
            val offsetY = centerY - lockPosition.dY(primaryStar) * (height / boxSize)

            if (offsetX >= 0 && offsetX < width && offsetY >= 0 && offsetY < height) {
                primaryStarDot.centerX = offsetX
                primaryStarDot.centerY = offsetY
                true
            } else {
                false
            }
        } else {
            false
        }
    }
}
