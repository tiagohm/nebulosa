package nebulosa.guiding.internal

data class StarDisplacement(
    val deltaTime: Double,
    val starPos: Double,
    val guided: Boolean = false,
    val reversal: Boolean = false,
) {

    companion object {

        @JvmStatic val ZERO = StarDisplacement(0.0, 0.0)
    }
}
