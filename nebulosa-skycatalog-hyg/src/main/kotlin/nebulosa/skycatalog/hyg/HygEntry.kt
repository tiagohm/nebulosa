package nebulosa.skycatalog.hyg

import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.skycatalog.SpectralSkyObject

data class HygEntry(
    override val id: Long = 0L,
    override val name: String = "",
    override val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    override val rightAscensionJ2000: Angle = 0.0,
    override val declinationJ2000: Angle = 0.0,
    override val spType: String = "",
    override val pmRA: Angle = 0.0,
    override val pmDEC: Angle = 0.0,
    override val parallax: Angle = 0.0,
    override val radialVelocity: Velocity = 0.0,
    override val redshift: Double = 0.0,
    override val constellation: Constellation = Constellation.AND,
) : DeepSkyObject, SpectralSkyObject {

    @Transient override val type = SkyObjectType.STAR
}
