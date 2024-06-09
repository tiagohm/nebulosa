package nebulosa.watney.platesolver

import nebulosa.math.Angle
import nebulosa.math.deg

data class NearbySearchStrategyOptions(
    val searchAreaRadius: Angle = 10.deg,
    val minFieldRadius: Angle = 0.0,
    val maxFieldRadius: Angle = 2.deg,
    val intermediateFieldRadiusSteps: Int = -1,
    override val maxNegativeDensityOffset: Int = 0,
    override val maxPositiveDensityOffset: Int = 0,
) : SearchStrategyOptions {

    init {
        require(maxFieldRadius > 0.0) { "maxFieldRadius <= 0: $maxFieldRadius" }
        require(maxFieldRadius >= minFieldRadius) { "maxFieldRadius < minFieldRadius: $maxFieldRadius < $minFieldRadius" }
    }

    companion object {

        @JvmStatic val DEFAULT = NearbySearchStrategyOptions()
    }
}
