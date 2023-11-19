package nebulosa.star.detection

data class DetectedStar(
    val x: Double, val y: Double,
    val hfd: Double, val snr: Double, val flux: Double,
)
