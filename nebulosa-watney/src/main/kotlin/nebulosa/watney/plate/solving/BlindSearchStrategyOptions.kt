package nebulosa.watney.plate.solving

import nebulosa.constants.DEG2RAD
import nebulosa.math.Angle

data class BlindSearchStrategyOptions(
    val startRadius: Angle = DEFAULT_START_RADIUS,
    val minRadius: Angle = DEFAULT_MIN_RADIUS,
    val searchOrderRA: RaSearchOrder = RaSearchOrder.EAST_FIRST,
    val searchOrderDEC: DecSearchOrder = DecSearchOrder.NORTH_FIRST,
    val maxNegativeDensityOffset: Int = 0,
    val maxPositiveDensityOffset: Int = 0,
) {

    enum class RaSearchOrder {
        EAST_FIRST,
        WEST_FIRST,
    }

    enum class DecSearchOrder {
        NORTH_FIRST,
        SOUTH_FIRST,
    }

    companion object {

        const val DEFAULT_START_RADIUS = 22.5 * DEG2RAD
        const val DEFAULT_MIN_RADIUS = (22.5 / 32) * DEG2RAD

        @JvmStatic val DEFAULT = BlindSearchStrategyOptions()
    }
}
