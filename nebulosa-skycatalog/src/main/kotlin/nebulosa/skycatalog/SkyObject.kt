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

        const val UNKNOWN_MAGNITUDE = 99.0
        const val NAME_SEPARATOR = "|"

        @JvmStatic
        fun computeConstellation(rightAscension: Angle, declination: Angle, time: UTC): Constellation {
            return Constellation.find(ICRF.equatorial(rightAscension, declination, time = time))
        }
    }
}
