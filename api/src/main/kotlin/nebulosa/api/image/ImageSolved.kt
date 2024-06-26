package nebulosa.api.image

import nebulosa.math.*
import nebulosa.platesolver.PlateSolution

data class ImageSolved(
    val solved: Boolean = false,
    val orientation: Double = 0.0,
    val scale: Double = 0.0,
    val rightAscensionJ2000: String = "",
    val declinationJ2000: String = "",
    val width: Double = 0.0,
    val height: Double = 0.0,
    val radius: Double = 0.0,
) {

    constructor(solution: PlateSolution) : this(
        solution.solved,
        solution.orientation.toDegrees,
        solution.scale.toArcsec,
        solution.rightAscension.formatHMS(),
        solution.declination.formatSignedDMS(),
        solution.width.toArcmin, solution.height.toArcmin,
        solution.radius.toDegrees,
    )
}
