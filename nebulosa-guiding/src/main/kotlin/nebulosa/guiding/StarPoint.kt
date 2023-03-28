package nebulosa.guiding

interface StarPoint : GuidePoint {

    val mass: Double

    val snr: Double

    val hfd: Double

    val peak: Double

    val wasFound: Boolean
}
