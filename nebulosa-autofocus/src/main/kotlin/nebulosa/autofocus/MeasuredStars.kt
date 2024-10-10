package nebulosa.autofocus

data class MeasuredStars(
    @JvmField val count: Int,
    @JvmField val hfd: Double,
    @JvmField val stdDev: Double,
) {

    companion object {

        @JvmStatic val EMPTY = MeasuredStars(0, 0.0, 0.0)
    }
}
