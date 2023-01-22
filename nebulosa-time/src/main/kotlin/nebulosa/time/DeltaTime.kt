package nebulosa.time

interface DeltaTime {

    /**
     * Computes de ΔT in seconds at [time].
     */
    fun delta(time: InstantOfTime): Double

    companion object {

        @Suppress("NOTHING_TO_INLINE")
        @JvmStatic
        inline fun offset(offset: Double) = SingleSpline(doubleArrayOf(0.0, 1.0, offset))
    }
}
