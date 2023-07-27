package nebulosa.api.data.responses

data class ImageInfoResponse(
    val camera: String,
    val path: String,
    val savedAt: Long,
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
