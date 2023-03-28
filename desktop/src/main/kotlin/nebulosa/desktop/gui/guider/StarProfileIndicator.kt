package nebulosa.desktop.gui.guider

import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import nebulosa.desktop.gui.control.Drawable
import nebulosa.guiding.GuidePoint

class StarProfileIndicator : Drawable() {

    private val crosshairLineHor = Line()
    private val crosshairLineVer = Line()
    private val primaryStarDot = Circle()

    @Volatile private var initialized = false
    @Volatile private lateinit var lockPosition: GuidePoint
    @Volatile private lateinit var primaryStar: GuidePoint

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
            radius = 1.0
            add(this)
        }
    }

    fun draw(lockPosition: GuidePoint, primaryStar: GuidePoint) {
        this.lockPosition = lockPosition
        this.primaryStar = primaryStar
        initialized = true
        redraw()
    }

    override fun redraw() {
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

        primaryStarDot.isVisible = initialized && primaryStar.valid

        if (initialized && primaryStar.valid) {
            val offsetX = centerX + primaryStar.x - lockPosition.x
            val offsetY = centerY + primaryStar.y - lockPosition.y
            primaryStarDot.centerX = offsetX
            primaryStarDot.centerY = offsetY
        }
    }
}
