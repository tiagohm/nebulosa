package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation

interface SkyObject {

    val id: Int

    val names: String

    val magnitude: Double

    val rightAscension: Angle

    val declination: Angle

    val type: SkyObjectType

    val redshift: Double

    val parallax: Angle

    val radialVelocity: Velocity

    // TODO: Use Distance type.
    // TODO: Compute from parallax or redshift if distance is not provided.
    val distance: Double

    val pmRA: Angle

    val pmDEC: Angle

    val constellation: Constellation

    companion object {

        const val UNKNOWN_MAGNITUDE = 99.0
        const val NAME_SEPARATOR = " · "
    }
}
