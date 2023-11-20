package nebulosa.watney.plate.solving

import nebulosa.constants.DEG2RAD
import nebulosa.math.Angle

data class PointSearchStrategyOptions(
    val radius: Angle = DEFAULT_RADIUS,
    val maxNegativeDensityOffset: Int = 0,
    val maxPositiveDensityOffset: Int = 0,
) {

    companion object {

        @JvmStatic val DEFAULT_RADIUS = 2 * DEG2RAD

        @JvmStatic val DEFAULT = PointSearchStrategyOptions()
    }
}
