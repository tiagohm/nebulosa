package nebulosa.api.image

import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

data class ImageInfo(
    val path: Path,
    val width: Int,
    val height: Int,
    val mono: Boolean,
    val stretchShadow: Float,
    val stretchHighlight: Float,
    val stretchMidtone: Float,
    val rightAscension: String?,
    val declination: String?,
    val solved: Boolean,
    val headers: List<ImageHeaderItem>,
    val camera: Camera?,
)
