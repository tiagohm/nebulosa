package nebulosa.watney.platesolver

import nebulosa.math.Angle
import nebulosa.math.deg

data class BlindSearchStrategyOptions(
    val startRadius: Angle = 22.5.deg,
    val minRadius: Angle = (22.5 / 32).deg,
    val searchOrderRA: RaSearchOrder = RaSearchOrder.EAST_FIRST,
    val searchOrderDEC: DecSearchOrder = DecSearchOrder.NORTH_FIRST,
    override val maxNegativeDensityOffset: Int = 0,
    override val maxPositiveDensityOffset: Int = 0,
) : SearchStrategyOptions {

    enum class RaSearchOrder {
        EAST_FIRST,
        WEST_FIRST,
    }

    enum class DecSearchOrder {
        NORTH_FIRST,
        SOUTH_FIRST,
    }

    companion object {

        @JvmStatic val DEFAULT = BlindSearchStrategyOptions()
    }
}
