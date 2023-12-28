package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.time.UTC

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
        fun computeConstellation(rightAscension: Angle, declination: Angle, time: UTC): Constellation {
            return Constellation.find(ICRF.equatorial(rightAscension, declination, time = time))
        }
    }
}
