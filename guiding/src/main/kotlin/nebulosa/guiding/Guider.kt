package nebulosa.guiding

/**
 * It is responsible for dealing with new images as they arrive,
 * making move requests to a mount by passing the difference
 * between [currentPoint] and [lockPoint].
 */
sealed class Guider {

    /**
     * A [Point] that represents the position in the image.
     */
    abstract fun currentPoint(index: Int): Point

    /**
     * Arbitrary [Point] on an image used as the desired location
     * in the calculation which determines how far the image has moved.
     * The user can assist this process with mouse clicks,
     * or the [Guider] can select a suitable point automatically.
     */
    abstract fun lockPoint(index: Int): Point
}
