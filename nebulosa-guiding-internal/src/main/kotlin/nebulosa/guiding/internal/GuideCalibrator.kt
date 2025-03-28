package nebulosa.guiding.internal

import nebulosa.constants.PI
import nebulosa.constants.PIOVERTWO
import nebulosa.guiding.GuideDirection
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.cos
import nebulosa.math.normalized
import nebulosa.math.rad
import nebulosa.math.sin
import nebulosa.math.toDegrees
import nebulosa.math.toHours
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

internal class GuideCalibrator(private val guider: MultiStarGuider) {

    private var calibration = GuideCalibration.EMPTY
    private var calibrationSteps = 0
    private var recenterRemaining = 0
    private var recenterDuration = 0
    private val calibrationInitialLocation = Point()
    private val calibrationStartingLocation = Point()
    private val calibrationStartingCoords = Point()
    private val southStartingLocation = Point()
    private val eastStartingLocation = Point()
    private val lastLocation = Point()
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

    var xRate = 0.0
        private set

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

    var yAngleError = 0.0
        private set

    var calibrated = false
        private set
        get() {
            if (!field) return false

            return when (guider.declinationGuideMode) {
                DeclinationGuideMode.NONE -> true
                DeclinationGuideMode.AUTO,
                DeclinationGuideMode.NORTH,
                DeclinationGuideMode.SOUTH -> calibration.yRate != CALIBRATION_RATE_UNCALIBRATED
            }
        }

    val xAngle
        get() = calibration.xAngle

    val yAngle
        get() = (calibration.xAngle - yAngleError + PIOVERTWO).normalized - PI

    internal fun load(newCalibration: GuideCalibration) {
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

        xRate = newCalibration.xRate

        // The angles are more difficult because we have to turn yAngle into a yError.
        yAngleError = (newCalibration.xAngle - newCalibration.yAngle + PIOVERTWO).normalized - PI

        calibrated = true
    }

    fun flip() {
        if (!calibrated) return

        val origX = xAngle
        val origY = yAngle

        val decFlipRequired = guider.calibrationFlipRequiresDecFlip

        var newX = origX + PI
        var newY = origY

        if (decFlipRequired) {
            newY += PI
        }

        // Normalize.
        newX = newX.normalized - PI
        newY = newY.normalized - PI

        val pierSideAtEast = !calibration.pierSideAtEast
        // Dec polarity changes when pier side changes, i.e. if Guide(NORTH) moves the star north on one side,
        // then Guide(NORTH) will move the star south on the other side of the pier.
        // For mounts with calibrationFlipRequiresDecFlip, the parity does not change after the flip.
        val newDecParity = if (decFlipRequired) calibration.decGuideParity else calibration.decGuideParity.opposite

        load(calibration.copy(xAngle = newX, yAngle = newY, pierSideAtEast = pierSideAtEast, decGuideParity = newDecParity))
    }

    fun beginCalibration(currentLocation: Point): Boolean {
        if (!currentLocation.valid) return false

        clear()

        calibrationSteps = 0
        calibrationInitialLocation.set(currentLocation)
        calibrationStartingLocation.invalidate()
        calibrationStartingCoords.invalidate()
        calibrationState = CalibrationState.GO_WEST
        raSteps = 0
        decSteps = 0

        return true
    }

    fun clear() {
        calibrated = false
        calibrationState = CalibrationState.CLEARED
        calibration = GuideCalibration.EMPTY
    }

    @Synchronized
    fun updateCalibrationState(currentLocation: Point): Boolean {
        LOG.d { info("updating calibration state. x={}, y={}", currentLocation.x, currentLocation.y) }

        if (!calibrationStartingLocation.valid) {
            calibrationStartingLocation.set(currentLocation)
            calibrationStartingCoords.set(guider.mount.rightAscension.toHours, guider.mount.declination.toDegrees)
        }

        var dx = calibrationStartingLocation.dX(currentLocation)
        var dy = calibrationStartingLocation.dY(currentLocation)
        var dist = calibrationStartingLocation.distance(currentLocation)
        val distCrit = guider.calibrationDistance

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
                            LOG.d { warn("calibration failed. star did not move enough.") }
                            guider.listeners.forEach { it.onCalibrationFailed() }
                            return false
                        }

                        LOG.d { info("west step {}, dist={}", calibrationSteps, dist) }

                        calibrationStatus(GuideDirection.WEST)

                        guideDirection(GuideDirection.WEST, guider.calibrationStep)

                        break
                    }

                    // West calibration complete.

                    calibration = calibration.copy(
                        xAngle = calibrationStartingLocation.angle(currentLocation),
                        xRate = dist / (calibrationSteps * guider.calibrationStep),
                        raGuideParity = GuideParity.UNKNOWN,
                    )

                    if (calibrationStartingCoords.valid) {
                        val endingCoords = Point(guider.mount.rightAscension.toHours, guider.mount.declination.toDegrees, true)

                        // True westward motion decreases RA.
                        val dra = endingCoords.x - calibrationStartingCoords.x
                        calibration = calibration.copy(
                            raGuideParity = if (dra < -ONE_ARCSEC_HOURS) GuideParity.EVEN
                            else if (dra > ONE_ARCSEC_HOURS) GuideParity.ODD
                            else calibration.raGuideParity,
                        )
                    }

                    LOG.d { info("west calibration completed. steps={}, angle={}, rate={}, parity={}", calibrationSteps, calibration.xAngle.toDegrees, calibration.xRate * 1000.0, calibration.raGuideParity) }

                    raSteps = calibrationSteps

                    // For GO_EAST recenterRemaining contains the total remaining duration.
                    // Choose the largest pulse size that will not lose the guide star or exceed
                    // the user-specified max pulse.
                    recenterRemaining = calibrationSteps * guider.calibrationStep

                    if (guider.fastRecenterEnabled) {
                        recenterDuration = floor(guider.searchRegion / calibration.xRate).toInt()
                        if (recenterDuration > guider.maxRADuration) recenterDuration = guider.maxRADuration
                        if (recenterDuration < guider.calibrationStep) recenterDuration = guider.calibrationStep
                    } else {
                        recenterDuration = guider.calibrationStep
                    }

                    calibrationSteps = (recenterRemaining + recenterDuration - 1) / recenterDuration
                    calibrationState = CalibrationState.GO_EAST
                    eastStartingLocation.set(currentLocation)
                }
                CalibrationState.GO_EAST -> {
                    if (recenterRemaining > 0) {
                        var duration = recenterDuration
                        if (duration > recenterRemaining) duration = recenterRemaining

                        LOG.d { info("East step {}, dist={}", calibrationSteps, dist) }

                        calibrationStatus(GuideDirection.EAST)

                        recenterRemaining -= duration
                        calibrationSteps--
                        lastLocation.set(currentLocation)

                        guideDirection(GuideDirection.EAST, duration)

                        break
                    }

                    // Setup for clear backlash.

                    calibrationSteps = 0
                    dist = 0.0
                    dx = 0.0
                    dy = 0.0
                    calibrationStartingLocation.set(currentLocation)

                    if (guider.declinationGuideMode == DeclinationGuideMode.NONE) {
                        LOG.d { info("skipping DEC calibration as declinationGuideMode == NONE") }
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
                    blMarkerPoint.set(currentLocation)
                    calibrationStartingCoords.set(guider.mount.rightAscension.toHours, guider.mount.declination.toDegrees)
                    blExpectedBacklashStep = calibration.xRate * guider.calibrationStep * 0.6

                    val raSpeed = guider.mount.rightAscensionGuideRate
                    val decSpeed = guider.mount.declinationGuideRate

                    if (raSpeed != 0.0 && raSpeed != decSpeed) {
                        blExpectedBacklashStep *= decSpeed / raSpeed
                    }

                    blMaxClearingPulses = max(8, BL_MAX_CLEARING_TIME / guider.calibrationStep)
                    blLastCumDistance = 0.0
                    blAcceptedMoves = 0

                    LOG.d { info("looking for 3 moves of {} px, max attempts = {}", blExpectedBacklashStep, blMaxClearingPulses) }
                }
                CalibrationState.CLEAR_BACKLASH -> {
                    val blDelta = blMarkerPoint.distance(currentLocation)
                    val blCumDelta = dist

                    // Want to see the mount moving north for 3 moves of >= expected distance
                    // pixels without any direction reversals.
                    if (calibrationSteps == 0) {
                        // Get things moving with the first clearing pulse.
                        LOG.d { info("starting north clearing using pulse width of {}", guider.calibrationStep) }
                        guideDirection(GuideDirection.NORTH, guider.calibrationStep)
                        calibrationSteps = 1
                        calibrationStatus(GuideDirection.NORTH)
                        break
                    }

                    if (blDelta >= blExpectedBacklashStep) {
                        // Just starting or still moving in same direction.
                        if (blAcceptedMoves == 0 || (blCumDelta > blLastCumDistance)) {
                            blAcceptedMoves++
                            LOG.d { info("accepted clearing move of {}", blDelta) }
                        } else {
                            // Reset on a direction reversal.
                            blAcceptedMoves = 0
                            LOG.d { info("rejected clearing move of {}, direction reversal", blDelta) }
                        }
                    } else if (blCumDelta < blLastCumDistance) {
                        blAcceptedMoves = 0
                        LOG.d { info("rejected small direction reversal of {} px", blDelta) }
                    } else {
                        LOG.d { info("rejected small move of {} px", blDelta) }
                    }

                    // More work to do.
                    if (blAcceptedMoves < BL_MIN_COUNT) {
                        if (calibrationSteps < blMaxClearingPulses && blCumDelta < distCrit) {
                            // Still have attempts left, haven't moved the star by 25 px yet.
                            guideDirection(GuideDirection.NORTH, guider.calibrationStep)

                            calibrationSteps++

                            blMarkerPoint.set(currentLocation)
                            calibrationStartingCoords.set(guider.mount.rightAscension.toHours, guider.mount.declination.toDegrees)
                            blLastCumDistance = blCumDelta

                            calibrationStatus(GuideDirection.NORTH)

                            LOG.d { info("last delta = {} px, cum distance = {}", blDelta, blCumDelta) }

                            break
                        } else {
                            // Used up all our attempts - might be ok or not.
                            if (blCumDelta >= BL_MIN_CLEARING_DISTANCE) {
                                // Exhausted all the clearing pulses without reaching the goal - but we did move the mount > 3 px (same as PHD1).
                                calibrationSteps = 0
                                calibrationStartingLocation.set(currentLocation)

                                dx = 0.0
                                dy = 0.0
                                dist = 0.0

                                LOG.d { info("reached clearing limit but total displacement > 3px - proceeding with calibration") }
                            } else {
                                guider.listeners.forEach { it.onCalibrationFailed() }
                                LOG.d { warn("clear backlash failed") }
                                return false
                            }
                        }
                    } else {
                        // Got our 3 moves, move ahead.
                        // We know the last backlash clearing move was big enough - include that as a north calibration move.
                        calibrationSteps = 1
                        calibrationStartingLocation.set(blMarkerPoint)

                        dx = blMarkerPoint.dX(currentLocation)
                        dy = blMarkerPoint.dY(currentLocation)
                        dist = blMarkerPoint.distance(currentLocation)

                        LOG.d { info("Got 3 acceptable moves, using last move as step 1 of N calibration") }
                    }

                    // Need this to set nudging limit.
                    blDistanceMoved = blMarkerPoint.distance(calibrationInitialLocation)

                    LOG.d { info("north calibration moves starting at x={}, y={}, offset = {} px", blMarkerPoint.x, blMarkerPoint.y, blDistanceMoved) }
                    LOG.d { info("total distance moved = {}", currentLocation.distance(calibrationInitialLocation)) }

                    calibrationState = CalibrationState.GO_NORTH
                }
                CalibrationState.GO_NORTH -> {
                    if (dist < distCrit) {
                        if (calibrationSteps++ > MAX_CALIBRATION_STEPS) {
                            LOG.d { warn("calibration failed. star did not move enough.") }
                            guider.listeners.forEach { it.onCalibrationFailed() }
                            return false
                        }

                        LOG.d { info("North step {}, dist={}", calibrationSteps, dist) }

                        calibrationStatus(GuideDirection.NORTH)

                        guideDirection(GuideDirection.NORTH, guider.calibrationStep)

                        break
                    }

                    // This calculation is reversed from the ra calculation, because
                    // that one was calibrating WEST, but the angle is really relative
                    // to EAST.
                    if (guider.assumeDECOrthogonalToRA) {
                        val a1 = (calibration.xAngle + PIOVERTWO).normalized - PI
                        val a2 = (calibration.xAngle - PIOVERTWO).normalized - PI
                        val ya = currentLocation.angle(calibrationStartingLocation)
                        val a1y = (a1 - ya).normalized - PI
                        val a2y = (a2 - ya).normalized - PI
                        val yAngle = if (abs(a1y) < abs(a2y)) a1 else a2
                        val decDist = dist * (yAngle - calibration.yAngle).cos
                        val yRate = decDist / (calibrationSteps * guider.calibrationStep)

                        calibration = calibration.copy(yAngle = yAngle, yRate = yRate, decGuideParity = GuideParity.UNKNOWN)

                        LOG.d { info("assuming orthogonal axes: measured Y angle={}, X angle={}, orthogonal={},{}, best = {}, dist = {}, decDist = {}", yAngle.toDegrees, calibration.xAngle.toDegrees, a1.toDegrees, a2.toDegrees, yAngle.toDegrees, dist, decDist) }
                    } else {
                        val yAngle = currentLocation.angle(calibrationStartingLocation)
                        val yRate = dist / (calibrationSteps * guider.calibrationStep)

                        calibration = calibration.copy(yAngle = yAngle, yRate = yRate, decGuideParity = GuideParity.UNKNOWN)
                    }

                    decSteps = calibrationSteps

                    if (calibrationStartingCoords.valid) {
                        val endingCoords = Point(guider.mount.rightAscension.toHours, guider.mount.declination.toDegrees, true)

                        // True Northward motion increases DEC.
                        val ddec = endingCoords.y - calibrationStartingCoords.y
                        calibration = calibration.copy(
                            decGuideParity = if (ddec < -ONE_ARCSEC_DEGREES) GuideParity.ODD
                            else if (ddec > ONE_ARCSEC_DEGREES) GuideParity.EVEN
                            else calibration.decGuideParity,
                        )
                    }

                    LOG.d { info("North calibration completes with angle={} rate={} parity={}", calibration.yAngle.toDegrees, calibration.yRate * 1000.0, calibration.decGuideParity) }

                    // For GO_SOUTH m_recenterRemaining contains the total remaining duration.
                    // Choose the largest pulse size that will not lose the guide star or exceed
                    // the user-specified max pulse.
                    recenterRemaining = calibrationSteps * guider.calibrationStep

                    if (guider.fastRecenterEnabled) {
                        recenterDuration = floor(0.8 * guider.searchRegion / calibration.yRate).toInt()
                        if (recenterDuration > guider.maxDECDuration) recenterDuration = guider.maxDECDuration
                        if (recenterDuration < guider.calibrationStep) recenterDuration = guider.calibrationStep
                    } else {
                        recenterDuration = guider.calibrationStep
                    }

                    calibrationSteps = (recenterRemaining + recenterDuration - 1) / recenterDuration
                    calibrationState = CalibrationState.GO_SOUTH
                    southStartingLocation.set(currentLocation)
                }
                CalibrationState.GO_SOUTH -> {
                    if (recenterRemaining > 0) {
                        var duration = recenterDuration
                        if (duration > recenterRemaining) duration = recenterRemaining

                        LOG.d { info("South step {}, dist={}", calibrationSteps, dist) }

                        calibrationStatus(GuideDirection.SOUTH)

                        recenterRemaining -= duration
                        calibrationSteps--

                        guideDirection(GuideDirection.SOUTH, duration)

                        break
                    }

                    lastLocation.set(currentLocation)

                    // Compute the vector for the north moves we made - use it to make sure any nudging is going in the correct direction.
                    // These are the direction cosines of the vector.
                    northDirCosX = calibrationInitialLocation.dX(southStartingLocation) / calibrationInitialLocation.distance(southStartingLocation)
                    northDirCosY = calibrationInitialLocation.dY(southStartingLocation) / calibrationInitialLocation.distance(southStartingLocation)

                    // Get magnitude and sign convention for the south moves we already made.
                    totalSouthAmt = mountCoords(southStartingLocation - lastLocation, calibration.xAngle, calibration.yAngle).y
                    calibrationState = CalibrationState.NUDGE_SOUTH
                    calibrationSteps = 0
                }
                CalibrationState.NUDGE_SOUTH -> {
                    // Nudge further South on DEC, get within 2 px North/South of starting point,
                    // don't try more than 3 times and don't do nudging at all if
                    // we're starting too far away from the target.
                    val nudgeAmt = currentLocation.distance(calibrationInitialLocation)
                    // Compute the direction cosines for the expected nudge op.
                    val nudgeDirCosX = currentLocation.dX(calibrationInitialLocation) / nudgeAmt
                    val nudgeDirCosY = currentLocation.dY(calibrationInitialLocation) / nudgeAmt
                    // Compute the angle between the nudge and north move vector - they should be reversed,
                    // i.e. something close to 180 deg.
                    val cosTheta = nudgeDirCosX * northDirCosX + nudgeDirCosY * northDirCosY
                    val theta = acos(cosTheta).rad

                    LOG.d { info("nudge. theta={} deg", theta.toDegrees) }

                    // We're going at least roughly in the right direction.
                    if (abs(abs(theta.toDegrees) - 180.0) < 40.0) {
                        if (calibrationSteps <= MAX_NUDGES
                            && nudgeAmt > NUDGE_TOLERANCE
                            && nudgeAmt < distCrit + blDistanceMoved
                        ) {
                            // Compute how much more south we need to go.
                            var decAmt = mountCoords(currentLocation - calibrationInitialLocation, calibration.xAngle, calibration.yAngle).y
                            LOG.d { info("South nudging, decAmt = {}, normal south moves = {}", decAmt, totalSouthAmt) }

                            // Still need to move south to reach target based on matching sign.
                            if (decAmt * totalSouthAmt > 0.0) {
                                // Sign doesn't matter now, we're always moving south.
                                decAmt = abs(decAmt)
                                decAmt = min(decAmt, guider.searchRegion.toDouble())
                                var pulseAmt = floor(decAmt / calibration.yRate).toInt()
                                // Be conservative, use durations that pushed us north in the first place.
                                if (pulseAmt > guider.calibrationStep) pulseAmt = guider.calibrationStep

                                LOG.d { info("sending nudge South pulse of duration {} ms", pulseAmt) }

                                calibrationSteps++

                                calibrationStatus(GuideDirection.SOUTH)

                                guideDirection(GuideDirection.SOUTH, pulseAmt)

                                break
                            }
                        }
                    } else {
                        LOG.d { info("nudging discontinued, wrong direction: {}", theta) }
                    }

                    LOG.d { info("final south nudging status: current location={},{}, targeting={},{}", currentLocation.x, currentLocation.y, calibrationInitialLocation.x, calibrationInitialLocation.y) }

                    calibrationState = CalibrationState.COMPLETE
                }
                CalibrationState.COMPLETE -> {
                    calibration = calibration.copy(
                        declination = guider.mount.declination,
                        pierSideAtEast = guider.mount.isPierSideAtEast,
                        rotatorAngle = guider.rotator?.angle ?: 0.0,
                        binning = guider.camera.binning,
                    )

                    load(calibration)

                    LOG.d { info("calibration completed. calibration={}", calibration) }

                    guider.listeners.forEach { it.onCalibrationCompleted(calibration) }

                    break
                }
                else -> Unit
            }
        }

        return true
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
        val binning = guider.camera.binning
        val pierSideAtEast = guider.mount.isPierSideAtEast
        val declination = guider.mount.declination
        val rotatorAngle = guider.rotator?.angle ?: 0.0
        var scaleAdjustment = 1.0

        if (calibration.binning != binning) {
            scaleAdjustment *= binning.toDouble() / calibration.binning
            val rateAdjustment = calibration.binning / binning.toDouble()
            val xRate = calibration.xRate * rateAdjustment
            val yRate = calibration.yRate * rateAdjustment

            LOG.d { info("binning changed. bin={} to {}, xRate={} to {}, yRate={} to {}", calibration.binning, binning, calibration.xRate, xRate, calibration.yRate, yRate) }

            load(calibration.copy(xRate = xRate, yRate = yRate, binning = binning))
        }

        // If the image scale has changed, make some other adjustments.
        if (abs(scaleAdjustment - 1.0) >= 0.01) {
            LOG.d { info("image scale ratio changed. scaleAdjustment={}", scaleAdjustment) }
            clear()
        }

        if (pierSideAtEast != calibration.pierSideAtEast) {
            LOG.d { info("guiding starts on opposite side of pier") }
            flip()
        }

        if (rotatorAngle.isFinite()) {
            if (!calibration.rotatorAngle.isFinite()) {
                // we do not know the rotator position at calibration time so
                // cannot automatically adjust calibration.
                calibration = calibration.copy(rotatorAngle = rotatorAngle)
            } else {
                val da = rotatorAngle - calibration.rotatorAngle

                LOG.d { info("new rotator position {} deg, prev={} deg, delta={} deg", rotatorAngle.toDegrees, calibration.rotatorAngle.toDegrees, da.toDegrees) }

                val xAngle = (calibration.xAngle - da).normalized - PI
                val yAngle = (calibration.yAngle - da).normalized - PI

                load(calibration.copy(xAngle = xAngle, yAngle = yAngle))
            }
        }

        var declinationCompensated = false

        if (declination != calibration.declination) {
            if (!guider.useDECCompensation) {
                LOG.d { info("skipping declination compensation") }
            } else {
                // Don't do a full dec comp too close to pole - xRate will become
                // a huge number and will cause problems downstream.
                xRate = (calibration.xRate / calibration.declination.cos) * declination.cos
                LOG.d { info("declination compensation. xRate={} to {}", calibration.xRate, xRate) }
                declinationCompensated = true
            }
        }

        if (!declinationCompensated) {
            xRate = calibration.xRate
        }
    }

    private fun guideDirection(direction: GuideDirection, duration: Int) {
        if (guider.guideDirection(direction, duration)) {
            Thread.sleep(duration.toLong())
        }
    }

    companion object {

        private const val CALIBRATION_RATE_UNCALIBRATED = 123E4
        private const val MAX_CALIBRATION_STEPS = 60
        private const val ONE_ARCSEC_HOURS = 24.0 / (360.0 * 60.0 * 60.0)
        private const val ONE_ARCSEC_DEGREES = 1.0 / (60.0 * 60.0)
        private const val BL_MIN_COUNT = 3
        private const val BL_MAX_CLEARING_TIME = 60000
        private const val BL_MIN_CLEARING_DISTANCE = 3
        private const val MAX_NUDGES = 3
        private const val NUDGE_TOLERANCE = 2.0

        private val LOG = loggerFor<GuideCalibrator>()

        private fun mountCoords(camera: Point, x: Angle, y: Angle): Point {
            val length = camera.length
            val cameraTheta = camera.angle
            val yAngleError = ((x - y) + PIOVERTWO).normalized - PI
            val xAngle = cameraTheta - x
            val yAngle = cameraTheta - (x + yAngleError)
            return Point(length * xAngle.cos, length * yAngle.sin)
        }
    }
}
