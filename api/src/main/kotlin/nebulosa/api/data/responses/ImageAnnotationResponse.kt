package nebulosa.api.data.responses

import nebulosa.api.atlas.DeepSkyObjectEntity
import nebulosa.api.atlas.StarEntity

data class ImageAnnotationResponse(
    val x: Double,
    val y: Double,
    val star: StarEntity? = null,
    val dso: DeepSkyObjectEntity? = null,
    val minorPlanet: MinorPlanet? = null,
) {

    data class MinorPlanet(
        val name: String,
        val rightAscension: String,
        val declination: String,
        val magnitude: String,
    )
}
