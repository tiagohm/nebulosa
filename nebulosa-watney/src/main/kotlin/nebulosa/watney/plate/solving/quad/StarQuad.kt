package nebulosa.watney.plate.solving.quad

import nebulosa.star.detection.ImageStar

/**
 * Represents a star quad (in database or in image).
 */
interface StarQuad {

    /**
     * The quad ratios (between stars).
     */
    val ratios: DoubleArray

    /**
     * The largest distance (degrees or pixels).
     */
    val largestDistance: Double

    /**
     * The X/RA mid point of the quad.
     */
    val midPointX: Double

    /**
     * The Y/DEC mid point of the quad.
     */
    val midPointY: Double

    /**
     * The stars that make up this quad.
     */
    val stars: List<ImageStar>
}
