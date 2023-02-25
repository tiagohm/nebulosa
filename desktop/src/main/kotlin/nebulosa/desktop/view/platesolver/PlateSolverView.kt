package nebulosa.desktop.view.platesolver

import nebulosa.desktop.view.View
import nebulosa.math.Angle
import java.io.File

interface PlateSolverView : View {

    var plateSolverType: PlateSolverType

    var pathOrUrl: String

    var apiKey: String

    val blind: Boolean

    val centerRA: Angle

    val centerDEC: Angle

    val radius: Angle

    val downsampleFactor: Int

    fun updateFilePath(file: File)

    fun updateAstrometrySolution(
        ra: Angle, dec: Angle,
        orientation: Angle, radius: Angle,
        scale: Double,
    )
}
