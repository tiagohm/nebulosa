package nebulosa.simbad

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.*

data class SimbadEntry(
    override var id: Long = 0L,
    override var name: String = "",
    override var magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    override var rightAscensionJ2000: Angle = 0.0,
    override var declinationJ2000: Angle = 0.0,
    override var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    override val spType: String = "",
    override var majorAxis: Angle = 0.0,
    override var minorAxis: Angle = 0.0,
    override var orientation: Angle = 0.0,
    override var pmRA: Angle = 0.0,
    override var pmDEC: Angle = 0.0,
    override var parallax: Angle = 0.0,
    override var radialVelocity: Velocity = 0.0,
    override var redshift: Double = 0.0,
    var distance: Distance = 0.0,
    override var constellation: Constellation = Constellation.AND,
) : DeepSkyObject, SpectralSkyObject, OrientedSkyObject
