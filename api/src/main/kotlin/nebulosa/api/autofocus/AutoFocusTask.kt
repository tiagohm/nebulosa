package nebulosa.api.autofocus

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.*
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.focusers.FocuserMoveAbsoluteTask
import nebulosa.api.focusers.FocuserMoveRelativeTask
import nebulosa.api.focusers.FocuserMoveTask
import nebulosa.api.image.ImageBucket
import nebulosa.api.messages.MessageEvent
import nebulosa.api.tasks.AbstractTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.CurvePoint.Companion.midPoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting
import nebulosa.image.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.log.loggerFor
import nebulosa.star.detection.ImageStar
import nebulosa.star.detection.StarDetector
import java.nio.file.Files
import java.time.Duration
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class AutoFocusTask(
    @JvmField val camera: Camera,
    @JvmField val focuser: Focuser,
    @JvmField val request: AutoFocusRequest,
    @JvmField val starDetection: StarDetector<Image>,
    @JvmField val imageBucket: ImageBucket,
) : AbstractTask<MessageEvent>(), Consumer<CameraCaptureEvent>, CameraEventAware, FocuserEventAware {

    data class MeasuredStars(
        @JvmField val averageHFD: Double = 0.0,
        @JvmField val hfdStandardDeviation: Double = 0.0,
    ) {

        companion object {

            @JvmStatic val ZERO = MeasuredStars()
        }
    }

    @JvmField val cameraRequest = request.capture.copy(
        exposureAmount = 0, exposureDelay = Duration.ZERO,
        savePath = Files.createTempDirectory("af"),
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val focusPoints = ArrayList<CurvePoint>()
    private val measurements = ArrayList<MeasuredStars>(request.capture.exposureAmount)
    private val cameraCaptureTask = CameraCaptureTask(camera, cameraRequest, exposureMaxRepeat = max(1, request.capture.exposureAmount))

    @Volatile private var focuserMoveTask: FocuserMoveTask? = null
    @Volatile private var trendLineCurve: TrendLineFitting.Curve? = null
    @Volatile private var parabolicCurve: Lazy<QuadraticFitting.Curve>? = null
    @Volatile private var hyperbolicCurve: Lazy<HyperbolicFitting.Curve>? = null

    @Volatile private var focusPoint = CurvePoint.ZERO

    init {
        cameraCaptureTask.subscribe(this)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraCaptureTask.handleCameraEvent(event)
    }

    override fun handleFocuserEvent(event: FocuserEvent) {
        focuserMoveTask?.handleFocuserEvent(event)
    }

    override fun canUseAsLastEvent(event: MessageEvent) = event is AutoFocusEvent

    override fun execute(cancellationToken: CancellationToken) {
        reset()

        val initialFocusPosition = focuser.position

        // Get initial position information, as average of multiple exposures, if configured this way.
        val initialHFD = if (request.rSquaredThreshold <= 0.0) takeExposure(cancellationToken).averageHFD else Double.NaN
        val reverse = request.backlashCompensationMode == BacklashCompensationMode.OVERSHOOT && request.backlashIn > 0 && request.backlashOut == 0

        LOG.info("Auto Focus started. initialHFD={}, reverse={}, camera={}, focuser={}", initialHFD, reverse, camera, focuser)

        var exited = false
        var numberOfAttempts = 0
        val maximumFocusPoints = request.capture.exposureAmount * request.initialOffsetSteps * 10

        camera.snoop(listOf(focuser))

        while (!exited && !cancellationToken.isCancelled) {
            numberOfAttempts++

            val offsetSteps = request.initialOffsetSteps
            val numberOfSteps = offsetSteps + 1

            LOG.info("attempt #{}. offsetSteps={}, numberOfSteps={}", numberOfAttempts, offsetSteps, numberOfSteps)

            obtainFocusPoints(numberOfSteps, offsetSteps, reverse, cancellationToken)

            var leftCount = trendLineCurve!!.left.points.size
            var rightCount = trendLineCurve!!.right.points.size

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

                leftCount = trendLineCurve!!.left.points.size
                rightCount = trendLineCurve!!.right.points.size

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

            if (exited) break

            val finalFocusPoint = determineFinalFocusPoint()
            val goodAutoFocus = validateCalculatedFocusPosition(finalFocusPoint, initialHFD, cancellationToken)

            if (!goodAutoFocus) {
                if (numberOfAttempts < request.totalNumberOfAttempts) {
                    moveFocuser(initialFocusPosition, cancellationToken, false)
                    LOG.warn("potentially bad auto-focus. reattempting")
                    reset()
                    continue
                } else {
                    LOG.warn("potentially bad auto-focus. Restoring original focus position")
                }
            } else {
                LOG.info("Auto Focus completed. x={}, y={}", finalFocusPoint.x, finalFocusPoint.y)
            }

            exited = true
        }

        if (exited) {
            sendEvent(AutoFocusState.FAILED)
            LOG.warn("Auto Focus did not complete successfully, so restoring the focuser position to $initialFocusPosition")
            moveFocuser(initialFocusPosition, CancellationToken.NONE, false)
        }

        reset()
        sendEvent(AutoFocusState.FINISHED)

        LOG.info("Auto Focus finished. camera={}, focuser={}", camera, focuser)
    }

    private fun determineFinalFocusPoint(): CurvePoint {
        val trendLine by lazy { TrendLineFitting.calculate(focusPoints) }
        val hyperbolic by lazy { HyperbolicFitting.calculate(focusPoints) }
        val parabolic by lazy { QuadraticFitting.calculate(focusPoints) }

        return when (request.fittingMode) {
            AutoFocusFittingMode.TRENDLINES -> trendLine.intersection
            AutoFocusFittingMode.PARABOLIC -> parabolic.minimum
            AutoFocusFittingMode.TREND_PARABOLIC -> trendLine.intersection midPoint parabolic.minimum
            AutoFocusFittingMode.HYPERBOLIC -> hyperbolic.minimum
            AutoFocusFittingMode.TREND_HYPERBOLIC -> trendLine.intersection midPoint hyperbolic.minimum
        }
    }

    private fun evaluateAllMeasurements(): MeasuredStars {
        var sumHFD = 0.0
        var sumVariances = 0.0

        for ((averageHFD, hfdStandardDeviation) in measurements) {
            sumHFD += averageHFD
            sumVariances += hfdStandardDeviation * hfdStandardDeviation
        }

        return MeasuredStars(sumHFD / measurements.size, sqrt(sumVariances / measurements.size))
    }

    override fun accept(event: CameraCaptureEvent) {
        if (event.state == CameraCaptureState.EXPOSURE_FINISHED) {
            sendEvent(AutoFocusState.COMPUTING, capture = event)
            val image = imageBucket.open(event.savePath!!)
            val detectedStars = starDetection.detect(image)
            LOG.info("detected ${detectedStars.size} stars")
            val measure = detectedStars.measureDetectedStars()
            LOG.info("HFD measurement. mean={}, stdDev={}", measure.averageHFD, measure.hfdStandardDeviation)
            measurements.add(measure)
            onNext(event)
        } else {
            sendEvent(AutoFocusState.EXPOSURING, capture = event)
        }
    }

    private fun takeExposure(cancellationToken: CancellationToken): MeasuredStars {
        return if (!cancellationToken.isCancelled) {
            measurements.clear()
            sendEvent(AutoFocusState.EXPOSURING)
            cameraCaptureTask.execute(cancellationToken)
            evaluateAllMeasurements()
        } else {
            MeasuredStars.ZERO
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

            if (remainingSteps > 1) {
                focusPosition = moveFocuser(direction * -stepSize, cancellationToken, true)
            }

            val measurement = takeExposure(cancellationToken)

            // If star measurement is 0, we didn't detect any stars or shapes,
            // and want this point to be ignored by the fitting as much as possible.
            if (measurement.averageHFD == 0.0) {
                LOG.warn("No stars detected in step")
                sendEvent(AutoFocusState.FAILED)
            } else {
                focusPoint = CurvePoint(currentFocusPosition.toDouble(), measurement.averageHFD)
                focusPoints.add(focusPoint)
                focusPoints.sortBy { it.x }

                LOG.info("focus point added. remainingSteps={}, point={}", remainingSteps, focusPoint)

                computeCurveFittings()

                sendEvent(AutoFocusState.FOCUS_POINT_ADDED)
            }

            remainingSteps--
        }
    }

    private fun computeCurveFittings() {
        with(focusPoints.toList()) {
            trendLineCurve = TrendLineFitting.calculate(this)

            if (size >= 3) {
                if (request.fittingMode == AutoFocusFittingMode.PARABOLIC || request.fittingMode == AutoFocusFittingMode.TREND_PARABOLIC) {
                    parabolicCurve = lazy { QuadraticFitting.calculate(this) }
                }
                if (request.fittingMode == AutoFocusFittingMode.HYPERBOLIC || request.fittingMode == AutoFocusFittingMode.TREND_HYPERBOLIC) {
                    hyperbolicCurve = lazy { HyperbolicFitting.calculate(this) }
                }
            }
        }
    }

    private fun validateCalculatedFocusPosition(focusPoint: CurvePoint, initialHFD: Double, cancellationToken: CancellationToken): Boolean {
        val threshold = request.rSquaredThreshold

        fun isTrendLineBad() = trendLineCurve?.let { it.left.rSquared < threshold || it.right.rSquared < threshold } ?: true
        fun isParabolicBad() = parabolicCurve?.value?.let { it.rSquared < threshold } ?: true
        fun isHyperbolicBad() = hyperbolicCurve?.value?.let { it.rSquared < threshold } ?: true

        if (threshold > 0.0) {
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

        val min = focusPoints.minOf { it.x }
        val max = focusPoints.maxOf { it.x }

        if (focusPoint.x < min || focusPoint.y > max) {
            LOG.error("determined focus point position is outside of the overall measurement points of the curve")
            return false
        }

        moveFocuser(focusPoint.x.roundToInt(), cancellationToken, false)
        val hfd = takeExposure(cancellationToken).averageHFD

        if (threshold <= 0) {
            if (initialHFD != 0.0 && hfd > initialHFD * 1.15) {
                LOG.warn("New focus point HFR $hfd is significantly worse than original HFR $initialHFD")
                return false
            }
        }

        return true
    }

    private fun moveFocuser(position: Int, cancellationToken: CancellationToken, relative: Boolean): Int {
        focuserMoveTask = if (relative) FocuserMoveRelativeTask(focuser, position)
        else FocuserMoveAbsoluteTask(focuser, position)
        sendEvent(AutoFocusState.MOVING)
        focuserMoveTask!!.execute(cancellationToken)
        return focuser.position
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendEvent(state: AutoFocusState, capture: CameraCaptureEvent? = null) {
        onNext(AutoFocusEvent(state, focusPoint, capture))
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
        @JvmStatic private val LOG = loggerFor<AutoFocusTask>()

        @JvmStatic
        private fun List<ImageStar>.measureDetectedStars(): MeasuredStars {
            if (isEmpty()) return MeasuredStars.ZERO

            val mean = sumOf { it.hfd } / size

            var stdDev = 0.0

            if (size > 1) {
                for (star in this) {
                    stdDev += (star.hfd - mean).let { it * it }
                }

                stdDev /= size - 1
                stdDev = sqrt(stdDev)
            } else {
                stdDev = Double.NaN
            }

            return MeasuredStars(mean, stdDev)
        }
    }
}
