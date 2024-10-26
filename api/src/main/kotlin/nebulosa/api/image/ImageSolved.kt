package nebulosa.api.image

import nebulosa.math.*
import nebulosa.platesolver.Parity
import nebulosa.platesolver.PlateSolution

data class ImageSolved(
    @JvmField val solved: Boolean = false,
    @JvmField val orientation: Double = 0.0,
    @JvmField val scale: Double = 0.0,
    @JvmField val rightAscensionJ2000: String = "",
    @JvmField val declinationJ2000: String = "",
    @JvmField val width: Double = 0.0,
    @JvmField val height: Double = 0.0,
    @JvmField val radius: Double = 0.0,
    @JvmField val parity: Parity = Parity.NORMAL,
) {

    constructor(solution: PlateSolution) : this(
        solution.solved,
        solution.orientation.toDegrees,
        solution.scale.toArcsec,
        solution.rightAscension.formatHMS(),
        solution.declination.formatSignedDMS(),
        solution.width.toArcmin, solution.height.toArcmin,
        solution.radius.toDegrees,
        solution.parity,
    )

    companion object {

        val NO_SOLUTION = ImageSolved(PlateSolution.NO_SOLUTION)
    }
}
