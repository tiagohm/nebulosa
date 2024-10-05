package nebulosa.api.autofocus

import nebulosa.api.cameras.*
import nebulosa.api.focusers.BacklashCompensationFocuserMoveTask
import nebulosa.api.focusers.BacklashCompensationMode
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.message.MessageEvent
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
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.LoopTask
import nebulosa.job.manager.Task
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.math.max
import kotlin.math.roundToInt

data class AutoFocusJob(
    @JvmField val autoFocusExecutor: AutoFocusExecutor,
    @JvmField val camera: Camera,
    @JvmField val focuser: Focuser,
    @JvmField val request: AutoFocusRequest,
    @JvmField val starDetection: StarDetector<Path>,
) : AbstractJob(), CameraEventAware, FocuserEventAware {

    data class MeasuredStars(@JvmField val hfd: Double, @JvmField val stdDev: Double) {

        companion object {

            @JvmStatic val EMPTY = MeasuredStars(0.0, 0.0)
        }
    }

    @JvmField val cameraRequest = request.capture.copy(
        savePath = CAPTURE_SAVE_PATH,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT,
        autoSave = false,
        autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val cameraExposureTask = CameraExposureTask(this, camera, cameraRequest)
    private val backlashCompensationFocuserMoveTask = BacklashCompensationFocuserMoveTask(this, focuser, 0, request.backlashCompensation)
    private val initialHFDTask = InitialHFDTask()
    private val reverse = request.backlashCompensation.mode == BacklashCompensationMode.OVERSHOOT && request.backlashCompensation.backlashIn > 0
    private val offsetSteps = request.initialOffsetSteps
    private val numberOfSteps = offsetSteps + 1
    private val obtainFocusPointsTask = ObtainFocusPointsTask(numberOfSteps, offsetSteps, reverse)
    private val morePointsNeededToTheLeftTask = MorePointsNeededToTheLeftTask()
    private val morePointsNeededToTheRightTask = MorePointsNeededToTheRightTask()
    private val computeTrendLineCountTask = ComputeTrendLineCountTask()
    private val computeFinalFocusPointTask = ComputeFinalFocusPointTask()
    private val maximumFocusPoints = request.capture.exposureAmount * request.initialOffsetSteps * 10
    private val measurements = ArrayList<MeasuredStars>(request.capture.exposureAmount)
    private val focusPoints = ArrayList<CurvePoint>(maximumFocusPoints)

    @Volatile private var initialFocusPosition = 0
    @Volatile private var initialHFD = MeasuredStars.EMPTY
    @Volatile private var measurementResult = MeasuredStars.EMPTY
    @Volatile private var numberOfAttempts = 0

    @Volatile private var trendLineCurve: TrendLineFitting.Curve? = null
    @Volatile private var parabolicCurve: QuadraticFitting.Curve? = null
    @Volatile private var hyperbolicCurve: HyperbolicFitting.Curve? = null
    @Volatile private var leftCount = 0
    @Volatile private var rightCount = 0

    @JvmField val status = AutoFocusEvent(camera)

    init {
        // Get initial position information, as average of multiple exposures, if configured this way.
        if (request.rSquaredThreshold <= 0.0) {
            add(initialHFDTask)
        }

        add(obtainFocusPointsTask)
        add(computeTrendLineCountTask)

        LoopTask(this, listOf(morePointsNeededToTheLeftTask, morePointsNeededToTheRightTask, computeTrendLineCountTask)) { _, _ ->
            (leftCount > 0 || rightCount > 0) && !isDataPointsEnough
        }.also(::add)

        add(computeFinalFocusPointTask)
    }

    val isDataPointsEnough
        get() = trendLineCurve != null && (rightCount + focusPoints.count { it.x > trendLineCurve!!.minimum.x && it.y == 0.0 } >= offsetSteps && leftCount + focusPoints.count { it.x < trendLineCurve!!.minimum.x && it.y == 0.0 } >= offsetSteps)

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun handleFocuserEvent(event: FocuserEvent) {
        backlashCompensationFocuserMoveTask.handleFocuserEvent(event)
    }

    override fun beforeStart() {
        initialFocusPosition = focuser.position

        LOG.debug { "Auto Focus started. reverse=$reverse, request=$request, camera=$camera, focuser=$focuser" }
    }

    override fun canRun(prev: Task?, current: Task): Boolean {
        if (current === initialHFDTask) {
            return numberOfAttempts == 0
        } else if (current === morePointsNeededToTheLeftTask) {
            return trendLineCurve!!.left.points.size < offsetSteps &&
                    focusPoints.count { it.x < trendLineCurve!!.minimum.x && it.y == 0.0 } < offsetSteps
        } else if (current === morePointsNeededToTheRightTask) {
            return trendLineCurve!!.right.points.size < offsetSteps &&
                    focusPoints.count { it.x > trendLineCurve!!.minimum.x && it.y == 0.0 } < offsetSteps
        }

        return super.canRun(prev, current)
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraExposureEvent -> {
                status.capture.handleCameraExposureEvent(event)

                if (event is CameraExposureFinished) {
                    status.capture.send()

                    status.state = AutoFocusState.ANALYSING
                    status.send()

                    val detectedStars = starDetection.detect(event.savedPath)
                    status.starCount = detectedStars.size
                    LOG.debug("detected {} stars", status.starCount)

                    val measurement = detectedStars.measureDetectedStars()
                    status.starHFD = measurement.hfd
                    LOG.debug("HFD measurement: hfd={}, stdDev={}", measurement.hfd, measurement.stdDev)
                    measurements.add(measurement)

                    status.state = AutoFocusState.ANALYSED
                } else {
                    status.state = AutoFocusState.EXPOSURING
                }

                status.send()
            }
        }
    }

    private fun evaluateAllMeasurements(): MeasuredStars {
        if (measurements.isEmpty()) MeasuredStars.EMPTY
        if (measurements.size == 1) return measurements[0]
        val descriptiveStatistics = DescriptiveStatistics(measurements.size)
        measurements.forEach { descriptiveStatistics.addValue(it.hfd) }
        return MeasuredStars(descriptiveStatistics.mean, descriptiveStatistics.standardDeviation)
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

            val predictedFocusPoint = status.determinedFocusPoint ?: determineFinalFocusPoint()
            val (minX, minY) = if (focusPoints.isEmpty()) CurvePoint.ZERO else focusPoints[0]
            val (maxX, maxY) = if (focusPoints.isEmpty()) CurvePoint.ZERO else focusPoints[focusPoints.lastIndex]
            status.chart = AutoFocusEvent.Chart(predictedFocusPoint, minX, minY, maxX, maxY, trendLineCurve, parabolicCurve, hyperbolicCurve)

            status.state = AutoFocusState.CURVE_FITTED
            status.copy().send() // TODO: Verificar se é necessário o copy por setar null abaixo.
            status.chart = null
        }
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

    private fun validateCalculatedFocusPosition(focusPoint: CurvePoint, initialHFD: Double): Boolean {
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

        if (isCancelled) return false

        MoveFocuserTask(focusPoint.x.roundToInt(), false).run()

        if (threshold <= 0) {
            TakeExposureTask().run()

            val hfd = measurementResult.hfd

            if (initialHFD != 0.0 && hfd > initialHFD * 1.15) {
                LOG.warn("New focus point HFR $hfd is significantly worse than original HFR $initialHFD")
                return false
            }
        }

        return true
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MessageEvent.send() {
        autoFocusExecutor.accept(this)
    }

    private inner class InitialHFDTask : Task {

        private val takeExposureTask = TakeExposureTask()

        override fun run() {
            takeExposureTask.run()
            initialHFD = measurementResult
        }
    }

    private inner class TakeExposureTask(private val exposureAmount: Int = cameraRequest.exposureAmount) : Task {

        override fun run() {
            measurementResult = if (!isCancelled) {
                measurements.clear()

                status.state = AutoFocusState.EXPOSURING
                status.send()

                repeat(max(1, exposureAmount)) {
                    cameraExposureTask.run()
                }

                evaluateAllMeasurements()
            } else {
                MeasuredStars.EMPTY
            }
        }
    }

    private inner class MoveFocuserTask(
        private val position: Int,
        private val relative: Boolean,
    ) : Task {

        override fun run() {
            status.state = AutoFocusState.MOVING
            status.send()

            backlashCompensationFocuserMoveTask.position = if (relative) focuser.position + position else position
            backlashCompensationFocuserMoveTask.run()
        }
    }

    private inner class ObtainFocusPointsTask(
        private val numberOfSteps: Int,
        private val offset: Int,
        private val reverse: Boolean,
    ) : Task {

        private val stepSize = request.stepSize
        private val direction = if (reverse) -1 else 1

        @JvmField val takeExposureTask = TakeExposureTask()
        @JvmField val initialOffsetMoveFocuserTask = MoveFocuserTask(direction * offset * stepSize, true)
        @JvmField val reversedMoveFocuserTask = MoveFocuserTask(direction * -stepSize, true)

        override fun run() {
            LOG.debug { "retrieving focus points. stepSize=$stepSize, numberOfSteps=$numberOfSteps, offset=$offset, reverse=$reverse" }

            var focusPosition = 0

            if (offset != 0) {
                initialOffsetMoveFocuserTask.run()
                focusPosition = focuser.position
            }

            var remainingSteps = numberOfSteps

            while (!isCancelled && remainingSteps > 0) {
                val currentFocusPosition = focusPosition

                takeExposureTask.run()
                val measurement = measurementResult

                if (isCancelled) break

                LOG.debug { "HFD measured after exposures. mean=$measurement" }

                if (remainingSteps-- > 1) {
                    reversedMoveFocuserTask.run()
                    focusPosition = focuser.position
                }

                if (isCancelled) break

                // If star measurement is 0, we didn't detect any stars or shapes,
                // and want this point to be ignored by the fitting as much as possible.
                if (measurement.hfd == 0.0) {
                    LOG.warn("No stars detected in step")
                } else {
                    val focusPoint = CurvePoint(currentFocusPosition.toDouble(), measurement.hfd, measurement.stdDev)
                    status.focusPoint = focusPoint

                    focusPoints.add(focusPoint)
                    focusPoints.sortBy { it.x }

                    LOG.debug { "focus point added. remainingSteps=$remainingSteps, point=$focusPoint" }

                    computeCurveFittings()
                }
            }
        }
    }

    private inner class MorePointsNeededToTheLeftTask : Task {

        private val obtainFocusPointsTask = ObtainFocusPointsTask(1, -1, false)

        override fun run() {
            LOG.info("more data points needed to the left of the minimum")

            val firstX = focusPoints.first().x.roundToInt()

            // Move to the leftmost point - this should never be necessary since we're already there, but just in case
            if (focuser.position != firstX) {
                MoveFocuserTask(firstX, false).run()
            }

            // More points needed to the left.
            obtainFocusPointsTask.run()
        }
    }

    private inner class MorePointsNeededToTheRightTask : Task {

        private val obtainFocusPointsTask = ObtainFocusPointsTask(1, 1, false)

        override fun run() {
            LOG.info("more data points needed to the right of the minimum")

            val lastX = focusPoints.last().x.roundToInt()

            // More points needed to the right. Let's get to the rightmost point, and keep going right one point at a time.
            if (focuser.position != lastX) {
                MoveFocuserTask(lastX, false).run()
            }

            // More points needed to the right.
            obtainFocusPointsTask.run()
        }
    }

    private inner class ComputeTrendLineCountTask : Task {

        override fun run() {
            leftCount = trendLineCurve?.left?.points?.size ?: 0
            rightCount = trendLineCurve?.right?.points?.size ?: 0
        }
    }

    private inner class ComputeFinalFocusPointTask : Task {

        override fun run() {
            val finalFocusPoint = determineFinalFocusPoint()

            if (finalFocusPoint == null || !validateCalculatedFocusPosition(finalFocusPoint, initialHFD.hfd)) {
                LOG.warn("potentially bad auto-focus. Restoring original focus position")
                MoveFocuserTask(initialFocusPosition, false).run()
            } else {
                status.determinedFocusPoint = finalFocusPoint
                status.state = AutoFocusState.FINISHED
                status.send()

                LOG.info("Auto Focus completed. x={}, y={}", finalFocusPoint.x, finalFocusPoint.y)
            }
        }
    }

    companion object {

        @JvmStatic private val MIN_EXPOSURE_TIME = Duration.ofSeconds(1L)
        @JvmStatic private val CAPTURE_SAVE_PATH = Files.createTempDirectory("af-")
        @JvmStatic private val LOG = loggerFor<AutoFocusJob>()

        @JvmStatic
        private fun List<StarPoint>.measureDetectedStars(): MeasuredStars {
            if (isEmpty()) return MeasuredStars.EMPTY
            val descriptiveStatistics = DescriptiveStatistics(size)
            forEach { descriptiveStatistics.addValue(it.hfd) }
            return MeasuredStars(descriptiveStatistics.mean, descriptiveStatistics.standardDeviation)
        }
    }
}
