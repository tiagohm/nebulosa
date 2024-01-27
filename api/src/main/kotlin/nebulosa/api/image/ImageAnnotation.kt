package nebulosa.api.image

import nebulosa.math.Angle
import nebulosa.math.Point2D
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.SkyObject

data class ImageAnnotation(
    override val x: Double,
    override val y: Double,
    val star: DeepSkyObject? = null,
    val dso: DeepSkyObject? = null,
    val minorPlanet: SkyObject? = null,
) : Point2D {

    internal data class MinorPlanet(
        override val id: Long = 0L,
        override val name: String = "",
        override val rightAscensionJ2000: Angle = 0.0,
        override val declinationJ2000: Angle = 0.0,
        override val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    ) : SkyObject
}
