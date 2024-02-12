package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.ONE_PARSEC
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.time.InstantOfTime

interface SkyObject {

    val id: Long

    val name: String

    val magnitude: Double

    val rightAscensionJ2000: Angle

    val declinationJ2000: Angle

    companion object {

        const val UNKNOWN_MAGNITUDE = 30.0
        const val MAGNITUDE_MIN = -UNKNOWN_MAGNITUDE
        const val MAGNITUDE_MAX = UNKNOWN_MAGNITUDE
        const val NAME_SEPARATOR = "|"

        @JvmStatic val MAGNITUDE_RANGE = MAGNITUDE_MIN..MAGNITUDE_MAX

        @JvmStatic
        fun constellationFor(icrf: ICRF): Constellation {
            return Constellation.find(icrf)
        }

        @JvmStatic
        fun constellationFor(rightAscension: Angle, declination: Angle, epoch: InstantOfTime? = null): Constellation {
            return constellationFor(ICRF.equatorial(rightAscension, declination, epoch = epoch))
        }

        @JvmStatic
        fun distanceFor(parallaxInMas: Double): Distance {
            return if (parallaxInMas > 0.0) (1000.0 * ONE_PARSEC) / parallaxInMas else 0.0 // AU
        }
    }
}
