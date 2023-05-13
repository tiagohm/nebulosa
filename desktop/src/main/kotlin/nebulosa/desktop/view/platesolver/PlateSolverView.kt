package nebulosa.desktop.view.platesolver

import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import java.io.File

interface PlateSolverView : View {

    var type: PlateSolverType

    var pathOrUrl: String

    var apiKey: String

    val blind: Boolean

    val centerRA: Angle

    val centerDEC: Angle

    var radius: Angle

    var downsampleFactor: Int

    fun fileWasLoaded(file: File)

    fun updateParameters(
        blind: Boolean,
        centerRA: Angle, centerDEC: Angle,
    )

    suspend fun solve(
        file: File,
        blind: Boolean = true,
        centerRA: Angle = Angle.ZERO, centerDEC: Angle = Angle.ZERO,
        radius: Angle = this.radius,
        block: (Calibration?) -> Unit,
    )
}
