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
}
