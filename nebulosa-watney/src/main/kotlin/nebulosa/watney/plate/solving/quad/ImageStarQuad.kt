package nebulosa.watney.plate.solving.quad

import nebulosa.star.detection.ImageStar

@Suppress("ArrayInDataClass")
data class ImageStarQuad(
    override val ratios: FloatArray,
    override val largestDistance: Float,
    override val midPointX: Float,
    override val midPointY: Float,
    override val stars: List<ImageStar> = emptyList(),
) : StarQuad {

    constructor(ratios: FloatArray, largestDistance: Float, stars: List<ImageStar> = emptyList())
            : this(
        ratios, largestDistance,
        (stars[0].x + stars[1].x + stars[2].x + stars[3].x) / 4f,
        (stars[0].y + stars[1].y + stars[2].y + stars[3].y) / 4f,
        stars
    )

    init {
        require(stars.size == 4) { "A quad has four stars" }
    }
}
