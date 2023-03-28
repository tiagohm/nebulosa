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

    private val stars = StarsAnnotation(calibration, hygDatabase)
    private val nebula = NebulaAnnotation(calibration, nebula)

    init {
        add(stars)
        add(this.nebula)
    }

    fun initialize() {
        stars.initialize()
        nebula.initialize()
    }

    override fun redraw() {
        stars.redraw()
        nebula.redraw()
    }
}
