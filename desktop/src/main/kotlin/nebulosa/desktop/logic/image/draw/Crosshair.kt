package nebulosa.desktop.logic.image.draw

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Draw
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D

object Crosshair : Draw() {

    override fun draw(source: Image, graphics: Graphics2D) {
        val centerX = source.width / 2
        val centerY = source.height / 2

        graphics.color = if (source.mono) Color.WHITE else Color.RED
        graphics.stroke = STROKE

        graphics.drawOval(centerX - 64, centerY - 64, 128, 128)
        graphics.drawOval(centerX - 128, centerY - 128, 256, 256)
        graphics.drawOval(centerX - 256, centerY - 256, 512, 512)
    }

    @JvmStatic private val STROKE = BasicStroke(1f)
}
