package nebulosa.guiding.internal

data class GuideStats(
    val timestamp: Long = 0L,
    val dx: Double = 0.0,
    val dy: Double = 0.0,
    val ra: Double = 0.0,
    val dec: Double = 0.0,
    // val starSNR: Double = 0.0,
    // val starMass: Double = 0.0,
    val raDuration: Int = 0,
    val decDuration: Int = 0,
    val raDirection: GuideDirection? = null,
    val decDirection: GuideDirection? = null,
    val rmsRA: Double = 0.0,
    val rmsDEC: Double = 0.0,
    val peakRA: Double = 0.0,
    val peakDEC: Double = 0.0,
) {

    companion object {

        @JvmStatic val EMPTY = GuideStats()
    }
}
