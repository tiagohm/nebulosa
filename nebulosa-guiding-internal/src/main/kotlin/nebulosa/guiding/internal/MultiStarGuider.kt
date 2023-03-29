package nebulosa.guiding.internal

import nebulosa.constants.PIOVERTWO
import nebulosa.guiding.*
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.star.hfd.FindMode
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * It is responsible for dealing with new images as they arrive,
 * making move requests to a mount by passing the difference
 * between [currentPosition] and [lockPosition].
 */
class MultiStarGuider(
    @JvmField internal val device: GuideDevice,
    private val executor: ExecutorService,
) : Guider {

    private data class MoveResult(
        val status: Boolean,
        val amountMoved: Int,
        val limitReached: Boolean,
    ) {

        companion object {

            @JvmStatic val FAILED = MoveResult(false, 0, true)
        }
    }

    private val guideStars = LinkedList<GuideStar>()
    private var starsUsed = 0

    private val massChecker = MassChecker()
    private val primaryDistStats = DescriptiveStats()
    private val distanceChecker = DistanceChecker(this)

    internal val listeners = ArrayList<GuiderListener>(1)

    var state = GuiderState.UNINITIALIZED
        private set

    private var starFoundTimestamp = 0L
    private var avgDistance = 0.0
    private var avgDistanceRA = 0.0
    private var avgDistanceLong = 0.0
    private var avgDistanceLongRA = 0.0
    private var avgDistanceCnt = 0
    private var avgDistanceNeedReset = false
    private val lockPositionShift = LockPositionShiftParams()
    private var ditherRecenterStep = Point()
    private var ditherRecenterRemaining = Point()
    private val ditherRecenterDir = DoubleArray(2)
    private var measurementMode = false
    private var lockPositionIsSticky = false

    var fastRecenterEnabled = false

    private val frame = AtomicReference<Image>()

    private val settler = Settler(this)

    override val image: Image?
        get() = frame.get()

    val settling
        get() = settler.settling

    private val capturer = GuideCameraCapturer(this)

    override var primaryStar = GuideStar(0.0, 0.0)
        private set

    override val lockPosition = ShiftPoint(Double.NaN, Double.NaN)

    private var stabilizing = false
    private var lockPositionMoved = false

    private var tolerateJumpsEnabled = false
    private var tolerateJumpsThreshold = 0.0
    private var maxStars = DEFAULT_MAX_STAR_COUNT
    private var stabilitySigmaX = DEFAULT_STABILITY_SIGMAX

    private var minStarHFD = 1.5

    private val guideCalibrator = GuideCalibrator(this)
    private val guideGraph = GuideGraph(this, 100)

    private val currentPosition
        get() = primaryStar

    private var massChangeThresholdEnabled = false

    internal var pauseType = PauseType.NONE

    var forceFullFrame = false
    var ignoreLostStarLooping = false

    var noiseReductionMethod = NoiseReductionMethod.NONE

    val guiding
        get() = state == GuiderState.GUIDING

    val paused
        get() = pauseType != PauseType.NONE

    val lockPositionShiftEnabled
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
            field = value

            if (value != prevMultiStar) {
                primaryDistStats.clear()

                if (state >= GuiderState.SELECTED) {
                    stopGuiding()
                    invalidateCurrentPosition(true)

                    if (autoSelect()) {
                        startGuiding()
                    }
                }
            }

            if (!value) stabilizing = false
        }

    override var searchRegion = 15.0
        set(value) {
            require(searchRegion >= MIN_SEARCH_REGION) { "searchRegion < $MIN_SEARCH_REGION" }
            require(searchRegion <= MAX_SEARCH_REGION) { "searchRegion > $MAX_SEARCH_REGION" }
            field = value
        }

    init {
        updateState(GuiderState.UNINITIALIZED)
    }

    fun registerListener(listener: GuiderListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: GuiderListener) {
        listeners.remove(listener)
    }

    override fun clearCalibration() {
        guideCalibrator.clear()
    }

    override fun loadCalibration(calibration: Calibration) {
        guideCalibrator.load(calibration)
    }

    fun pause(type: PauseType) {
        LOG.info("pause. type={}", type)

        if (type == PauseType.FULL && type != pauseType) {
            LOG.info("resetting avg dist filter")
            avgDistanceNeedReset = true
        }

        pauseType = type
    }

    private fun lockPosition(position: Point): Boolean {
        if (!position.valid) return false
        if (position.x < 0.0) return false
        if (position.x >= image!!.width) return false
        if (position.y < 0.0) return false
        if (position.y >= image!!.height) return false

        if (!lockPosition.valid ||
            position.x != lockPosition.x ||
            position.y != lockPosition.y
        ) {
            listeners.forEach { it.onLockPositionChanged(position) }

            if (state == GuiderState.GUIDING) {
                listeners.forEach { it.onGuidingDithered(position.x - lockPosition.x, position.y - lockPosition.y, false) }
            }
        }

        LOG.info("setting lock position. x={}, y={}", position.x, position.y)

        lockPosition.set(position)

        if (multiStar) {
            LOG.info("stabilizing after lock position change")
            lockPositionMoved = true
            stabilizing = true
        }

        return true
    }

    private fun moveLockPosition(delta: Point): Boolean {
        val image = image ?: return false

        if (!delta.valid) return false

        var cameraDelta = Point()
        var mountDelta = Point()
        var dBest = 0.0

        for (q in 0..3) {
            val sx = 1 - (q and 1 shl 1)
            val sy = 1 - (q and 2)

            val tmpMount = Point(delta.x * sx, delta.y * sy)
            val tmpCamera = Point()

            if (transformMountCoordinatesToCameraCoordinates(tmpMount, tmpCamera)) {
                val tmpLockPosition = lockPosition + tmpCamera

                if (isValidLockPosition(tmpLockPosition)) {
                    cameraDelta = tmpCamera
                    mountDelta = tmpMount
                    break
                }

                LOG.info("dither produces an invalid lock position, try a variation")

                val d = min(tmpLockPosition.x, min(image.width - tmpLockPosition.x, min(tmpLockPosition.y, image.height - tmpLockPosition.y)))

                if (q == 0 || d > dBest) {
                    cameraDelta = tmpCamera
                    mountDelta = tmpMount
                    dBest = d
                }
            } else {
                return false
            }
        }

        val newLockPosition = lockPosition + cameraDelta

        return if (lockPosition(newLockPosition)) {
            // Update average distance right away so GetCurrentDistance
            // reflects the increased distance from the dither.
            val dist = cameraDelta.distance
            val distRA = abs(mountDelta.x)
            avgDistance += dist
            avgDistanceLong += dist
            avgDistanceRA += distRA
            avgDistanceLongRA += distRA

            if (fastRecenterEnabled) {
                ditherRecenterRemaining.set(abs(mountDelta.x), abs(mountDelta.y))
                ditherRecenterDir[0] = if (mountDelta.x < 0.0) 1.0 else -1.0
                ditherRecenterDir[1] = if (mountDelta.y < 0.0) 1.0 else -1.0
                // Make each step a bit less than the full search region distance to avoid losing the star.
                val f = (searchRegion * 0.7) / ditherRecenterRemaining.distance
                ditherRecenterStep.set(f * ditherRecenterRemaining.x, f * ditherRecenterRemaining.y)
            }

            true
        } else {
            false
        }
    }

    private fun updateState(newState: GuiderState) {
        LOG.info("changing from state {} to {}", state, newState)

        var requestedState = newState

        if (newState == GuiderState.STOP) {
            // We are going to stop looping exposures. We should put
            // ourselves into a good state to restart looping later.
            requestedState = when (state) {
                GuiderState.UNINITIALIZED,
                GuiderState.SELECTING,
                GuiderState.SELECTED -> state
                // Because we have done some moving here, we need to just start over...
                GuiderState.CALIBRATING -> GuiderState.UNINITIALIZED
                GuiderState.CALIBRATED,
                GuiderState.GUIDING -> GuiderState.SELECTED
                GuiderState.STOP -> newState
            }
        }

        if (requestedState.ordinal > state.ordinal + 1) {
            LOG.warn("cannot transition from {} to {}", state, requestedState)
            return
        }

        var nextState = requestedState

        when (requestedState) {
            GuiderState.UNINITIALIZED -> {
                invalidateLockPosition()
                invalidateCurrentPosition()
                nextState = GuiderState.SELECTING
            }
            GuiderState.CALIBRATING -> {
                if (!guideCalibrator.calibrated) {
                    if (!guideCalibrator.beginCalibration(currentPosition)) {
                        nextState = GuiderState.UNINITIALIZED
                        LOG.error("begin calibration failed")
                    } else {
                        listeners.forEach { it.onStartCalibration() }
                    }
                }
            }
            GuiderState.GUIDING -> {
                ditherRecenterRemaining.invalidate()

                guideCalibrator.adjustCalibrationForScopePointing()

                if (lockPosition.valid && lockPositionIsSticky) {
                    LOG.info("keeping sticky lock position")
                } else {
                    lockPosition(currentPosition)
                }
            }
            else -> Unit
        }

        if (nextState >= requestedState) {
            state = nextState
            LOG.info("new guider state. state={}", state)
        } else {
            LOG.warn("recurses new state. state={}", nextState)
            updateState(nextState)
        }
    }

    fun lockPositionToStarAtPosition(starPosHint: Point) {
        if (currentPosition(image!!, starPosHint) && currentPosition.valid) {
            lockPosition(currentPosition)
        }
    }

    private fun updateCurrentDistance(distance: Double, distanceRA: Double) {
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
            // Avg distance history invalidated.
            avgDistance = distance
            avgDistanceLong = avgDistance
            avgDistanceRA = distanceRA
            avgDistanceLongRA = avgDistanceRA

            avgDistanceCnt = 1
            avgDistanceNeedReset = false
        }
    }

    override fun startGuiding() {
        // We set the state to calibrating. The state machine will
        // automatically move from calibrating > calibrated > guiding
        // when it can.
        updateState(GuiderState.CALIBRATING)
    }

    override fun stopGuiding() {
        when (state) {
            GuiderState.CALIBRATING,
            GuiderState.CALIBRATED -> {
                listeners.forEach { it.onCalibrationFailed() }
            }
            GuiderState.GUIDING -> {
                listeners.forEach { it.onGuidingStopped() }
            }
            else -> Unit
        }

        updateState(GuiderState.STOP)
    }

    override fun reset(fullReset: Boolean) {
        updateState(GuiderState.UNINITIALIZED)

        if (fullReset) invalidateCurrentPosition(true)
    }

    private fun shiftLockPosition(): Boolean {
        lockPosition.updateShift()
        return isValidLockPosition(lockPosition)
    }

    private fun enableLockPositionShift(enable: Boolean) {
        if (enable != lockPositionShift.shiftEnabled) {
            lockPositionShift.shiftEnabled = enable

            if (enable) {
                lockPosition.beginShift()
            }
        }
    }

    @Synchronized
    internal fun updateGuide(frame: Image, frameNumber: Int, stopping: Boolean) {
        this.frame.set(frame)

        if (stopping) return stopGuiding()

        if (lockPositionShiftEnabled && guiding) {
            if (!shiftLockPosition()) {
                listeners.forEach { it.onLockShiftLimitReached() }
                enableLockPositionShift(false)
            }
        }

        val offset = GuiderOffset(Point(), Point())

        if (!updateCurrentPosition(frame, offset)) {
            when (state) {
                GuiderState.UNINITIALIZED,
                GuiderState.SELECTING -> {
                    listeners.forEach { it.onLooping(frame, frameNumber, null) }
                }
                GuiderState.SELECTED -> {
                    // We had a current position and lost it.
                    listeners.forEach { it.onLooping(frame, frameNumber, null) }

                    if (!ignoreLostStarLooping) {
                        updateState(GuiderState.UNINITIALIZED)
                        listeners.forEach { it.onStarLost() }
                    }
                }
                GuiderState.CALIBRATING -> {
                    listeners.forEach { it.onStarLost() }
                }
                GuiderState.GUIDING -> {
                    listeners.forEach { it.onStarLost() }
                    // Allow guide algorithms to attempt dead reckoning.
                    device.moveOffset(ZERO_OFFSET, DEDUCED_MOVE)
                }
                else -> Unit
            }
        } else {
            listeners.forEach { it.onLooping(frame, frameNumber, primaryStar) }

            // we have a star selected, so re-enable subframes.
            if (forceFullFrame) {
                forceFullFrame = false
            }

            // Skipping frame - guider is paused.
            if (paused) {
                if (state == GuiderState.GUIDING) {
                    // Allow guide algorithms to attempt dead reckoning.
                    device.moveOffset(ZERO_OFFSET, DEDUCED_MOVE)
                }
            } else {
                when (state) {
                    GuiderState.SELECTING -> {
                        lockPosition(currentPosition)
                        listeners.forEach { it.onStarSelected(currentPosition) }
                        updateState(GuiderState.SELECTED)
                    }
                    GuiderState.SELECTED -> {
                        // StaticPaTool,PolarDriftTool -> UpdateState()
                    }
                    GuiderState.CALIBRATING -> {
                        if (!guideCalibrator.calibrated) {
                            if (!guideCalibrator.updateCalibrationState(currentPosition)) {
                                updateState(GuiderState.UNINITIALIZED)
                                LOG.error("calibration failed")
                                return
                            }

                            if (guideCalibrator.calibrated) {
                                updateState(GuiderState.CALIBRATED)
                            }
                        } else {
                            updateState(GuiderState.CALIBRATED)
                        }

                        if (state == GuiderState.CALIBRATED) {
                            updateState(GuiderState.GUIDING)

                            // Camera angle is known, so ok to calculate shift rate camera coords.
                            updateLockPositionShiftCameraCoords()
                        }
                    }
                    GuiderState.CALIBRATED -> {
                        updateState(GuiderState.GUIDING)

                        // Camera angle is known, so ok to calculate shift rate camera coords.
                        updateLockPositionShiftCameraCoords()
                    }
                    GuiderState.GUIDING -> {
                        if (ditherRecenterRemaining.valid) {
                            // Fast recenter after dither taking large steps and bypassing guide algorithms.
                            val step =
                                Point(min(ditherRecenterRemaining.x, ditherRecenterStep.x), min(ditherRecenterRemaining.y, ditherRecenterStep.y))

                            LOG.info(
                                "dither recenter. remaining: x={}, y={}, step: x={}, y={}",
                                ditherRecenterRemaining.x * ditherRecenterDir[0],
                                ditherRecenterRemaining.y * ditherRecenterDir[1],
                                step.x * ditherRecenterDir[0], step.y * ditherRecenterDir[1],
                            )

                            ditherRecenterRemaining -= step

                            if (ditherRecenterRemaining.x < 0.5 && ditherRecenterRemaining.y < 0.5) {
                                LOG.info("fast recenter is done")
                                // Fast recenter is done.
                                ditherRecenterRemaining.invalidate()
                                // Reset distance tracker.
                                avgDistanceNeedReset = true
                            }

                            offset.mount.set(step.x * ditherRecenterDir[0], step.y * ditherRecenterDir[1])
                            transformMountCoordinatesToCameraCoordinates(offset.mount, offset.camera)
                            device.moveOffset(offset, RECOVERY_MOVE)
                            // Let guide algorithms know about the direct move.
                            device.notifyDirectMove(offset.mount)
                        } else if (measurementMode) {
                            // GuidingAssistant::NotifyBacklashStep(CurrentPosition());
                        } else {
                            device.moveOffset(offset, GUIDE_STEP)
                        }
                    }
                    else -> Unit
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
                val raParity = device.rightAscensionParity
                val decParity = device.declinationParity
                var x = raDecRates.x
                var y = raDecRates.y

                if (raParity == GuideParity.ODD) {
                    x = -x
                }

                if (decParity == GuideParity.ODD) {
                    y = -y
                }

                val declination = device.mountDeclination

                if (declination.valid) {
                    x *= declination.cos
                }

                raDecRates.set(x, y)
            }

            transformMountCoordinatesToCameraCoordinates(raDecRates, rate)
        } else {
            rate.set(lockPositionShift.shiftRate)
        }

        // Convert arc-seconds to pixels.
        if (lockPositionShift.shiftUnit == ShiftUnit.ARCSEC) {
            rate.set(rate.x / device.cameraPixelScale, rate.y / device.cameraPixelScale)
        }

        // Per hour to per second.
        rate.set(rate.x / 3600.0, rate.y / 3600.0)

        lockPosition.shiftRate(rate.x, rate.y)
    }

    private fun clearSecondaryStars() {
        while (guideStars.size > 1) {
            val star = guideStars.removeAt(1)
            LOG.info("secondary guide star removed. x={}, y={}", star.x, star.y)
        }
    }

    private fun tolerateJumps(enable: Boolean, threshold: Double) {
        tolerateJumpsEnabled = enable
        tolerateJumpsThreshold = threshold
    }

    private fun currentPosition(image: Image, position: Point): Boolean {
        require(position.valid) { "position is invalid" }
        return currentPosition(image, position.x, position.y)
    }

    private fun currentPosition(image: Image, x: Double, y: Double): Boolean {
        require(x > 0.0 && x < image.width) { "invalid x value" }
        require(y > 0.0 && y < image.height) { "invalid y value" }

        massChecker.reset()

        LOG.info("current position. x={}, y={}", x, y)

        return primaryStar
            .find(image, searchRegion, x, y, FindMode.CENTROID, minStarHFD)
    }

    override fun autoSelect(): Boolean {
        // TODO:
        return false
    }

    private fun invalidateCurrentPosition(fullReset: Boolean = false) {
        if (fullReset) {
            primaryStar.set(0.0, 0.0)
        }

        primaryStar.invalidate()
    }

    private fun invalidateLockPosition() {
        if (lockPosition.valid) listeners.forEach { it.onLockPositionLost() }
        lockPosition.invalidate()
    }

    val starCount
        get() = guideStars.size

    /**
     * Uses secondary stars to refine Offset value if appropriate.
     * Returns of true means offset has been adjusted.
     */
    private fun refineOffset(image: Image, offset: GuiderOffset): Boolean {
        var primarySigma = 0.0
        var averaged = false
        var validStars = 0
        val origOffset = offset.copy()
        var refined = false

        starsUsed = 1

        // Primary star is in position 0 of the list.
        if (guiding && starCount > 1 && device.guidingEnabled && !settling) {
            var sumWeights = 1.0
            var sumX = origOffset.camera.x
            var sumY = origOffset.camera.y
            val primaryDistance = hypot(sumX, sumY)

            primaryDistStats.add(primaryDistance)

            if (primaryDistStats.count > 5) {
                primarySigma = primaryDistStats.sigma

                if (!stabilizing && primaryDistance > stabilitySigmaX * primarySigma) {
                    LOG.info("large primary error, entering stabilization period")
                    stabilizing = true
                } else if (stabilizing) {
                    if (primaryDistance <= 2 * primarySigma) {
                        stabilizing = false

                        LOG.info("exiting stabilization period")

                        if (lockPositionMoved) {
                            lockPositionMoved = false

                            LOG.info("updating star positions after lock position change")

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
                                    LOG.info("star removed after lock position change. x={}, y={}", star.x, star.y)
                                    guideStarsIter.remove()
                                }
                            }

                            if (starCount > 1) {
                                LOG.info("{} stars in list after lock position change", starCount)
                            } else {
                                LOG.info("no secondary stars found after lock position change")
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
                        offset.camera.set(sumX, sumY)
                        refined = true
                    }

                    LOG.info(
                        "{}: {} stars included. multi-star: x={} y={}. single-star: x={}, y={}",
                        if (refined) "refined" else "single-star", validStars,
                        sumX, sumY, origOffset.camera.x, origOffset.camera.y,
                    )
                }
            }
        }

        return refined
    }

    // Shift + Left Click.
    override fun deselectGuideStar() {
        invalidateCurrentPosition(true)
    }

    // Left Click.
    override fun selectGuideStar(
        x: Double, y: Double,
    ): Boolean {
        val image = image ?: return false

        if (state > GuiderState.SELECTED) {
            LOG.warn("state > SELECTED. state={}", state)
            return false
        }

        if (x <= searchRegion || x + searchRegion >= image.width
            || y <= searchRegion || y + searchRegion >= image.height
        ) {
            LOG.warn("outside of search region. x={}, y={}, searchRegion={}", x, y, searchRegion)
            return false
        }

        return if (currentPosition(image, x, y)
            && primaryStar.valid
        ) {
            lockPosition(primaryStar)

            clearSecondaryStars()

            if (starCount == 0) {
                guideStars.add(primaryStar)
                LOG.info("primary star added. x={}, y={}", primaryStar.x, primaryStar.y)
            }

            LOG.info("single-star usage forced by user star selection")

            listeners.forEach { it.onStarSelected(primaryStar) }

            updateState(GuiderState.SELECTED)

            true
        } else {
            LOG.warn("no star selected at position. x={}, y={}", x, y)
            false
        }
    }

    private fun updateCurrentPosition(
        image: Image,
        offset: GuiderOffset,
    ): Boolean {
        if (!primaryStar.valid && primaryStar.x == 0.0 && primaryStar.y == 0.0) {
            LOG.warn("no star selected")
            return false
        }

        val newStar = GuideStar(primaryStar)

        if (!newStar.find(image, searchRegion, minHFD = minStarHFD)) {
            distanceChecker.activate()
            LOG.warn("new star not found. x={}, y={}", newStar.x, newStar.y)
            return false
        }

        // check to see if it seems like the star we just found was the
        // same as the original star by comparing the mass.
        if (massChangeThresholdEnabled) {
            massChecker.exposure(device.cameraExposure)

            val checkedMass = massChecker.checkMass(newStar.mass, massChangeThreshold)

            if (checkedMass.reject) {
                massChecker.add(newStar.mass)
                distanceChecker.activate()
                LOG.warn("mass changed. checkedMass={}", checkedMass)
                return false
            }
        }

        var distance = if (lockPosition.valid) {
            if (device.guidingRAOnly) {
                abs(newStar.x - lockPosition.x)
            } else {
                newStar.distance(lockPosition)
            }
        } else {
            0.0
        }

        LOG.info("checking distance. dist={}, raOnly={}", distance, device.guidingRAOnly)

        val tolerance = if (tolerateJumpsEnabled) tolerateJumpsThreshold else Double.MAX_VALUE

        if (!distanceChecker.checkDistance(distance, device.guidingRAOnly, tolerance)) {
            LOG.info("check distance error")
            return false
        }

        primaryStar = newStar
        guideStars[0] = primaryStar

        massChecker.add(newStar.mass)

        if (lockPosition.valid) {
            offset.camera.set(primaryStar - lockPosition)

            if (multiStar && starCount > 1) {
                if (refineOffset(image, offset)) {
                    distance = hypot(offset.camera.x, offset.camera.y)
                    LOG.info("refined distance. dist={}", distance)
                }
            } else {
                starsUsed = 1
            }

            if (guideCalibrator.calibrated) {
                transformCameraCoordinatesToMountCoordinates(offset.camera, offset.mount)
            }

            val distanceRA = if (offset.mount.valid) abs(offset.mount.x) else 0.0
            updateCurrentDistance(distance, distanceRA)
        }

        // pFrame->pProfile->UpdateData(pImage, m_primaryStar.X, m_primaryStar.Y)

        return true
    }

    private fun isValidLockPosition(point: Point): Boolean {
        val image = image ?: return false

        val region = 1.0 + searchRegion
        return point.x >= region &&
                point.x + region < image.width &&
                point.y >= region &&
                point.y + region < image.height
    }

    private fun isValidSecondaryStarPosition(point: Point): Boolean {
        val image = image ?: return false

        return point.x >= 5.0 &&
                point.x + 5.0 < image.width &&
                point.y >= 5.0 &&
                point.y + 5.0 < image.height
    }

    private fun GuideDevice.moveOffset(offset: GuiderOffset, moveOptions: List<MountMoveOption>): Boolean {
        if (MountMoveOption.ALGORITHM_DEDUCE in moveOptions) {
            val xDistance = xGuideAlgorithm.deduce()
            val yDistance = yGuideAlgorithm.deduce()

            if (xDistance != 0.0 || yDistance != 0.0) {
                LOG.info("deduced move. x={}, y={}", xDistance, yDistance)
                offset.mount.set(xDistance, yDistance)
            }
        } else {
            if (!offset.mount.valid) {
                if (!transformCameraCoordinatesToMountCoordinates(offset.camera, offset.mount)) {
                    LOG.error("unable to transform camera coordinates")
                    return false
                }
            }

            var xDistance = offset.mount.x
            var yDistance = offset.mount.y

            // Let BLC track the raw offsets in DEC
            // TODO: if (m_backlashComp)
            //    m_backlashComp->TrackBLCResults(moveOptions, yDistance)

            if (MountMoveOption.ALGORITHM_RESULT in moveOptions) {
                xDistance = xGuideAlgorithm.compute(xDistance)
                yDistance = yGuideAlgorithm.compute(yDistance)
            }

            if (xDistance != 0.0 || yDistance != 0.0) {
                val xDirection = if (xDistance > 0.0) GuideDirection.LEFT_WEST else GuideDirection.RIGHT_EAST
                val requestedXAmount = abs(xDistance / guideCalibrator.xRate).roundToInt()

                LOG.info("move RA. distance={}, direction={}", xDistance, xDirection)

                val xResult = moveAxis(xDirection, requestedXAmount, moveOptions)

                if (xResult.status) {
                    val yDirection = if (yDistance > 0.0) GuideDirection.DOWN_SOUTH else GuideDirection.UP_NORTH
                    val requestedYAmount = abs(yDistance / guideCalibrator.yRate).roundToInt()

                    LOG.info("move DEC. distance={}, direction={}", yDistance, yDirection)

                    // TODO: if (m_backlashComp)
                    //     m_backlashComp->ApplyBacklashComp(moveOptions, yDistance, &requestedYAmount);

                    val yResult = moveAxis(yDirection, requestedYAmount, moveOptions)

                    val history = guideGraph.add(offset, xResult.amountMoved, yResult.amountMoved, xDirection, yDirection)
                }
            }
        }

        return true
    }

    private fun GuideDevice.moveAxis(direction: GuideDirection, duration: Int, moveOptions: List<MountMoveOption>): MoveResult {
        if (!guidingEnabled && MountMoveOption.MANUAL !in moveOptions) {
            LOG.warn("guiding disabled")
            return MoveResult.FAILED
        }

        // Compute the actual guide durations.
        var newDuration = duration

        var limitReached = false

        when (direction) {
            GuideDirection.UP_NORTH,
            GuideDirection.DOWN_SOUTH -> {
                // Enforce DEC guide mode and max duration for guide step (or deduced step) moves.
                if (MountMoveOption.ALGORITHM_RESULT in moveOptions ||
                    MountMoveOption.ALGORITHM_DEDUCE in moveOptions
                ) {
                    if ((device.declinationGuideMode == DeclinationGuideMode.NONE) ||
                        (direction == GuideDirection.DOWN_SOUTH && device.declinationGuideMode == DeclinationGuideMode.NORTH) ||
                        (direction == GuideDirection.UP_NORTH && device.declinationGuideMode == DeclinationGuideMode.SOUTH)
                    ) {
                        newDuration = 0
                        LOG.info("duration set to 0. mode={}", device.declinationGuideMode)
                    }

                    if (newDuration > device.maxDeclinationDuration) {
                        newDuration = device.maxDeclinationDuration
                        limitReached = true
                        LOG.info("duration set to maxDeclinationDuration. duration={}", newDuration)
                    }
                }
            }
            GuideDirection.LEFT_WEST,
            GuideDirection.RIGHT_EAST -> {
                // Enforce RA guide mode and max duration for guide step (or deduced step) moves.
                if (MountMoveOption.ALGORITHM_RESULT in moveOptions ||
                    MountMoveOption.ALGORITHM_DEDUCE in moveOptions
                ) {
                    if (newDuration > device.maxRightAscensionDuration) {
                        newDuration = device.maxRightAscensionDuration
                        limitReached = true
                        LOG.info("duration set to maxRightAscensionDuration. duration={}", newDuration)
                    }
                }
            }
            else -> Unit
        }

        return if (newDuration > 0) {
            LOG.info("move axis. direction={}, duration={}", direction, newDuration)

            val status = when (direction) {
                GuideDirection.UP_NORTH -> device.guideNorth(newDuration)
                GuideDirection.DOWN_SOUTH -> device.guideSouth(newDuration)
                GuideDirection.LEFT_WEST -> device.guideWest(newDuration)
                GuideDirection.RIGHT_EAST -> device.guideEast(newDuration)
                GuideDirection.NONE -> false
            }

            MoveResult(status, newDuration, limitReached)
        } else {
            MoveResult.FAILED
        }
    }

    private fun transformMountCoordinatesToCameraCoordinates(mount: Point, camera: Point): Boolean {
        if (!mount.valid) return false
        val distance = mount.distance
        var mountTheta = mount.angle
        if (abs(guideCalibrator.yAngleError.value) > PIOVERTWO) mountTheta = -mountTheta
        val xAngle = mountTheta + guideCalibrator.xAngle
        camera.set(xAngle.cos * distance, xAngle.sin * distance)
        return true
    }

    private fun transformCameraCoordinatesToMountCoordinates(camera: Point, mount: Point): Boolean {
        if (!camera.valid) return false
        val distance = camera.distance
        val cameraTheta = camera.angle
        val xAngle = cameraTheta - guideCalibrator.xAngle
        val yAngle = cameraTheta - (guideCalibrator.xAngle + guideCalibrator.yAngleError)
        mount.set(xAngle.cos * distance, yAngle.sin * distance)
        return true
    }

    override fun startLooping() {
        if (capturer.stopped) {
            executor.submit(capturer)
        } else {
            capturer.unpause()
        }
    }

    override fun stopLooping() {
        capturer.pause()
    }

    fun currentError(raOnly: Boolean): Double {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceRA else avgDistance)
    }

    fun currentErrorSmoothed(raOnly: Boolean): Double {
        return currentError(starFoundTimestamp, if (raOnly) avgDistanceLongRA else avgDistanceLong)
    }

    override fun iterator() = guideStars.iterator()

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(MultiStarGuider::class.java)
        @JvmStatic internal val ZERO_OFFSET = GuiderOffset(Point(), Point())
        @JvmStatic internal val GUIDE_STEP = listOf(MountMoveOption.ALGORITHM_RESULT, MountMoveOption.USE_BACKSLASH_COMPENSATION)
        @JvmStatic internal val DEDUCED_MOVE = listOf(MountMoveOption.ALGORITHM_DEDUCE, MountMoveOption.USE_BACKSLASH_COMPENSATION)
        @JvmStatic internal val RECOVERY_MOVE = listOf(MountMoveOption.USE_BACKSLASH_COMPENSATION)

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
    }
}
