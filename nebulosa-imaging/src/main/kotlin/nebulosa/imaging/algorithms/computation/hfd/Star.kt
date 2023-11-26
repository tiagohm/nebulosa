package nebulosa.imaging.algorithms.computation.hfd

data class Star(
    @JvmField val x: Int, @JvmField val y: Int,
    @JvmField val hfd: Double = 0.0,
    @JvmField val snr: Double = 0.0,
    @JvmField val flux: Double = 0.0,
)
