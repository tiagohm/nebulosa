package nebulosa.watney.plate.solving

import nebulosa.math.Angle
import nebulosa.math.deg

data class NearbySearchStrategyOptions(
    val searchAreaRadius: Angle = 10.deg,
    val maxFieldRadius: Angle = 2.deg,
    val minFieldRadius: Angle = 0.0,
    val intermediateFieldRadiusSteps: Int = -1,
    override val maxNegativeDensityOffset: Int = 0,
    override val maxPositiveDensityOffset: Int = 0,
) : SearchStrategyOptions {

    companion object {

        @JvmStatic val DEFAULT = NearbySearchStrategyOptions()
    }
}
