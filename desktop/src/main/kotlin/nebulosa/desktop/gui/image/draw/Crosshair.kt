package nebulosa.desktop.gui.image.draw

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import nebulosa.desktop.view.image.Drawable

object Crosshair : Drawable {

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        val centerX = width / 2
        val centerY = height / 2

        graphics.stroke = Color.BLUE
        graphics.lineWidth = 2.0

        graphics.strokeOval(centerX - 3.0, centerY - 3.0, 3.0, 3.0)
        graphics.strokeOval(centerX - 64.0, centerY - 64.0, 128.0, 128.0)
        graphics.strokeOval(centerX - 128.0, centerY - 128.0, 256.0, 256.0)
        graphics.strokeOval(centerX - 256.0, centerY - 256.0, 512.0, 512.0)
    }
}
