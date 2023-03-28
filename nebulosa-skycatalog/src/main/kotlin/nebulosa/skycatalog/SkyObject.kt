package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import java.io.Serializable

interface SkyObject : Serializable {

    val id: Int

    val names: List<String>

    val mB: Double

    val mV: Double

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
}
