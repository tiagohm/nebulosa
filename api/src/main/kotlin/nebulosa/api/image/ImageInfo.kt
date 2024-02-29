package nebulosa.api.image

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.beans.converters.angle.DeclinationSerializer
import nebulosa.api.beans.converters.angle.RightAscensionSerializer
import nebulosa.imaging.algorithms.computation.Statistics
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

data class ImageInfo(
    val path: Path,
    val width: Int, val height: Int, val mono: Boolean,
    val stretchShadow: Float = 0.0f, val stretchHighlight: Float = 1.0f, val stretchMidtone: Float = 0.5f,
    @field:JsonSerialize(using = RightAscensionSerializer::class) val rightAscension: Double? = null,
    @field:JsonSerialize(using = DeclinationSerializer::class) val declination: Double? = null,
    val solved: ImageSolved? = null,
    val headers: List<ImageHeaderItem> = emptyList(),
    val camera: Camera? = null,
    @JsonIgnoreProperties("histogram") val statistics: Statistics.Data? = null,
)
