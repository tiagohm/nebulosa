package nebulosa.api.data.responses

import java.nio.file.Path

data class ImageInfoResponse(
    val path: Path?,
    val width: Int,
    val height: Int,
    val mono: Boolean,
    val stretchShadow: Float,
    val stretchHighlight: Float,
    val stretchMidtone: Float,
    val rightAscension: String?,
    val declination: String?,
    val calibrated: Boolean,
    val headers: List<FITSHeaderItemResponse>,
)
