package nebulosa.guiding.internal

interface StarPoint : GuidePoint {

    val flux: Float

    val snr: Float

    val hfd: Float

    val wasFound: Boolean
}
