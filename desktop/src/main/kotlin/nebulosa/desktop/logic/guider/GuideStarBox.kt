package nebulosa.desktop.logic.guider

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import nebulosa.desktop.view.image.Drawable
import nebulosa.guiding.internal.GuideStar

data class GuideStarBox(private val guideStars: Iterable<GuideStar>) : Drawable {

    var boxSize = 30.0

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = 3.0
        graphics.stroke = Color.GREEN
        graphics.fill = Color.GREEN

        val radius = boxSize / 2.0

        guideStars.forEachIndexed { index, star ->
            if (!star.valid) return@forEachIndexed
            val centerX = star.x - radius
            val centerY = star.y - radius
            if (index == 0) graphics.strokeRect(centerX, centerY, boxSize, boxSize)
            else graphics.strokeOval(centerX, centerY, 6.0, 6.0)
        }
    }
}
