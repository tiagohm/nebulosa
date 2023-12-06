package nebulosa.watney.plate.solving

import nebulosa.math.Angle

@Suppress("ArrayInDataClass")
data class SearchRun(
    val radius: Angle,
    val centerRA: Angle,
    val centerDEC: Angle,
    val densityOffsets: IntArray,
)
