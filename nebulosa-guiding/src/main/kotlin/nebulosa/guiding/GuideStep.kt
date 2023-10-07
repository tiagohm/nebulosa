package nebulosa.guiding

interface GuideStep {

    val frame: Int

    val starMass: Double

    val snr: Double

    val hfd: Double

    val dx: Double

    val dy: Double

    val raDistance: Double

    val decDistance: Double

    val raDistanceGuide: Double

    val decDistanceGuide: Double

    val raDuration: Long

    val raDirection: GuideDirection

    val decDuration: Long

    val decDirection: GuideDirection

    val averageDistance: Double
}
