package nebulosa.api.autofocus

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.*
import nebulosa.api.focusers.BacklashCompensationFocuserMoveTask
import nebulosa.api.focusers.BacklashCompensationMode
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.tasks.AbstractTask
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.CurvePoint.Companion.midPoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.log.loggerFor
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import nebulosa.util.concurrency.cancellation.CancellationToken
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.math.max
import kotlin.math.roundToInt

data class AutoFocusTask(
    @JvmField val camera: Camera,
    @JvmField val focuser: Focuser,
    @JvmField val request: AutoFocusRequest,
    @JvmField val starDetection: StarDetector<Path>,
) : AbstractTask<MessageEvent>(), Consumer<CameraCaptureEvent>, CameraEventAware, FocuserEventAware {

    data class MeasuredStars(@JvmField val hfd: Double, @JvmField val stdDev: Double) {

        companion object {

            @JvmStatic val EMPTY = MeasuredStars(0.0, 0.0)
        }
    }

    @JvmField val cameraRequest = request.capture.copy(
        exposureAmount = 0, exposureDelay = Duration.ZERO,
        savePath = CAPTURE_SAVE_PATH,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val focusPoints = ArrayList<CurvePoint>()
    private val measurements = ArrayList<MeasuredStars>(request.capture.exposureAmount)
    private val cameraCaptureTask = CameraCaptureTask(camera, cameraRequest, focuser = focuser)
    private val focuserMoveTask = BacklashCompensationFocuserMoveTask(focuser, 0, request.backlashCompensation)

    @Volatile private var trendLineCurve: TrendLineFitting.Curve? = null
    @Volatile private var parabolicCurve: QuadraticFitting.Curve? = null
    @Volatile private var hyperbolicCurve: HyperbolicFitting.Curve? = null

    @Volatile private var focusPoint: CurvePoint? = null
    @Volatile private var starCount = 0
    @Volatile private var starHFD = 0.0
    @Volatile private var determinedFocusPoint: CurvePoint? = null

    init {
        cameraCaptureTask.subscribe(this)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraCaptureTask.handleCameraEvent(event)
    }

    override fun handleFocuserEvent(event: FocuserEvent) {
        focuserMoveTask.handleFocuserEvent(event)
    }

    override fun canUseAsLastEvent(event: MessageEvent) = event is AutoFocusEvent

    override fun execute(cancellationToken: CancellationToken) {
        reset()

        val initialFocusPosition = focuser.position

        cameraCaptureTask.initialize(cancellationToken)

        // Get initial position information, as average of multiple exposures, if configured this way.
        val initialHFD = if (request.rSquaredThreshold <= 0.0) takeExposure(cancellationToken) else MeasuredStars.EMPTY
        val reverse = request.backlashCompensation.mode == BacklashCompensationMode.OVERSHOOT && request.backlashCompensation.backlashIn > 0

        LOG.info("Auto Focus started. initialHFD={}, reverse={}, request={}, camera={}, focuser={}", initialHFD, reverse, request, camera, focuser)

        var exited = false
        var numberOfAttempts = 0
        val maximumFocusPoints = request.capture.exposureAmount * request.initialOffsetSteps * 10

        while (!exited && !cancellationToken.isCancelled) {
            numberOfAttempts++

            val offsetSteps = request.initialOffsetSteps
            val numberOfSteps = offsetSteps + 1

            LOG.info("attempt #{}. offsetSteps={}, numberOfSteps={}", numberOfAttempts, offsetSteps, numberOfSteps)

            obtainFocusPoints(numberOfSteps, offsetSteps, reverse, cancellationToken)

            if (cancellationToken.isCancelled) break

            var leftCount = trendLineCurve?.left?.points?.size ?: 0
            var rightCount = trendLineCurve?.right?.points?.size ?: 0

            LOG.info("trend line computed. left=$leftCount, right=$rightCount")

            // When data points are not sufficient analyze and take more.
            do {
                if (leftCount == 0 && rightCount == 0) {
                    LOG.warn("Not enought spreaded points")
                    exited = true
                    break
                }

                LOG.info("data points are not sufficient. attempt={}, numberOfSteps={}", numberOfAttempts, numberOfSteps)

                // Let's keep moving in, one step at a time, until we have enough left trend points.
                // Then we can think about moving out to fill in the right trend points.
                if (trendLineCurve!!.left.points.size < offsetSteps
                    && focusPoints.count { it.x < trendLineCurve!!.minimum.x && it.y == 0.0 } < offsetSteps
                ) {
                    LOG.info("more data points needed to the left of the minimum")

                    // Move to the leftmost point - this should never be necessary since we're already there, but just in case
                    if (focuser.position != focusPoints.first().x.roundToInt()) {
                        moveFocuser(focusPoints.first().x.roundToInt(), cancellationToken, false)
                    }

                    // More points needed to the left.
                    obtainFocusPoints(1, -1, false, cancellationToken)
                } else if (trendLineCurve!!.right.points.size < offsetSteps
                    && focusPoints.count { it.x > trendLineCurve!!.minimum.x && it.y == 0.0 } < offsetSteps
                ) {
                    // Now we can go to the right, if necessary.
                    LOG.info("more data points needed to the right of the minimum")

                    // More points needed to the right. Let's get to the rightmost point, and keep going right one point at a time.
                    if (focuser.position != focusPoints.last().x.roundToInt()) {
                        moveFocuser(focusPoints.last().x.roundToInt(), cancellationToken, false)
                    }

                    // More points needed to the right.
                    obtainFocusPoints(1, 1, false, cancellationToken)
                }

                if (cancellationToken.isCancelled) break

                leftCount = trendLineCurve!!.left.points.size
                rightCount = trendLineCurve!!.right.points.size

                LOG.info("trend line computed. left=$leftCount, right=$rightCount")

                if (maximumFocusPoints < focusPoints.size) {
                    // Break out when the maximum limit of focus points is reached
                    LOG.error("failed to complete. Maximum number of focus points exceeded ($maximumFocusPoints).")
                    break
                }

                if (focuser.position <= 0 || focuser.position >= focuser.maxPosition) {
                    // Break out when the focuser hits the min/max position. It can't continue from there.
                    LOG.error("failed to complete. position reached ${focuser.position}")
                    break
                }
            } while (!cancellationToken.isCancelled && (rightCount + focusPoints.count { it.x > trendLineCurve!!.minimum.x && it.y == 0.0 } < offsetSteps || leftCount + focusPoints.count { it.x < trendLineCurve!!.minimum.x && it.y == 0.0 } < offsetSteps))

            if (exited || cancellationToken.isCancelled) break

            val finalFocusPoint = determineFinalFocusPoint()

            if (finalFocusPoint == null || !validateCalculatedFocusPosition(finalFocusPoint, initialHFD.hfd, cancellationToken)) {
                if (cancellationToken.isCancelled) {
                    break
                } else if (numberOfAttempts < request.totalNumberOfAttempts) {
                    moveFocuser(initialFocusPosition, cancellationToken, false)
                    LOG.warn("potentially bad auto-focus. Reattempting")
                    reset()
                    continue
                } else {
                    LOG.warn("potentially bad auto-focus. Restoring original focus position")
                    exited = true
                }
            } else {
                determinedFocusPoint = finalFocusPoint
                LOG.info("Auto Focus completed. x={}, y={}", finalFocusPoint.x, finalFocusPoint.y)
                break
            }
        }

        cameraCaptureTask.finalize(cancellationToken)

        if (exited || cancellationToken.isCancelled) {
            LOG.warn("Auto Focus did not complete successfully, so restoring the focuser position to $initialFocusPosition")
            sendEvent(if (exited) AutoFocusState.FAILED else AutoFocusState.FINISHED)

            if (exited) {
                moveFocuser(initialFocusPosition, CancellationToken.NONE, false)
            }
        } else {
            sendEvent(AutoFocusState.FINISHED)
        }

        reset()

        LOG.info("Auto Focus finished. camera={}, focuser={}", camera, focuser)
    }

    private fun determineFinalFocusPoint(): CurvePoint? {
        return when (request.fittingMode) {
            AutoFocusFittingMode.TRENDLINES -> trendLineCurve!!.intersection
            AutoFocusFittingMode.PARABOLIC -> parabolicCurve?.minimum
            AutoFocusFittingMode.TREND_PARABOLIC -> parabolicCurve?.minimum?.midPoint(trendLineCurve!!.intersection)
            AutoFocusFittingMode.HYPERBOLIC -> hyperbolicCurve?.minimum
            AutoFocusFittingMode.TREND_HYPERBOLIC -> hyperbolicCurve?.minimum?.midPoint(trendLineCurve!!.intersection)
        }
    }

    private fun evaluateAllMeasurements(): MeasuredStars {
        if (measurements.isEmpty()) MeasuredStars.EMPTY
        if (measurements.size == 1) return measurements[0]
        val descriptiveStatistics = DescriptiveStatistics(measurements.size)
        measurements.forEach { descriptiveStatistics.addValue(it.hfd) }
        return MeasuredStars(descriptiveStatistics.mean, descriptiveStatistics.standardDeviation)
    }

    override fun accept(event: CameraCaptureEvent) {
        if (event.state == CameraCaptureState.EXPOSURE_FINISHED) {
            sendEvent(AutoFocusState.EXPOSURED, event)
            sendEvent(AutoFocusState.ANALYSING)
            val detectedStars = starDetection.detect(event.savedPath!!)
            starCount = detectedStars.size
            LOG.info("detected $starCount stars")
            val measurement = detectedStars.measureDetectedStars()
            LOG.info("HFD measurement: hfd={}, stdDev={}", measurement.hfd, measurement.stdDev)
            measurements.add(measurement)
            sendEvent(AutoFocusState.ANALYSED)
            onNext(event)
        } else {
            sendEvent(AutoFocusState.EXPOSURING, event)
        }
    }

    private fun takeExposure(cancellationToken: CancellationToken): MeasuredStars {
        return if (!cancellationToken.isCancelled) {
            measurements.clear()
            sendEvent(AutoFocusState.EXPOSURING)
            cameraCaptureTask.executeUntil(cancellationToken, max(1, request.capture.exposureAmount))
            evaluateAllMeasurements()
        } else {
            MeasuredStars.EMPTY
        }
    }

    private fun obtainFocusPoints(numberOfSteps: Int, offset: Int, reverse: Boolean, cancellationToken: CancellationToken) {
        val stepSize = request.stepSize
        val direction = if (reverse) -1 else 1

        LOG.info("retrieving focus points. numberOfSteps={}, offset={}, reverse={}", numberOfSteps, offset, reverse)

        var focusPosition = 0

        if (offset != 0) {
            focusPosition = moveFocuser(direction * offset * stepSize, cancellationToken, true)
        }

        var remainingSteps = numberOfSteps

        while (!cancellationToken.isCancelled && remainingSteps > 0) {
            val currentFocusPosition = focusPosition

            val measurement = takeExposure(cancellationToken)

            if (cancellationToken.isCancelled) break

            LOG.info("HFD measured after exposures. mean={}", measurement)

            if (remainingSteps-- > 1) {
                focusPosition = moveFocuser(direction * -stepSize, cancellationToken, true)
            }

            if (cancellationToken.isCancelled) break

            // If star measurement is 0, we didn't detect any stars or shapes,
            // and want this point to be ignored by the fitting as much as possible.
            if (measurement.hfd == 0.0) {
                LOG.warn("No stars detected in step")
            } else {
                focusPoint = CurvePoint(currentFocusPosition.toDouble(), measurement.hfd, measurement.stdDev)
                focusPoints.add(focusPoint!!)
                focusPoints.sortBy { it.x }

                LOG.info("focus point added. remainingSteps={}, point={}", remainingSteps, focusPoint)

                computeCurveFittings()
            }
        }
    }

    private fun computeCurveFittings() {
        with(focusPoints) {
            trendLineCurve = TrendLineFitting.calculate(this)

            if (size >= 3) {
                if (request.fittingMode == AutoFocusFittingMode.PARABOLIC || request.fittingMode == AutoFocusFittingMode.TREND_PARABOLIC) {
                    parabolicCurve = QuadraticFitting.calculate(this)
                } else if (request.fittingMode == AutoFocusFittingMode.HYPERBOLIC || request.fittingMode == AutoFocusFittingMode.TREND_HYPERBOLIC) {
                    hyperbolicCurve = HyperbolicFitting.calculate(this)
                }
            }

            sendEvent(AutoFocusState.CURVE_FITTED)
        }
    }

    private fun validateCalculatedFocusPosition(focusPoint: CurvePoint, initialHFD: Double, cancellationToken: CancellationToken): Boolean {
        val threshold = request.rSquaredThreshold

        LOG.info("validating calculated focus position. threshold={}", threshold)

        if (threshold > 0.0) {
            fun isTrendLineBad() = trendLineCurve?.let { it.left.rSquared < threshold || it.right.rSquared < threshold } != false
            fun isParabolicBad() = parabolicCurve?.let { it.rSquared < threshold } != false
            fun isHyperbolicBad() = hyperbolicCurve?.let { it.rSquared < threshold } != false

            val isBad = when (request.fittingMode) {
                AutoFocusFittingMode.TRENDLINES -> isTrendLineBad()
                AutoFocusFittingMode.PARABOLIC -> isParabolicBad()
                AutoFocusFittingMode.TREND_PARABOLIC -> isParabolicBad() || isTrendLineBad()
                AutoFocusFittingMode.HYPERBOLIC -> isHyperbolicBad()
                AutoFocusFittingMode.TREND_HYPERBOLIC -> isHyperbolicBad() || isTrendLineBad()
            }

            if (isBad) {
                LOG.error("coefficient of determination is below threshold")
                return false
            }
        }

        val min = focusPoints.first().x
        val max = focusPoints.last().x

        if (focusPoint.x < min || focusPoint.x > max) {
            LOG.error("determined focus point position is outside of the overall measurement points of the curve")
            return false
        }

        if (cancellationToken.isCancelled) return false

        moveFocuser(focusPoint.x.roundToInt(), cancellationToken, false)
        val (hfd) = takeExposure(cancellationToken)

        if (threshold <= 0) {
            if (initialHFD != 0.0 && hfd > initialHFD * 1.15) {
                LOG.warn("New focus point HFR $hfd is significantly worse than original HFR $initialHFD")
                return false
            }
        }

        return true
    }

    private fun moveFocuser(position: Int, cancellationToken: CancellationToken, relative: Boolean): Int {
        sendEvent(AutoFocusState.MOVING)
        focuserMoveTask.position = if (relative) focuser.position + position else position
        focuserMoveTask.execute(cancellationToken)
        return focuser.position
    }

    private fun sendEvent(state: AutoFocusState, capture: CameraCaptureEvent? = null) {
        val chart = when (state) {
            AutoFocusState.FINISHED,
            AutoFocusState.CURVE_FITTED -> {
                val predictedFocusPoint = determinedFocusPoint ?: determineFinalFocusPoint()
                val (minX, minY) = if (focusPoints.isEmpty()) CurvePoint.ZERO else focusPoints[0]
                val (maxX, maxY) = if (focusPoints.isEmpty()) CurvePoint.ZERO else focusPoints[focusPoints.lastIndex]
                AutoFocusEvent.Chart(predictedFocusPoint, minX, minY, maxX, maxY, trendLineCurve, parabolicCurve, hyperbolicCurve)
            }
            else -> null
        }

        onNext(AutoFocusEvent(state, focusPoint, determinedFocusPoint, starCount, starHFD, chart, capture))
    }

    override fun reset() {
        cameraCaptureTask.reset()
        focusPoints.clear()

        trendLineCurve = null
        parabolicCurve = null
        hyperbolicCurve = null
    }

    override fun close() {
        super.close()
        cameraCaptureTask.close()
    }

    companion object {

        @JvmStatic private val MIN_EXPOSURE_TIME = Duration.ofSeconds(1L)
        @JvmStatic private val CAPTURE_SAVE_PATH = Files.createTempDirectory("af-")
        @JvmStatic private val LOG = loggerFor<AutoFocusTask>()

        @JvmStatic
        private fun List<StarPoint>.measureDetectedStars(): MeasuredStars {
            if (isEmpty()) return MeasuredStars.EMPTY
            val descriptiveStatistics = DescriptiveStatistics(size)
            forEach { descriptiveStatistics.addValue(it.hfd) }
            return MeasuredStars(descriptiveStatistics.mean, descriptiveStatistics.standardDeviation)
        }
    }
}
