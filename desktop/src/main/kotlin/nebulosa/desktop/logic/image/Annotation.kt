package nebulosa.desktop.logic.image

import nebulosa.desktop.gui.control.Drawable
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.skycatalog.stellarium.Nebula

class Annotation(
    calibration: Calibration,
    nebula: Nebula,
    hygDatabase: HygDatabase,
) : Drawable() {

    private val annotations = arrayOf(
        StarsAnnotation(calibration, hygDatabase),
        NebulaAnnotation(calibration, nebula),
    )

    init {
        annotations.forEach(::add)
    }

    override fun redraw(width: Double, height: Double) {
        annotations.forEach { it.redraw(width, height) }
    }
}
