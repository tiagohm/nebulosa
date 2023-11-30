package nebulosa.watney.plate.solving.quad

import nebulosa.star.detection.ImageStar

@Suppress("ArrayInDataClass")
data class CellStarQuad(
    override val ratios: DoubleArray,
    override val largestDistance: Double,
    override val midPointX: Double,
    override val midPointY: Double,
) : StarQuad {

    override val stars: List<ImageStar> = emptyList()
}
