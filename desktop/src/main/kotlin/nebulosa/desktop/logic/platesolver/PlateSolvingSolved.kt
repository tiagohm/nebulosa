package nebulosa.desktop.logic.platesolver

import nebulosa.platesolving.Calibration
import java.io.File

data class PlateSolvingSolved(
    override val file: File,
    val calibration: Calibration,
) : PlateSolvingEvent
