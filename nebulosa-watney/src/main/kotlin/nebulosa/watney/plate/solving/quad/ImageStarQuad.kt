package nebulosa.watney.plate.solving.quad

import nebulosa.star.detection.ImageStar

@Suppress("ArrayInDataClass")
data class ImageStarQuad(
    override val ratios: DoubleArray,
    override val largestDistance: Double,
    override val midPointX: Double,
    override val midPointY: Double,
    override val stars: List<ImageStar> = emptyList(),
) : StarQuad {

    constructor(ratios: DoubleArray, largestDistance: Double, stars: List<ImageStar> = emptyList())
            : this(
        ratios, largestDistance,
        (stars[0].x + stars[1].x + stars[2].x + stars[3].x) / 4.0,
        (stars[0].y + stars[1].y + stars[2].y + stars[3].y) / 4.0,
        stars
    )

    init {
        require(stars.size == 4) { "A quad has four stars" }
    }
}
