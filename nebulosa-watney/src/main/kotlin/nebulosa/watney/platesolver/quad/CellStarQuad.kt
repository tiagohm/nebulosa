package nebulosa.watney.platesolver.quad

import nebulosa.stardetector.StarPoint

@Suppress("ArrayInDataClass")
data class CellStarQuad(
    override val ratios: DoubleArray,
    override val largestDistance: Double,
    override val midPointX: Double,
    override val midPointY: Double,
) : StarQuad {

    override val stars: List<StarPoint> = emptyList()
}
