package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation

data class Star(
    override val id: Long = 0L,
    override val name: String = "",
    override val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    override val rightAscensionJ2000: Angle = Angle.ZERO,
    override val declinationJ2000: Angle = Angle.ZERO,
    override val type: SkyObjectType = SkyObjectType.STAR,
    override val spType: String = "",
    override val pmRA: Angle = Angle.ZERO,
    override val pmDEC: Angle = Angle.ZERO,
    override val parallax: Angle = Angle.ZERO,
    override val radialVelocity: Velocity = Velocity.ZERO,
    override val redshift: Double = 0.0,
    // override val distance: Double = 0.0,
    override val constellation: Constellation = Constellation.AND,
) : SkyObject, SpectralObject
