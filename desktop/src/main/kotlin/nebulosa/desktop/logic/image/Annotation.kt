package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import nebulosa.desktop.view.image.Drawable
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.stellarium.Nebula

data class Annotation(
    val calibration: Calibration,
    val nebula: Nebula,
) : Drawable {

    private val brightStars = BrightStarsAnnotation(calibration)
    private val nebulaStars = NebulaAnnotation(calibration, nebula)

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        brightStars.draw(width, height, graphics)
        nebulaStars.draw(width, height, graphics)
    }
}
