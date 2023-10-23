package nebulosa.api.image

import java.time.LocalDateTime

@Suppress("ArrayInDataClass")
data class CoordinateInterpolation(
    val ma: DoubleArray,
    val md: DoubleArray,
    val x0: Int, val y0: Int, val x1: Int, val y1: Int,
    val delta: Int, val date: LocalDateTime?,
)
