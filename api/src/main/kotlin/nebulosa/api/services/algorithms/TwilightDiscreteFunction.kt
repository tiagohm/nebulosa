package nebulosa.api.services.algorithms

import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.almanac.DiscreteFunction

data class TwilightDiscreteFunction(private val ephemeris: List<HorizonsElement>) : DiscreteFunction {

    private val cached = DoubleArray(ephemeris.size) { Double.NaN }

    override val stepSize = 1.0

    override fun compute(x: Double): Int {
        val index = x.toInt()

        val altitude = if (cached[index].isNaN()) ephemeris[index].asDouble(HorizonsQuantity.APPARENT_ALT)
        else cached[index]

        cached[index] = altitude

        return when {
            altitude <= ASTRONOMICAL_TWILIGHT -> 1 // Night.
            altitude <= NAUTICAL_TWILIGHT -> 2 // Astronomical.
            altitude <= CIVIL_TWILIGHT -> 3 // Nautical.
            altitude <= 0.0 -> 4 // Civil.
            else -> 0 // Day.
        }
    }

    companion object {

        const val ASTRONOMICAL_TWILIGHT = -18.0
        const val NAUTICAL_TWILIGHT = -12.0
        const val CIVIL_TWILIGHT = -6.0
    }
}
