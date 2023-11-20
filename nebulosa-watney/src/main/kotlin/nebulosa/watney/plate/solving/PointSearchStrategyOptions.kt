package nebulosa.watney.plate.solving

import nebulosa.math.Angle
import nebulosa.math.deg

data class PointSearchStrategyOptions(
    val radius: Angle = 2.deg,
    override val maxNegativeDensityOffset: Int = 0,
    override val maxPositiveDensityOffset: Int = 0,
) : SearchStrategyOptions {

    companion object {

        @JvmStatic val DEFAULT = PointSearchStrategyOptions()
    }
}
