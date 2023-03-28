package nebulosa.desktop.logic.image

import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import nebulosa.desktop.gui.control.Drawable
import kotlin.math.min

class Crosshair : Drawable() {

    private val lineHor = Line()
    private val lineVer = Line()
    private val circles = Array(4) { Circle() }

    init {
        with(lineHor) {
            stroke = Color.RED
            strokeWidth = 2.0
            add(this)
        }

        with(lineVer) {
            stroke = Color.RED
            strokeWidth = 2.0
            add(this)
        }

        for (circle in circles) {
            with(circle) {
                fill = Color.TRANSPARENT
                stroke = Color.RED
                strokeWidth = 2.0
                add(this)
            }
        }
    }

    override fun redraw() {
        val centerX = width / 2.0
        val centerY = height / 2.0

        lineHor.startX = 0.0
        lineHor.startY = centerY
        lineHor.endX = width
        lineHor.endY = centerY

        lineVer.startX = centerX
        lineVer.startY = 0.0
        lineVer.endX = centerX
        lineVer.endY = height

        var radius = min(width, height) / 4.0

        for (circle in circles) {
            circle.centerX = centerX
            circle.centerY = centerY
            circle.radius = radius
            radius /= 2.0
        }
    }
}
