package nebulosa.api.data.responses

data class GuidingStarResponse(
    val image: String,
    val lockPositionX: Double,
    val lockPositionY: Double,
    val primaryStarX: Double,
    val primaryStarY: Double,
    val peak: Double,
    val fwhm: Float,
    val hfd: Double,
    val snr: Double,
)
