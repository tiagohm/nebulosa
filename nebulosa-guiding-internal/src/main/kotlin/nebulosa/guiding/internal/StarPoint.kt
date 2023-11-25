package nebulosa.guiding.internal

interface StarPoint : GuidePoint {

    val flux: Double

    val snr: Double

    val hfd: Double

    val wasFound: Boolean
}
