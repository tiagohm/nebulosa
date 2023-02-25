package nebulosa.desktop.view.platesolver

import nebulosa.desktop.view.View
import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import java.io.File
import java.util.concurrent.CompletableFuture

interface PlateSolverView : View {

    var plateSolverType: PlateSolverType

    var pathOrUrl: String

    var apiKey: String

    val blind: Boolean

    val centerRA: Angle

    val centerDEC: Angle

    var radius: Angle

    val downsampleFactor: Int

    fun updateFilePath(file: File)

    fun updateParameters(blind: Boolean, ra: Angle, dec: Angle)

    fun solve(
        file: File,
        blind: Boolean = true,
        centerRA: Angle = Angle.ZERO, centerDEC: Angle = Angle.ZERO,
        radius: Angle = Angle.ZERO
    ): CompletableFuture<Calibration>

    fun updateAstrometrySolution(
        ra: Angle, dec: Angle,
        orientation: Angle, radius: Angle,
        scale: Double,
        width: Double, height: Double,
    )
}
