package nebulosa.api.data.responses

import nebulosa.api.data.entities.DeepSkyObjectEntity
import nebulosa.api.data.entities.StarEntity

data class ImageAnnotationResponse(
    val x: Double,
    val y: Double,
    val star: StarEntity? = null,
    val dso: DeepSkyObjectEntity? = null,
)
