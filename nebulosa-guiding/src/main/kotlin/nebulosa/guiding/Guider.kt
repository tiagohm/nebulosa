package nebulosa.guiding

import nebulosa.imaging.Image

/**
 * It is responsible for dealing with new images as they arrive,
 * making move requests to a mount by passing the difference
 * between [currentPosition] and [lockPosition].
 */
sealed class Guider {

    protected val listeners = ArrayList<GuiderListener>(1)
    protected var state = GuiderState.UNINITIALIZED

    protected val lockPosition = ShiftPoint(Float.NaN, Float.NaN)

    abstract val currentPosition: Star

    abstract val currentImage: Image?

    abstract fun setCurrentPosition(image: Image, position: Point): Boolean

    fun setLockPosition(position: Point) {
        if (position.x != lockPosition.x ||
            position.y != lockPosition.y
        ) {
            listeners.forEach { it.onLockPositionChanged(this, position) }

            if (state == GuiderState.GUIDING) {
                listeners.forEach { it.onGuidingDithered(this, position.x - lockPosition.x, position.y - lockPosition.y, false) }
            }
        }

        lockPosition.updateXY(position.x, position.y)
    }

    fun setLockPositionToStarAtPosition(starPosHint: Point) {
        if (setCurrentPosition(currentImage!!, starPosHint) && currentPosition.isValid) {
            setLockPosition(currentPosition)
        }
    }
}
