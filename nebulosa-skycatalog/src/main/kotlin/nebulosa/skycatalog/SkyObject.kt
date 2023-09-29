package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.time.UTC

interface SkyObject {

    val id: Long

    val name: String

    val magnitude: Double

    val rightAscensionJ2000: Angle

    val declinationJ2000: Angle

    val type: SkyObjectType

    val pmRA: Angle

    val pmDEC: Angle

    val parallax: Angle

    val radialVelocity: Velocity

    val redshift: Double

    // TODO: Use Distance type.
    // TODO: Compute from parallax or redshift if distance is not provided.
    // val distance: Double

    val constellation: Constellation

    companion object {

        const val UNKNOWN_MAGNITUDE = 99.0
        const val NAME_SEPARATOR = "|"

        @JvmStatic
        fun computeConstellation(rightAscension: Angle, declination: Angle, time: UTC): Constellation {
            return Constellation.find(ICRF.equatorial(rightAscension, declination, time = time))
        }
    }
}
