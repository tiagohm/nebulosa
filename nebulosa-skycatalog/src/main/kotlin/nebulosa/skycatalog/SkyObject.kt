package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.FixedStar
import java.io.Serializable

interface SkyObject : Serializable {

    val id: Int

    val names: List<String>

    val mB: Double  // B magnitude

    val mV: Double  // V magnitude

    val rightAscension: Angle // RA

    val declination: Angle // DEC

    val type: SkyObjectType

    val redshift: Double  // Redshift

    val parallax: Angle // Parallax

    val radialVelocity: Velocity // Radial velocity

    // TODO: Use Distance type.
    // TODO: Compute from parallax or redshift if distance is not provided.
    val distance: Double

    val pmRA: Angle

    val pmDEC: Angle

    val position: FixedStar
}
