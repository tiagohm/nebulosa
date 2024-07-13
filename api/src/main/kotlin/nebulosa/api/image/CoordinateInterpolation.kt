package nebulosa.api.image

import java.time.LocalDateTime

@Suppress("ArrayInDataClass")
data class CoordinateInterpolation(
    @JvmField val ma: DoubleArray,
    @JvmField val md: DoubleArray,
    @JvmField val x0: Int,
    @JvmField val y0: Int,
    @JvmField val x1: Int,
    @JvmField val y1: Int,
    @JvmField val delta: Int,
    @JvmField val date: LocalDateTime?,
)
