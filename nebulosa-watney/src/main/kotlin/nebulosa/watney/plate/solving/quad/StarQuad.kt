package nebulosa.watney.plate.solving.quad

import nebulosa.star.detection.ImageStar

/**
 * Represents a star quad (in database or in image).
 */
interface StarQuad {

    /**
     * The quad ratios (between stars).
     */
    val ratios: FloatArray

    /**
     * The largest distance (degrees or pixels).
     */
    val largestDistance: Float

    /**
     * The X/RA mid point of the quad.
     */
    val midPointX: Float

    /**
     * The Y/DEC mid point of the quad.
     */
    val midPointY: Float

    /**
     * The stars that make up this quad.
     */
    val stars: List<ImageStar>

    class StarBasedEqualityKey(@JvmField val quad: StarQuad) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StarBasedEqualityKey) return false
            if (quad === other.quad) return true
            return quad.stars.containsAll(other.quad.stars)
        }

        override fun hashCode(): Int {
            return quad.stars[0].hashCode() xor quad.stars[1].hashCode() xor
                    quad.stars[2].hashCode() xor quad.stars[3].hashCode()
        }
    }

    class RatioBasedEqualityKey(@JvmField val quad: StarQuad) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is RatioBasedEqualityKey) return false
            if (quad === other.quad) return true
            if (quad.largestDistance != other.quad.largestDistance) return false

            for (r in 0..4) {
                if (quad.ratios[r] != other.quad.ratios[r]) return false
            }

            return true
        }

        override fun hashCode(): Int {
            return quad.ratios[0].hashCode() xor quad.ratios[1].hashCode() xor
                    quad.ratios[2].hashCode() xor quad.ratios[3].hashCode() xor
                    quad.ratios[4].hashCode() xor quad.largestDistance.hashCode()
        }
    }
}
