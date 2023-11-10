package nebulosa.imaging.algorithms.star.detection

interface DetectedImage {

    val x: Double

    val y: Double

    val mass: Double

    val snr: Double

    val hfd: Double

    val peak: Double
}
