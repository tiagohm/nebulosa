package nebulosa.watney.platesolver

import nebulosa.math.Angle

@Suppress("ArrayInDataClass")
data class SearchRun(
    val radius: Angle,
    val centerRA: Angle,
    val centerDEC: Angle,
    val densityOffsets: IntArray,
)
