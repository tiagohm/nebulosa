package nebulosa.guiding.internal

class HighPassFilter(
    val alphaCutoff: Double = 1.0,
    val count: Double = 0.0,
    val prevVal: Double = 0.0,
    val hpfResult: Double = 0.0,
)
