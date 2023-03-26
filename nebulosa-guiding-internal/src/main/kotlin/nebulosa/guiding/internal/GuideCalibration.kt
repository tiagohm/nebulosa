package nebulosa.guiding.internal

import nebulosa.constants.PI
import nebulosa.constants.PIOVERTWO
import nebulosa.math.Angle
import org.slf4j.LoggerFactory
import kotlin.math.floor
import kotlin.math.max

class GuideCalibration(private val guider: MultiStarGuider) {

    private inline val mount: GuideMount?
        get() = guider.mount

    private var calibration = Calibration.EMPTY
    private var calibrationSteps = 0
    private var recenterRemaining = 0
    private var recenterDuration = 0
    private var calibrationInitialLocation = Point()
    private var calibrationStartingLocation = Point()
    private var calibrationStartingCoords = Point()
    private var southStartingLocation = Point()
    private var eastStartingLocation = Point()
    private var lastLocation = Point()
    private var totalSouthAmt = 0.0
    private var northDirCosX = 0.0
    private var northDirCosY = 0.0
    private var calibrationState = CalibrationState.CLEARED
    private var assumeOrthogonal = false
    private var raSteps = 0
    private var decSteps = 0

    // Backlash.
    private var blMarkerPoint = Point()
    private var blExpectedBacklashStep = 0.0
    private var blLastCumDistance = 0.0
    private var blAcceptedMoves = 0
    private var blDistanceMoved = 0.0
    private var blMaxClearingPulses = 0

    val xRate
        get() = calibration.xRate

    val yRate
        get() = calibration.yRate

    val declination
        get() = calibration.declination

    val rotatorAngle
        get() = calibration.rotatorAngle

    val binning
        get() = calibration.binning

    val pierSideAtEast
        get() = calibration.pierSideAtEast

    val raGuideParity
        get() = calibration.raGuideParity

    val decGuideParity
        get() = calibration.decGuideParity

    var yAngleError = Angle.ZERO
        private set

    var calibrated = false
        get() {
            if (!field) return false

            return when (mount?.declinationGuideMode) {
                DeclinationGuideMode.NONE -> true
                DeclinationGuideMode.AUTO,
                DeclinationGuideMode.NORTH,
                DeclinationGuideMode.SOUTH -> calibration.yRate != CALIBRATION_RATE_UNCALIBRATED
                null -> true
            }
        }
        private set

    val xAngle
        get() = calibration.xAngle

    val yAngle
        // TODO: Check if normalization [-PI..PI] is correct.
        get() = (calibration.xAngle - yAngleError + PIOVERTWO).normalized - PI

    fun set(newCalibration: Calibration) {
        calibration = calibration.copy(
            xRate = newCalibration.xRate,
            yRate = newCalibration.yRate,
            binning = newCalibration.binning,
            declination = newCalibration.declination,
            pierSideAtEast = newCalibration.pierSideAtEast,
            rotatorAngle = newCalibration.rotatorAngle,
            xAngle = newCalibration.xAngle,
            yAngle = newCalibration.yAngle,
            raGuideParity = if (newCalibration.raGuideParity != GuideParity.UNCHANGED) newCalibration.raGuideParity else calibration.raGuideParity,
            decGuideParity = if (newCalibration.decGuideParity != GuideParity.UNCHANGED) newCalibration.decGuideParity else calibration.decGuideParity,
        )

        // The angles are more difficult because we have to turn yAngle into a yError.
        // TODO: Check if normalization [-PI..PI] is correct.
        yAngleError = (newCalibration.xAngle - newCalibration.yAngle + PIOVERTWO).normalized - PI

        calibrated = true
    }

    fun flip() {
        if (!calibrated) return

        val mount = mount ?: return

        val origX = xAngle
        val origY = yAngle

        val decFlipRequired = mount.calibrationFlipRequiresDecFlip

        var newX = origX + PI
        var newY = origY

        if (decFlipRequired) {
            newY += PI
        }

        // Normalize.
        // TODO: Check if normalization [-PI..PI] is correct.
        newX = newX.normalized - PI
        newY = newY.normalized - PI

        val pierSideAtEast = !calibration.pierSideAtEast
        // Dec polarity changes when pier side changes, i.e. if Guide(NORTH) moves the star north on one side,
        // then Guide(NORTH) will move the star south on the other side of the pier.
        // For mounts with calibrationFlipRequiresDecFlip, the parity does not change after the flip.
        val newDecParity = if (decFlipRequired) calibration.decGuideParity else calibration.decGuideParity.opposite

        set(calibration.copy(xAngle = newX, yAngle = newY, pierSideAtEast = pierSideAtEast, decGuideParity = newDecParity))
    }

    fun beginCalibration(currentLocation: Point): Boolean {
        if (!currentLocation.valid) return false
        val mount = mount ?: return false

        // Make sure guide speeds or binning haven't changed underneath us.
        checkCalibrationDuration(mount.calibrationDuration)
        clearCalibration()
        calibrationSteps = 0
        calibrationInitialLocation = currentLocation
        calibrationStartingLocation.invalidate()
        calibrationStartingCoords.invalidate()
        calibrationState = CalibrationState.GO_WEST
        // calibrationDetails.raSteps.clear()
        // calibrationDetails.decSteps.clear()
        raSteps = 0
        decSteps = 0
        // calibrationDetails.lastIssue = CI_None

        return true
    }

    private fun clearCalibration() {
        calibrated = false
        calibrationState = CalibrationState.CLEARED
    }

    @Synchronized
    fun updateCalibrationState(currentLocation: Point): Boolean {
        val mount = mount ?: return false

        if (!mount.connected) return false

        LOG.info("updating calibration state. x={}, y={}", currentLocation.x, currentLocation.y)

        if (!calibrationStartingLocation.valid) {
            calibrationStartingLocation = currentLocation
            calibrationStartingCoords.set(mount.rightAscension.hours, mount.declination.degrees)
        }

        var dx = calibrationStartingLocation.dX(currentLocation)
        var dy = calibrationStartingLocation.dY(currentLocation)
        var dist = calibrationStartingLocation.distance(currentLocation)
        val distCrit = mount.calibrationDistance
        var blDelta = 0.0
        var blCumDelta = 0.0
        var nudgeAmt = 0.0
        var nudgeDirCosX = 0.0
        var nudgeDirCosY = 0.0
        var cosTheta = 0.0
        var theta = 0.0
        var southDistMoved = 0.0
        var northDistMoved = 0.0
        var southAngle = 0.0

        fun calibrationStatus(direction: GuideDirection) {
            guider.listeners.forEach {
                it.onCalibrationStep(
                    calibrationState,
                    direction, calibrationSteps,
                    dx, dy, currentLocation.x, currentLocation.y,
                    dist,
                )
            }
        }

        // Simulate C-style switch fall through.
        while (true) {
            when (calibrationState) {
                CalibrationState.GO_WEST -> {
                    if (dist < distCrit) {
                        if (calibrationSteps++ > MAX_CALIBRATION_STEPS) {
                            guider.listeners.forEach { it.onCalibrationFailed() }
                            return false
                        }

                        LOG.info("West step {}, dist={}", calibrationSteps, dist)

                        calibrationStatus(GuideDirection.LEFT_WEST)

                        mount.guideTo(GuideDirection.LEFT_WEST, mount.calibrationDuration)

                        break
                    }

                    // West calibration complete.

                    calibration = calibration.copy(
                        xAngle = calibrationStartingLocation.angle(currentLocation),
                        xRate = dist / (calibrationSteps * mount.calibrationDuration),
                        raGuideParity = GuideParity.UNKNOWN,
                    )

                    if (calibrationStartingCoords.valid) {
                        val endingCoords = Point(mount.rightAscension.hours, mount.declination.degrees, true)

                        // True westward motion decreases RA.
                        val dra = endingCoords.x - calibrationStartingCoords.x
                        calibration = calibration.copy(
                            raGuideParity = if (dra < -ONE_ARCSEC_HOURS) GuideParity.EVEN
                            else if (dra > ONE_ARCSEC_HOURS) GuideParity.ODD
                            else calibration.raGuideParity,
                        )
                    }

                    LOG.info(
                        "West calibration completed. steps={}, angle={}, rate={}, parity={}",
                        calibrationSteps, calibration.xAngle.degrees, calibration.xRate * 1000.0, calibration.raGuideParity
                    )

                    raSteps = calibrationSteps

                    // For GO_EAST recenterRemaining contains the total remaining duration.
                    // Choose the largest pulse size that will not lose the guide star or exceed
                    // the user-specified max pulse.
                    recenterRemaining = calibrationSteps * mount.calibrationDuration

                    if (guider.fastRecenterEnabled) {
                        recenterDuration = floor(guider.searchRegion / calibration.xRate).toInt()
                        if (recenterDuration > mount.maxRightAscensionDuration) recenterDuration = mount.maxRightAscensionDuration
                        if (recenterDuration < mount.calibrationDuration) recenterDuration = mount.calibrationDuration
                    } else {
                        recenterDuration = mount.calibrationDuration
                    }

                    calibrationSteps = (recenterRemaining + recenterDuration - 1) / recenterDuration
                    calibrationState = CalibrationState.GO_EAST
                    eastStartingLocation = currentLocation
                }
                CalibrationState.GO_EAST -> {
                    if (recenterRemaining > 0) {
                        var duration = recenterDuration
                        if (duration > recenterRemaining) duration = recenterRemaining

                        LOG.info("East step {}, dist={}", calibrationSteps, dist)

                        calibrationStatus(GuideDirection.RIGHT_EAST)

                        recenterRemaining -= duration
                        calibrationSteps--
                        lastLocation = currentLocation

                        // TODO: Wait guideTo complete.

                        mount.guideTo(GuideDirection.RIGHT_EAST, duration)

                        break
                    }

                    // Setup for clear backlash.

                    calibrationSteps = 0
                    dist = 0.0
                    dx = 0.0
                    dy = 0.0
                    calibrationStartingLocation = currentLocation

                    if (mount.declinationGuideMode == DeclinationGuideMode.NONE) {
                        LOG.info("skipping DEC calibration as declinationGuideMode == NONE")
                        calibrationState = CalibrationState.COMPLETE

                        calibration = calibration.copy(
                            // Choose an arbitrary angle perpendicular to xAngle.
                            yAngle = (calibration.xAngle + PIOVERTWO).normalized - PI,
                            // Indicate lack of Dec calibration data.
                            yRate = CALIBRATION_RATE_UNCALIBRATED,
                            decGuideParity = GuideParity.UNKNOWN,
                        )

                        break
                    }

                    calibrationState = CalibrationState.CLEAR_BACKLASH
                    blMarkerPoint = currentLocation
                    calibrationStartingCoords.set(mount.rightAscension.hours, mount.declination.degrees)
                    blExpectedBacklashStep = calibration.xRate * mount.calibrationDuration * 0.6

                    val raSpeed = mount.rightAscensionGuideRate
                    val decSpeed = mount.declinationGuideRate

                    if (raSpeed != 0.0 && raSpeed != decSpeed) {
                        blExpectedBacklashStep *= decSpeed / raSpeed
                    }

                    blMaxClearingPulses = max(8, BL_MAX_CLEARING_TIME / mount.calibrationDuration)
                    blLastCumDistance = 0.0
                    blAcceptedMoves = 0

                    LOG.info("looking for 3 moves of {} px, max attempts = {}", blExpectedBacklashStep, blMaxClearingPulses)
                }
                CalibrationState.CLEAR_BACKLASH -> {
                    blDelta = blMarkerPoint.distance(currentLocation)
                    blCumDelta = dist

                    // Want to see the mount moving north for 3 moves of >= expected distance
                    // pixels without any direction reversals.
                    if (calibrationSteps == 0) {
                        // Get things moving with the first clearing pulse
                        LOG.info("starting north clearing using pulse width of {}", mount.calibrationDuration)
                        mount.guideTo(GuideDirection.UP_NORTH, mount.calibrationDuration)
                        calibrationSteps = 1
                        calibrationStatus(GuideDirection.UP_NORTH)
                        break
                    }

                    if (blDelta >= blExpectedBacklashStep) {
                        // Just starting or still moving in same direction.
                        if (blAcceptedMoves == 0 || (blCumDelta > blLastCumDistance)) {
                            blAcceptedMoves++
                            LOG.info("accepted clearing move of {}", blDelta)
                        } else {
                            // Reset on a direction reversal.
                            blAcceptedMoves = 0
                            LOG.info("rejected clearing move of {}, direction reversal", blDelta)
                        }
                    } else if (blCumDelta < blLastCumDistance) {
                        blAcceptedMoves = 0
                        LOG.info("rejected small direction reversal of {} px", blDelta)
                    } else {
                        LOG.info("rejected small move of {} px", blDelta)
                    }

                    // More work to do.
                    if (blAcceptedMoves < BL_MIN_COUNT) {
                        if (calibrationSteps < blMaxClearingPulses && blCumDelta < distCrit) {
                            // Still have attempts left, haven't moved the star by 25 px yet.
                            mount.guideTo(GuideDirection.UP_NORTH, mount.calibrationDuration)
                            calibrationSteps++
                            blMarkerPoint = currentLocation
                            calibrationStartingCoords.set(mount.rightAscension.hours, mount.declination.degrees)
                            blLastCumDistance = blCumDelta
                            calibrationStatus(GuideDirection.UP_NORTH)
                            LOG.info("last delta = {} px, cum distance = {}", blDelta, blCumDelta)
                            break
                        } else {
                            // Used up all our attempts - might be ok or not.
                            if (blCumDelta >= BL_MIN_CLEARING_DISTANCE) {
                                // Exhausted all the clearing pulses without reaching the goal - but we did move the mount > 3 px (same as PHD1).
                                calibrationSteps = 0
                                calibrationStartingLocation = currentLocation
                                dx = 0.0
                                dy = 0.0
                                dist = 0.0
                                LOG.info("reached clearing limit but total displacement > 3px - proceeding with calibration")
                            } else {
                                guider.listeners.forEach { it.onCalibrationFailed() }
                                LOG.error("clear backlash failed")
                                break
                            }
                        }
                    } else {
                        // Got our 3 moves, move ahead.
                        // We know the last backlash clearing move was big enough - include that as a north calibration move.
                        calibrationSteps = 1;
                        calibrationStartingLocation = blMarkerPoint
                        dx = blMarkerPoint.dX(currentLocation)
                        dy = blMarkerPoint.dY(currentLocation)
                        dist = blMarkerPoint.distance(currentLocation)
                        LOG.info("Got 3 acceptable moves, using last move as step 1 of N calibration")
                    }

                    // Need this to set nudging limit.
                    blDistanceMoved = blMarkerPoint.distance(calibrationInitialLocation)

                    LOG.info("north calibration moves starting at x={}, y={}, offset = {} px", blMarkerPoint.x, blMarkerPoint.y, blDistanceMoved)
                    LOG.info("total distance moved = {}", currentLocation.distance(calibrationInitialLocation))

                    calibrationState = CalibrationState.GO_NORTH
                }
                CalibrationState.GO_NORTH -> {
                    if (dist < distCrit) {

                    }
                }
                CalibrationState.GO_SOUTH -> TODO()
                CalibrationState.NUDGE_SOUTH -> TODO()
                CalibrationState.COMPLETE -> TODO()
                else -> Unit
            }
        }

        return true
    }

    fun checkCalibrationDuration(currDuration: Int) {
        // TODO:
    }

    /*
     * Adjusts the calibration data for the scope's current coordinates.
     *
     * This includes adjusting the xRate to compensate for changes in declination
     * relative to the declination where calibration was done, and possibly flipping
     * the calibration data if the mount is known to be on the other side of the
     * pier from where calibration was done.
     */
    fun adjustCalibrationForScopePointing() {
        // TODO
    }

    companion object {

        private const val CALIBRATION_RATE_UNCALIBRATED = 123E4
        private const val MAX_CALIBRATION_STEPS = 60
        private const val ONE_ARCSEC_HOURS = 24.0 / (360.0 * 60.0 * 60.0)
        private const val BL_MIN_COUNT = 3
        private const val BL_MAX_CLEARING_TIME = 60000
        private const val BL_MIN_CLEARING_DISTANCE = 3

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuideCalibration::class.java)
    }
}
