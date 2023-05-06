package nebulosa.skycatalog

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation

data class Star(
    override val id: Int = 0,
    override val names: String = "",
    val hr: Int = 0,
    val hd: Int = 0,
    val hip: Int = 0,
    val sao: Int = 0,
    override val magnitude: Double = Double.MAX_VALUE,
    override val rightAscension: Angle = Angle.ZERO,
    override val declination: Angle = Angle.ZERO,
    val spType: String? = null,
    override val redshift: Double = 0.0,
    override val parallax: Angle = Angle.ZERO,
    override val radialVelocity: Velocity = Velocity.ZERO,
    override val distance: Double = 0.0,
    override val pmRA: Angle = Angle.ZERO,
    override val pmDEC: Angle = Angle.ZERO,
    override val type: SkyObjectType = SkyObjectType.STAR,
    override val constellation: Constellation = Constellation.AND,
) : SkyObject
