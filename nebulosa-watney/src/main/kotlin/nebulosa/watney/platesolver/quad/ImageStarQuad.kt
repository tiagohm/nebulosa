package nebulosa.watney.platesolver.quad

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

    class StarBasedEqualityKey(@JvmField val quad: ImageStarQuad) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StarBasedEqualityKey) return false
            if (quad === other.quad) return true
            // Disallow a quad definition that has same pixel coords than another one (this is so that equations
            // won't flip when we get two slightly different ra,dec coordinates representing the same pixel)
            if (quad.midPointX == other.quad.midPointX && quad.midPointY == other.quad.midPointY) return true
            return quad.stars.containsAll(other.quad.stars)
        }

        override fun hashCode(): Int {
            return quad.stars[0].hashCode() xor quad.stars[1].hashCode() xor
                    quad.stars[2].hashCode() xor quad.stars[3].hashCode() xor
                    quad.midPointX.hashCode() xor quad.midPointY.hashCode()
        }
    }
}
