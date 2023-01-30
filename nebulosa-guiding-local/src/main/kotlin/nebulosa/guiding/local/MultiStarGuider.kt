package nebulosa.guiding.local

import nebulosa.imaging.Image
import kotlin.math.abs
import kotlin.math.hypot

abstract class MultiStarGuider : Guider() {

    private var primaryStar = Star(0f, 0f)
    private val guideStars = ArrayList<Star>(0)

    private var starFoundTimestamp = 0L  // timestamp when star was last found
    private var avgDistance = 0f   // averaged distance for distance reporting
    private var avgDistanceRA = 0f   // averaged distance, RA only
    private var avgDistanceLong = 0f   // averaged distance, more smoothed
    private var avgDistanceLongRA = 0f   // averaged distance, more smoothed, RA only
    private var avgDistanceCnt = 0
    private var avgDistanceNeedReset = false

    var isMultiStar = false
    var isStabilizing = false
    var isLockPositionMoved = false

    var isMassChangeThresholdEnabled = false
    var massChangeThreshold = 0f
    var isTolerateJumpsEnabled = false
    var tolerateJumpsThreshold = 0f
    var maxStars = 0
    var stabilitySigmaX = 0f

    var isGuidingRAOnly = false

    var searchRegion = 15f
    var minStarHFD = 1.5f

    var calibration = Calibration.EMPTY
    var yAngleError = 0f

    override val currentPosition get() = primaryStar

    @Volatile override var currentImage: Image? = null
        protected set

    fun registerListener(listener: GuiderListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: GuiderListener) {
        listeners.remove(listener)
    }

    fun selectGuideStar(
        image: Image,
        x: Float, y: Float,
    ) {
        require(x > searchRegion && x + searchRegion < image.width) { "outside of search region" }
        require(y > searchRegion && y + searchRegion < image.height) { "outside of search region" }
        require(state.ordinal <= GuiderState.SELECTED.ordinal) { "state > SELECTED" }

        this.currentImage = image

        if (setCurrentPosition(image, Point(x, y))
            && primaryStar.isValid
        ) {
            setLockPosition(primaryStar)

            if (guideStars.size > 1) {
                guideStars.clear()
            }

            if (guideStars.isEmpty()) {
                guideStars.add(primaryStar)
            }

            // On callback the START PROFILE calls UpdateData
            listeners.forEach { it.onStarSelected(this, primaryStar) }

            state = GuiderState.SELECTED
        } else {
            throw IllegalArgumentException("no star selected at position: $x, $y")
        }
    }

    override fun setCurrentPosition(
        image: Image,
        position: Point,
    ): Boolean {
        // m_massChecker->Reset()

        return primaryStar
            .find(image, searchRegion, position.x, position.y, minHFD = minStarHFD)
    }

    fun updateCurrentPosition(
        image: Image,
        offset: GuiderOffset,
    ) {
        if (!primaryStar.isValid && primaryStar.x == 0f && primaryStar.y == 0f) {
            throw IllegalStateException("no star selected")
        }

        val newStar = Star(primaryStar)

        if (!newStar.find(image, searchRegion, minHFD = minStarHFD)) {
            throw IllegalArgumentException("not found")
        }

        // check to see if it seems like the star we just found was the
        // same as the original star by comparing the mass.
        if (isMassChangeThresholdEnabled) {

        }

        var distance = if (lockPosition.isValid) {
            if (isGuidingRAOnly) {
                abs(newStar.x - lockPosition.x)
            } else {
                newStar.distance(lockPosition)
            }
        } else {
            0f
        }

        val tolerance = if (isTolerateJumpsEnabled) tolerateJumpsThreshold else Float.MAX_VALUE

        primaryStar = newStar

        if (lockPosition.isValid) {
            offset.camera = primaryStar - lockPosition

            if (isMultiStar && guideStars.size > 1) {
                if (refineOffset(image, offset)) {
                    distance = hypot(offset.camera.x, offset.camera.y)
                }
            } else {
                starsUsed = 1
            }

            transformCameraCoordinatesToMountCoordinates(offset.camera, offset.mount, calibration, yAngleError)

            val distanceRA = if (offset.mount.isValid) abs(offset.mount.x) else 0f
            updateCurrentDistance(distance, distanceRA)
        }
    }

    abstract val isGuiding: Boolean

    abstract val isSettling: Boolean

    private fun updateCurrentDistance(distance: Float, distanceRA: Float) {
        // starFoundTimestamp = System.currentTimeMillis()

        if (isGuiding) {
            avgDistance += 0.3f * (distance - avgDistance)
            avgDistanceRA += 0.3f * (distanceRA - avgDistanceRA)

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

    private fun refineOffset(image: Image, offset: GuiderOffset) {
        var origOffset = offset

        if (isGuiding && guideStars.size > 1 && !isSettling) {
            val sumWeights = 1.0
            val sumX = origOffset.camera.x
            val sumY = origOffset.camera.y
            primaryDistance = hypot(sumX, sumY)
        }
    }

    private fun currentError(avgDist: Float): Float {
        if (starFoundTimestamp == 0L) return 100f
        if (System.currentTimeMillis() - starFoundTimestamp > 20000L) return 100f
        return avgDist
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun currentError(raOnly: Boolean): Float {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceRA else avgDistance)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun currentErrorSmoothed(raOnly: Boolean): Float {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceLongRA else avgDistanceLong)
    }

    companion object {

        @JvmStatic
        private fun currentError(starFoundTimestamp: Long, avgDist: Float): Float {
            if (starFoundTimestamp == 0L) return 100f
            if (System.currentTimeMillis() - starFoundTimestamp > 20000L) return 100f
            return avgDist
        }

        @JvmStatic
        private fun transformCameraCoordinatesToMountCoordinates(
            camera: Point, mount: Point,
            calibration: Calibration,
            yAngleError: Float,
        ) {
            val hyp = camera.distance
            val cameraTheta = camera.angle
            val xAngle = cameraTheta - calibration.xAngle
            val yAngle = cameraTheta - (calibration.xAngle + yAngleError)

            mount.x = (xAngle.cos * hyp).toFloat()
            mount.y = (yAngle.sin * hyp).toFloat()
        }
    }
}
