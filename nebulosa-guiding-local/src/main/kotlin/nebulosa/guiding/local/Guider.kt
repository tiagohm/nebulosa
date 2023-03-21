package nebulosa.guiding.local

import nebulosa.imaging.Image

/**
 * It is responsible for dealing with new images as they arrive,
 * making move requests to a mount by passing the difference
 * between [currentPosition] and [lockPosition].
 */
sealed class Guider {

    protected val listeners = ArrayList<GuiderListener>(1)
    protected var state = GuiderState.UNINITIALIZED

    protected var starFoundTimestamp = 0L
    protected var avgDistance = 0.0   // averaged distance for distance reporting
    protected var avgDistanceRA = 0.0   // averaged distance, RA only
    protected var avgDistanceLong = 0.0   // averaged distance, more smoothed
    protected var avgDistanceLongRA = 0.0   // averaged distance, more smoothed, RA only
    protected var avgDistanceCnt = 0
    protected var avgDistanceNeedReset = false
    protected var currentImage: Image? = null
    protected val lockPositionShift = LockPositionShiftParams()

    protected val lockPositionShiftEnabled
        get() = lockPositionShift.shiftEnabled

    protected val lockPosition = ShiftPoint(Double.NaN, Double.NaN)

    abstract val currentPosition: Star

    abstract fun currentPosition(image: Image, position: Point): Boolean

    abstract fun invalidateCurrentPosition(fullReset: Boolean)

    open fun lockPosition(position: Point): Boolean {
        if (!position.valid) return false
        if (position.x < 0.0) return false
        if (position.x >= currentImage!!.width) return false
        if (position.y < 0.0) return false
        if (position.y >= currentImage!!.height) return false

        if (!lockPosition.valid ||
            position.x != lockPosition.x ||
            position.y != lockPosition.y
        ) {
            listeners.forEach { it.onLockPositionChanged(this, position) }

            if (state == GuiderState.GUIDING) {
                listeners.forEach { it.onGuidingDithered(this, position.x - lockPosition.x, position.y - lockPosition.y, false) }
            }
        }

        lockPosition.set(position)

        return true
    }

    fun lockPositionToStarAtPosition(starPosHint: Point) {
        if (currentPosition(currentImage!!, starPosHint) && currentPosition.valid) {
            lockPosition(currentPosition)
        }
    }

    fun updateCurrentDistance(distance: Double, distanceRA: Double) {
        starFoundTimestamp = System.currentTimeMillis()

        if (guiding) {
            avgDistance += 0.3 * (distance - avgDistance)
            avgDistanceRA += 0.3 * (distanceRA - avgDistanceRA)

            avgDistanceCnt++

            if (avgDistanceCnt < 10) {
                // Initialize smoothed running avg with mean of first 10 pts.
                avgDistanceLong += (distance - avgDistanceLong) / avgDistanceCnt
                avgDistanceLongRA += (distance - avgDistanceLongRA) / avgDistanceCnt
            } else {
                avgDistanceLong += 0.045f * (distance - avgDistanceLong)
                avgDistanceLongRA += 0.045f * (distance - avgDistanceLongRA)
            }
        } else {
            // Not yet guiding, reinitialize average distance.
            avgDistance = distance
            avgDistanceLong = avgDistance
            avgDistanceRA = distanceRA
            avgDistanceLongRA = avgDistanceRA
            avgDistanceCnt = 1
        }

        if (avgDistanceNeedReset) {
            // avg distance history invalidated
            avgDistance = distance
            avgDistanceLong = avgDistance
            avgDistanceRA = distanceRA
            avgDistanceLongRA = avgDistanceRA

            avgDistanceCnt = 1
            avgDistanceNeedReset = false
        }
    }

    fun startGuiding() {
        // We set the state to calibrating. The state machine will
        // automatically move from calibrating > calibrated > guiding
        // when it can.
        state = GuiderState.CALIBRATING_PRIMARY
    }

    fun stopGuiding() {
        when (state) {
            GuiderState.UNINITIALIZED,
            GuiderState.SELECTING,
            GuiderState.STOP,
            GuiderState.SELECTED -> Unit
            GuiderState.CALIBRATING_PRIMARY,
            GuiderState.CALIBRATING_SECONDARY,
            GuiderState.CALIBRATED -> {
                val mount = if (state == GuiderState.CALIBRATING_SECONDARY) mount else mount
                listeners.forEach { it.onCalibrationFailed(mount) }
            }
            GuiderState.GUIDING -> {
                if (!mount.busy) listeners.forEach { it.onGuidingStopped() }
            }
        }

        state = GuiderState.STOP
    }

    fun reset(fullReset: Boolean) {
        state = GuiderState.UNINITIALIZED

        if (fullReset) invalidateCurrentPosition(true)
    }

    protected abstract fun isValidLockPosition(point: Point): Boolean

    fun shiftLockPosition(): Boolean {
        lockPosition.updateShift()
        return isValidLockPosition(lockPosition)
    }

    fun enableLockPositionShift(enable: Boolean) {
        if (enable != lockPositionShift.shiftEnabled) {
            lockPositionShift.shiftEnabled = enable

            if (enable) {
                lockPosition.beginShift()
            }
        }
    }

    protected abstract fun updateCurrentPosition(image: Image, offset: GuiderOffset): Boolean

    fun updateGuide(image: Image, stopping: Boolean) {
        currentImage = image

        if (stopping) return stopGuiding()

        if (lockPositionShiftEnabled && mount.guiding) {
            if (!shiftLockPosition()) {
                listeners.forEach { it.onLockShiftLimitReached() }
                enableLockPositionShift(false)
            }
        }

        val offset = GuiderOffset(Point(0.0, 0.0), Point(0.0, 0.0))

        if (!updateCurrentPosition(image, offset)) {

        }
    }

    fun currentError(raOnly: Boolean): Double {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceRA else avgDistance)
    }

    fun currentErrorSmoothed(raOnly: Boolean): Double {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceLongRA else avgDistanceLong)
    }

    companion object {

        @JvmStatic
        private fun currentError(starFoundTimestamp: Long, avgDist: Double): Double {
            if (starFoundTimestamp == 0L) return 100.0
            if (System.currentTimeMillis() - starFoundTimestamp > 20000L) return 100.0
            return avgDist
        }
    }
}
