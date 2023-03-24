package nebulosa.guiding.local

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.star.hfd.FindMode
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

/**
 * It is responsible for dealing with new images as they arrive,
 * making move requests to a mount by passing the difference
 * between [currentPosition] and [lockPosition].
 */
class Guider {

    private val guideStars = LinkedList<GuideStar>()
    private var starsUsed = 0
    private var lastStarsUsed = 0

    private val massChecker = MassChecker()
    private val primaryDistStats = DescriptiveStats()
    private val distanceChecker = DistanceChecker(this)

    private val listeners = ArrayList<GuiderListener>(1)
    private var state = GuiderState.UNINITIALIZED

    private var starFoundTimestamp = 0L
    private var avgDistance = 0.0   // averaged distance for distance reporting
    private var avgDistanceRA = 0.0   // averaged distance, RA only
    private var avgDistanceLong = 0.0   // averaged distance, more smoothed
    private var avgDistanceLongRA = 0.0   // averaged distance, more smoothed, RA only
    private var avgDistanceCnt = 0
    private var avgDistanceNeedReset = false
    private val lockPositionShift = LockPositionShiftParams()
    private var ignoreLostStarLooping = false
    private var primaryStar = GuideStar(0.0, 0.0)
    private var forceFullFrame = false
    private var ditherRecenterStep = Point()
    private var ditherRecenterRemaining = Point()
    private val ditherRecenterDir = DoubleArray(2)
    private var measurementMode = false

    internal val frame = AtomicReference<Frame>()
    internal val guideMount = AtomicReference<GuideMount>()
    internal val guideCamera = AtomicReference<GuideCamera>()

    private val settler = Settler(this)

    internal val image
        get() = frame.get()?.image

    internal val frameNumber
        get() = frame.get()?.frameNumber ?: -1

    internal val mount
        get() = guideMount.get()

    internal val camera
        get() = guideCamera.get()

    val settling
        get() = settler.settling

    private val capturer = GuideCameraCapturer(this)

    private val lockPosition = ShiftPoint(Double.NaN, Double.NaN)

    var lastPrimaryDistance = 0
    var stabilizing = false
    var lockPositionMoved = false

    var tolerateJumpsEnabled = false
    var tolerateJumpsThreshold = 0.0
    var maxStars = DEFAULT_MAX_STAR_COUNT
    var stabilitySigmaX = DEFAULT_STABILITY_SIGMAX

    var minStarHFD = 1.5

    var calibration = Calibration.EMPTY
    var yAngleError = 0.0

    val currentPosition
        get() = primaryStar

    var massChangeThresholdEnabled = false

    internal var pauseType = PauseType.NONE

    val guiding
        get() = state == GuiderState.GUIDING

    val paused
        get() = pauseType != PauseType.NONE

    private val lockPositionShiftEnabled
        get() = lockPositionShift.shiftEnabled

    val currentErrorFrameCount
        get() = avgDistanceCnt

    var massChangeThreshold = 0.0
        set(value) {
            require(value >= 0.0) { "massChangeThreshold < 0" }
            field = value
        }

    var multiStar = false
        set(value) {
            val prevMultiStar = field
            var autoFindForced = false
            field = value

            if (value != prevMultiStar) {
                primaryDistStats.clear()

                if (state >= GuiderState.SELECTED) {
                    stopGuiding()
                    invalidateCurrentPosition(true)

                    if (!autoSelect()) {
                        startGuiding()
                        autoFindForced = true
                    }
                }
            }

            if (!value) stabilizing = false

            // TODO: NotifyGuidingParam(MultiStar, value)
        }

    var searchRegion = 15.0
        set(value) {
            require(searchRegion >= MIN_SEARCH_REGION) { "searchRegion < $MIN_SEARCH_REGION" }
            require(searchRegion <= MAX_SEARCH_REGION) { "searchRegion > $MAX_SEARCH_REGION" }
            field = value
        }

    var loopingDuration = 1000L


    fun registerListener(listener: GuiderListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: GuiderListener) {
        listeners.remove(listener)
    }

    fun attach(guideCamera: GuideCamera) {
        this.guideCamera.set(guideCamera)
    }

    fun lockPosition(position: Point): Boolean {
        if (!position.valid) return false
        if (position.x < 0.0) return false
        if (position.x >= image!!.width) return false
        if (position.y < 0.0) return false
        if (position.y >= image!!.height) return false

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

        if (multiStar) {
            lockPositionMoved = true
            stabilizing = true
        }

        return true
    }

    fun lockPositionToStarAtPosition(starPosHint: Point) {
        if (currentPosition(image!!, starPosHint) && currentPosition.valid) {
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
        state = GuiderState.CALIBRATING
    }

    fun stopGuiding() {
        when (state) {
            GuiderState.UNINITIALIZED,
            GuiderState.SELECTING,
            GuiderState.STOP,
            GuiderState.SELECTED -> Unit
            GuiderState.CALIBRATING,
            GuiderState.CALIBRATED -> {
                listeners.forEach { it.onCalibrationFailed() }
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

    fun updateGuide(frame: Frame, stopping: Boolean) {
        this.frame.set(frame)

        if (stopping) return stopGuiding()

        if (lockPositionShiftEnabled && guiding) {
            if (!shiftLockPosition()) {
                listeners.forEach { it.onLockShiftLimitReached() }
                enableLockPositionShift(false)
            }
        }

        val offset = GuiderOffset(Point(0.0, 0.0), Point(0.0, 0.0))

        if (!updateCurrentPosition(frame.image, offset)) {
            when (state) {
                GuiderState.UNINITIALIZED,
                GuiderState.SELECTING -> {
                    listeners.forEach { it.onLooping(frameNumber, null) }
                }
                GuiderState.SELECTED -> {
                    // We had a current position and lost it.
                    listeners.forEach { it.onLooping(frameNumber, null) }

                    if (!ignoreLostStarLooping) {
                        state = GuiderState.UNINITIALIZED
                        listeners.forEach { it.onStarLost() }
                    }
                }
                GuiderState.CALIBRATING -> {
                    listeners.forEach { it.onStarLost() }
                }
                GuiderState.GUIDING -> {
                    listeners.forEach { it.onStarLost() }
                    // Allow guide algorithms to attempt dead reckoning.
                    // pFrame->SchedulePrimaryMove(pMount, GuiderOffset.ZERO, MOVEOPTS_DEDUCED_MOVE);
                }
                GuiderState.CALIBRATED,
                GuiderState.STOP -> Unit
            }
        } else {
            if (state.looping) {
                listeners.forEach { it.onLooping(frameNumber, primaryStar) }
            }

            // we have a star selected, so re-enable subframes.
            if (forceFullFrame) {
                forceFullFrame = false
            }

            // Skipping frame - guider is paused.
            if (paused) {
                if (state == GuiderState.GUIDING) {
                    // Allow guide algorithms to attempt dead reckoning.
                    // pFrame->SchedulePrimaryMove(pMount, GuiderOffset.ZERO, MOVEOPTS_DEDUCED_MOVE);
                }
            } else {
                fun calibrated() {
                    state = GuiderState.GUIDING

                    // Camera angle is known, so ok to calculate shift rate camera coords.
                    updateLockPositionShiftCameraCoords()
                }

                when (state) {
                    GuiderState.SELECTING -> {
                        lockPosition(currentPosition)
                        listeners.forEach { it.onStarSelected(this, currentPosition) }
                        state = GuiderState.SELECTED
                    }
                    GuiderState.SELECTED -> {
                        // StaticPaTool,PolarDriftTool -> UpdateState()
                    }
                    GuiderState.CALIBRATING -> {
                        if (!mount.calibrated) {
                            if (!mount.updateCalibrationState(currentPosition)) {
                                state = GuiderState.UNINITIALIZED
                                println("Calibration failed")
                                return
                            }

                            if (mount.calibrated) {
                                state = GuiderState.CALIBRATED
                            }
                        } else {
                            state = GuiderState.CALIBRATED
                        }

                        if (state == GuiderState.CALIBRATED) {
                            calibrated()
                        }
                    }
                    GuiderState.CALIBRATED -> {
                        calibrated()
                    }
                    GuiderState.GUIDING -> {
                        if (ditherRecenterRemaining.valid) {
                            // Fast recenter after dither taking large steps and bypassing guide algorithms
                            val step =
                                Point(min(ditherRecenterRemaining.x, ditherRecenterStep.x), min(ditherRecenterRemaining.y, ditherRecenterStep.y))

                            ditherRecenterRemaining -= step

                            if (ditherRecenterRemaining.x < 0.5 && ditherRecenterRemaining.y < 0.5) {
                                // Fast recenter is done.
                                ditherRecenterRemaining.invalidate()
                                // Reset distance tracker.
                                avgDistanceNeedReset = true
                            }

                            offset.mount.set(step.x * ditherRecenterDir[0], step.y * ditherRecenterDir[1])
                            mount.transformMountCoordinatesToCameraCoordinates(offset.mount, offset.camera)
                            mount.moveOffset(offset, MountMoveOption.USE_BACKSLASH_COMPENSATION)
                            // Let guide algorithms know about the direct move.
                            mount.notifyDirectMove(offset.mount)
                        } else if (measurementMode) {
                            // GuidingAssistant::NotifyBacklashStep(CurrentPosition());
                        } else {
                            // mount.moveOffset(offset, MOVEOPTS_GUIDE_STEP)
                        }
                    }
                    GuiderState.UNINITIALIZED,
                    GuiderState.STOP -> Unit
                }
            }
        }
    }

    private fun updateLockPositionShiftCameraCoords() {
        if (!lockPositionShift.shiftRate.valid) {
            return lockPosition.disableShift()
        }

        val rate = Point()

        if (lockPositionShift.shiftIsMountCoords) {
            val raDecRates = lockPositionShift.shiftRate

            if (lockPositionShift.shiftUnit == ShiftUnit.ARCSEC) {
                val raParity = mount.raParity
                val decParity = mount.decParity

                if (raParity == GuideParity.ODD) {
                    raDecRates.x = -raDecRates.x
                }

                if (decParity == GuideParity.ODD) {
                    raDecRates.y = -raDecRates.y
                }

                val declination = mount.declination

                if (declination.value.isFinite()) {
                    raDecRates.x *= declination.cos
                }
            }

            mount.transformMountCoordinatesToCameraCoordinates(raDecRates, rate)
        } else {
            rate.set(lockPositionShift.shiftRate)
        }

        // Convert arc-seconds to pixels.
        if (lockPositionShift.shiftUnit == ShiftUnit.ARCSEC) {
            rate.set(rate.x / camera.pixelScale, rate.y / camera.pixelScale)
        }

        // Per hour to per second.
        rate.set(rate.x / 3600.0, rate.y / 3600.0)

        lockPosition.shiftRate(rate.x, rate.y)
    }

    fun clearSecondaryStars() {
        while (guideStars.size > 1) {
            guideStars.removeAt(1)
        }
    }

    fun tolerateJumps(enable: Boolean, threshold: Double) {
        tolerateJumpsEnabled = enable
        tolerateJumpsThreshold = threshold
    }

    fun currentPosition(image: Image, position: Point): Boolean {
        require(position.valid) { "position is invalid" }
        require(position.x > 0.0 && position.x < image.width) { "invalid x value" }
        require(position.y > 0.0 && position.y < image.height) { "invalid y value" }

        massChecker.reset()

        return primaryStar.find(image, searchRegion, position.x, position.y, FindMode.CENTROID, minStarHFD)
    }

    fun autoSelect(): Boolean {
        // TODO:
        return false
    }

    fun invalidateCurrentPosition(fullReset: Boolean) {
        primaryStar.invalidate()

        if (fullReset) {
            primaryStar.x = 0.0
            primaryStar.y = 0.0
        }
    }

    val starCount
        get() = guideStars.size

    /**
     * Uses secondary stars to refine Offset value if appropriate.
     * Returns of true means offset has been adjusted.
     */
    fun refineOffset(image: Image, offset: GuiderOffset): Boolean {
        var primarySigma = 0.0
        var averaged = false
        var validStars = 0
        val origOffset = offset.copy()
        var refined = false

        starsUsed = 1

        // Primary star is in position 0 of the list.
        if (guiding && starCount > 1 && mount.guidingEnabled && !settling) {
            var sumWeights = 1.0
            var sumX = origOffset.camera.x
            var sumY = origOffset.camera.y
            val primaryDistance = hypot(sumX, sumY)

            primaryDistStats.add(primaryDistance)

            if (primaryDistStats.count > 5) {
                primarySigma = primaryDistStats.sigma

                if (!stabilizing && primaryDistance > stabilitySigmaX * primarySigma) {
                    stabilizing = true
                } else if (stabilizing) {
                    if (primaryDistance <= 2 * primarySigma) {
                        stabilizing = false

                        if (lockPositionMoved) {
                            lockPositionMoved = false

                            val guideStarsIter = guideStars.listIterator(1)

                            while (guideStarsIter.hasNext()) {
                                val star = guideStarsIter.next()

                                val expectedLoc = primaryStar + star.offsetFromPrimary

                                val found = if (isValidSecondaryStarPosition(expectedLoc)) {
                                    star.find(image, searchRegion, expectedLoc.x, expectedLoc.y, FindMode.CENTROID, minStarHFD)
                                } else {
                                    star.find(image, searchRegion, star.x, star.y, FindMode.CENTROID, minStarHFD)
                                }

                                if (found) {
                                    star.referencePoint.set(star)
                                } else {
                                    guideStarsIter.remove()
                                }
                            }

                            return false
                        }
                    }
                }
            } else {
                stabilizing = true
            }

            if (!stabilizing && starCount > 1 && (sumX != 0.0 || sumY != 0.0)) {
                val guideStarsIter = guideStars.listIterator(1)

                while (guideStarsIter.hasNext()) {
                    if (starsUsed >= maxStars || starCount == 1) break

                    // "used" means "considered" for purposes of UI.
                    starsUsed++

                    val star = guideStarsIter.next()

                    if (star.find(image, searchRegion, star.x, star.y, FindMode.CENTROID, minStarHFD)) {
                        val dx = star.x - star.referencePoint.x
                        val dy = star.y - star.referencePoint.y

                        if (star.lostCount > 0) star.lostCount--

                        if (dx != 0.0 || dy != 0.0) {
                            // Handle zero-counting - suspect results of exactly zero movement
                            if (dx == 0.0 || dy == 0.0) star.zeroCount++
                            else if (star.zeroCount > 0) star.zeroCount--

                            if (star.zeroCount == 5) {
                                guideStarsIter.remove()
                                continue
                            }

                            // Handle suspicious excursions - counted as "misses".
                            val secondaryDistance = hypot(dx, dy)

                            if (secondaryDistance > 2.5 * primarySigma) {
                                if (++star.missCount > 10) {
                                    // Reset the reference point to wherever it is now.
                                    star.referencePoint.set(star)
                                    star.missCount = 0
                                }

                                continue
                            } else if (star.missCount > 0) {
                                star.missCount--
                            }

                            // At this point we have usable data from the secondary star
                            val wt = (star.snr / primaryStar.snr)
                            sumX += wt * dx
                            sumY += wt * dy
                            sumWeights += wt
                            averaged = true
                            validStars++
                        } else {
                            guideStarsIter.remove()
                        }
                    } else {
                        // Star not found in its search region.
                        if (++star.lostCount >= 3) {
                            guideStarsIter.remove()
                        }
                    }
                }

                if (averaged) {
                    sumX /= sumWeights
                    sumY /= sumWeights

                    // Apply average only if its smaller than single-star delta.
                    if (hypot(sumX, sumY) < primaryDistance) {
                        offset.camera.x = sumX
                        offset.camera.y = sumY
                        refined = true
                    }
                }
            }
        }

        return refined
    }

    // Shift + Left click.
    fun deselectGuideStar() {
        invalidateCurrentPosition(true)
    }

    fun selectGuideStar(
        x: Double, y: Double,
    ) {
        val image = frame.get()?.image ?: return

        require(state <= GuiderState.SELECTED) { "state > SELECTED" }
        require(x > searchRegion && x + searchRegion < image.width) { "outside of search region" }
        require(y > searchRegion && y + searchRegion < image.height) { "outside of search region" }

        if (currentPosition(image, Point(x, y))
            && primaryStar.valid
        ) {
            lockPosition(primaryStar)

            if (starCount > 1) {
                clearSecondaryStars()
            }

            if (starCount == 0) {
                guideStars.add(primaryStar)
            }

            listeners.forEach { it.onStarSelected(this, primaryStar) }

            state = GuiderState.SELECTED
        } else {
            throw IllegalArgumentException("no star selected at position: $x, $y")
        }
    }

    fun updateCurrentPosition(
        image: Image,
        offset: GuiderOffset,
    ): Boolean {
        if (!primaryStar.valid && primaryStar.x == 0.0 && primaryStar.y == 0.0) {
            return false
        }

        val newStar = GuideStar(primaryStar)

        if (!newStar.find(image, searchRegion, minHFD = minStarHFD)) {
            distanceChecker.activate()
            println("new star not found")
            return false
        }

        // check to see if it seems like the star we just found was the
        // same as the original star by comparing the mass.
        if (massChangeThresholdEnabled) {
            massChecker.exposure(camera.exposure, camera.autoExposure)

            val checkedMass = massChecker.checkMass(newStar.mass, massChangeThreshold)

            if (checkedMass.reject) {
                // Mass changed!
                massChecker.add(newStar.mass)
                distanceChecker.activate()

                println("mass changed")
                return false
            }
        }

        var distance = if (lockPosition.valid) {
            if (mount.guidingRAOnly) {
                abs(newStar.x - lockPosition.x)
            } else {
                newStar.distance(lockPosition)
            }
        } else {
            0.0
        }

        val tolerance = if (tolerateJumpsEnabled) tolerateJumpsThreshold else Double.MAX_VALUE

        if (!distanceChecker.checkDistance(distance, mount.guidingRAOnly, tolerance)) {
            println("distance changed")
            return false
        }

        primaryStar = newStar
        massChecker.add(newStar.mass)

        if (lockPosition.valid) {
            offset.camera.set(primaryStar - lockPosition)

            if (multiStar && starCount > 1) {
                if (refineOffset(image, offset)) {
                    distance = hypot(offset.camera.x, offset.camera.y)
                }
            } else {
                starsUsed = 1
            }

            if (mount.calibrated) {
                mount.transformCameraCoordinatesToMountCoordinates(offset.camera, offset.mount)
            }

            val distanceRA = if (offset.mount.valid) abs(offset.mount.x) else 0.0
            updateCurrentDistance(distance, distanceRA)
        }

        // pFrame->pProfile->UpdateData(pImage, m_primaryStar.X, m_primaryStar.Y)

        return true
    }

    fun isValidLockPosition(point: Point): Boolean {
        val image = image ?: return false

        val region = 1.0 + searchRegion
        return point.x >= region &&
                point.x + region < image.width &&
                point.y >= region &&
                point.y + region < image.height
    }

    fun isValidSecondaryStarPosition(point: Point): Boolean {
        val image = image ?: return false

        return point.x >= 5.0 &&
                point.x + 5.0 < image.width &&
                point.y >= 5.0 &&
                point.y + 5.0 < image.height
    }

    fun startLooping() {
        capturer.start()
    }

    fun currentError(raOnly: Boolean): Double {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceRA else avgDistance)
    }

    fun currentErrorSmoothed(raOnly: Boolean): Double {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceLongRA else avgDistanceLong)
    }

    companion object {

        const val MIN_SEARCH_REGION = 7f
        const val DEFAULT_SEARCH_REGION = 15f
        const val MAX_SEARCH_REGION = 50f
        const val DEFAULT_MAX_STAR_COUNT = 9
        const val DEFAULT_STABILITY_SIGMAX = 5f

        @JvmStatic
        private fun currentError(starFoundTimestamp: Long, avgDist: Double): Double {
            if (starFoundTimestamp == 0L) return 100.0
            if (System.currentTimeMillis() - starFoundTimestamp > 20000L) return 100.0
            return avgDist
        }

        @JvmStatic
        private fun transformCameraCoordinatesToMountCoordinates(
            camera: Point, mount: Point,
            calibration: Calibration,
            yAngleError: Double,
        ) {
            val hyp = camera.distance
            val cameraTheta = camera.angle
            val xAngle = cameraTheta - calibration.xAngle
            val yAngle = cameraTheta - (calibration.xAngle + yAngleError)

            mount.set(xAngle.cos * hyp, yAngle.sin * hyp)
        }
    }
}
