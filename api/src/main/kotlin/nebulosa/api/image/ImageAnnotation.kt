package nebulosa.api.image

import nebulosa.api.atlas.DeepSkyObjectEntity
import nebulosa.api.atlas.StarEntity
import nebulosa.math.Angle
import nebulosa.skycatalog.SkyObject

data class ImageAnnotation(
    val x: Double,
    val y: Double,
    val star: StarEntity? = null,
    val dso: DeepSkyObjectEntity? = null,
    val minorPlanet: SkyObject? = null,
) {

    internal data class MinorPlanet(
        override val id: Long = 0L,
        override val name: String = "",
        override val rightAscensionJ2000: Angle = 0.0,
        override val declinationJ2000: Angle = 0.0,
        override val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    ) : SkyObject
}
