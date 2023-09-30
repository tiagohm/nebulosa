package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation

interface DeepSkyObject : SkyObject {

    val type: SkyObjectType

    val pmRA: Angle

    val pmDEC: Angle

    val parallax: Angle

    val radialVelocity: Velocity

    val redshift: Double

    val constellation: Constellation
}
