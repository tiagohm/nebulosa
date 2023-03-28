package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import nebulosa.desktop.view.image.Drawable
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.skycatalog.stellarium.Nebula

class Annotation(
    calibration: Calibration,
    nebula: Nebula,
    hygDatabase: HygDatabase,
) : Drawable {

    private val stars = StarsAnnotation(calibration, hygDatabase)
    private val nebula = NebulaAnnotation(calibration, nebula)

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        stars.draw(width, height, graphics)
        nebula.draw(width, height, graphics)
    }
}
