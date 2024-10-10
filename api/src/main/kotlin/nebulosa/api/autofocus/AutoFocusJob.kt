package nebulosa.api.autofocus

import nebulosa.api.cameras.*
import nebulosa.api.focusers.BacklashCompensationFocuserMoveTask
import nebulosa.api.focusers.BacklashCompensationMode
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.autofocus.AutoFocus
import nebulosa.autofocus.AutoFocusListener
import nebulosa.autofocus.AutoFocusResult
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.Task
import nebulosa.log.loggerFor
import nebulosa.stardetector.StarDetector
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

data class AutoFocusJob(
    @JvmField val autoFocusExecutor: AutoFocusExecutor,
    @JvmField val camera: Camera,
    @JvmField val focuser: Focuser,
    @JvmField val request: AutoFocusRequest,
    @JvmField val starDetector: StarDetector<Path>,
) : AbstractJob(), CameraEventAware, FocuserEventAware, AutoFocusListener {

    @JvmField val cameraRequest = request.capture.copy(
        savePath = CAPTURE_SAVE_PATH,
        exposureTime = maxOf(request.capture.exposureTime, MIN_EXPOSURE_TIME),
        frameType = FrameType.LIGHT,
        autoSave = false,
        autoSubFolderMode = AutoSubFolderMode.OFF,
    )

    private val cameraExposureTask = CameraExposureTask(this, camera, cameraRequest)
    private val backlashCompensationFocuserMoveTask = BacklashCompensationFocuserMoveTask(this, focuser, 0, request.backlashCompensation)
    private val reverse = request.backlashCompensation.mode == BacklashCompensationMode.OVERSHOOT && request.backlashCompensation.backlashIn > 0
    private val finished = AtomicBoolean()

    private val autoFocus = AutoFocus(
        starDetector, request.capture.exposureAmount,
        request.initialOffsetSteps, request.stepSize, request.fittingMode,
        request.rSquaredThreshold, reverse, focuser.maxPosition,
    )

    @Volatile private var initialFocusPosition = 0

    @JvmField val status = AutoFocusEvent(camera)

    init {
        initialFocusPosition = focuser.position

        val result = autoFocus.determinate(focuser.position)
        result.handle()
    }

    private fun AutoFocusResult.handle() {
        when (this) {
            AutoFocusResult.Determinate -> return
            AutoFocusResult.TakeExposure -> {
                add(cameraExposureTask)
            }
            is AutoFocusResult.MoveFocuser -> {
                backlashCompensationFocuserMoveTask.position = if (relative) focuser.position + position else position
                add(backlashCompensationFocuserMoveTask)
            }
            is AutoFocusResult.Completed -> {
                status.determinedFocusPoint = determinedFocusPoint
                status.starHFD = determinedFocusPoint.y
                status.state = AutoFocusState.FINISHED
                status.send()

                movingFocuserToDeterminedFocusPosition(determinedFocusPoint)
            }
            is AutoFocusResult.Failed -> {
                status.determinedFocusPoint = null
                status.starCount = 0
                status.starHFD = 0.0
                status.state = AutoFocusState.FAILED
                status.send()

                restoringFocuserToInitialPosition()
            }
        }
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun handleFocuserEvent(event: FocuserEvent) {
        backlashCompensationFocuserMoveTask.handleFocuserEvent(event)
    }

    override fun beforeStart() {
        LOG.debug("Auto Focus started. reverse={}, request={}, camera={}, focuser={}", reverse, request, camera, focuser)

        autoFocus.registerAutoFocusListener(this)
        finished.set(false)

        camera.snoop(camera.snoopedDevices.filter { it !is Focuser } + focuser)
    }

    override fun beforeTask(task: Task) {
        if (task === backlashCompensationFocuserMoveTask) {
            status.state = AutoFocusState.MOVING
            status.send()
        } else if (task === cameraExposureTask) {
            status.state = AutoFocusState.EXPOSURING
            status.send()
        }
    }

    override fun afterTask(task: Task, exception: Throwable?): Boolean {
        if (exception == null && !finished.get()) {
            status.state = AutoFocusState.ANALYSING
            status.send()

            while (true) {
                val result = try {
                    autoFocus
                        .determinate(focuser.position)
                        .also { it.handle() }
                } catch (e: Throwable) {
                    LOG.error("auto focus determination failed", e)

                    status.state = AutoFocusState.FAILED
                    status.send()

                    restoringFocuserToInitialPosition()

                    return false
                }

                if (result !is AutoFocusResult.Determinate) break
            }

            if (!finished.get()) {
                status.state = AutoFocusState.ANALYSED
                status.send()
            }
        } else {
            status.state = AutoFocusState.FAILED
            status.send()

            restoringFocuserToInitialPosition()
        }

        return super.afterTask(task, exception)
    }

    override fun afterFinish() {
        autoFocus.unregisterAutoFocusListener(this)

        if (!finished.get()) {
            status.state = AutoFocusState.IDLE
            status.capture.state = CameraCaptureState.IDLE
            status.send()
        }

        LOG.debug("Auto Focus finished. camera={}, focuser={}", camera, focuser)
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraExposureEvent -> {
                status.capture.handleCameraExposureEvent(event)

                if (!finished.get()) {
                    if (!isCancelled) {
                        status.state = if (event is CameraExposureFinished) {
                            autoFocus.add(event.savedPath)

                            status.capture.send()
                            AutoFocusState.EXPOSURED
                        } else {
                            AutoFocusState.EXPOSURING
                        }

                        status.send()
                    }
                } else if (event is CameraExposureFinished) {
                    status.capture.send()
                }
            }
        }
    }

    private fun restoringFocuserToInitialPosition() {
        finished.set(true)
        backlashCompensationFocuserMoveTask.position = initialFocusPosition
        backlashCompensationFocuserMoveTask.run()
        cameraExposureTask.run()
    }

    private fun movingFocuserToDeterminedFocusPosition(position: CurvePoint) {
        finished.set(true)
        backlashCompensationFocuserMoveTask.position = position.x.roundToInt()
        backlashCompensationFocuserMoveTask.run()
        cameraExposureTask.run()
    }

    override fun onStarDetected(count: Int, hfd: Double, stdDev: Double, afterExposures: Boolean) {
        status.starCount = count
        status.starHFD = hfd
    }

    override fun onCurveFitted(
        predictedFocusPoint: CurvePoint?,
        minX: Double, minY: Double,
        maxX: Double, maxY: Double,
        trendLine: TrendLineFitting.Curve?, parabolic: QuadraticFitting.Curve?, hyperbolic: HyperbolicFitting.Curve?
    ) {
        status.state = AutoFocusState.CURVE_FITTED
        status.chart = AutoFocusEvent.Chart(predictedFocusPoint, minX, minY, maxX, maxY, trendLine, parabolic, hyperbolic)
        status.send()
        status.chart = null
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MessageEvent.send() {
        autoFocusExecutor.accept(this)
    }

    companion object {

        @JvmStatic private val MIN_EXPOSURE_TIME = Duration.ofSeconds(1L)
        @JvmStatic private val CAPTURE_SAVE_PATH = Files.createTempDirectory("af-")
        @JvmStatic private val LOG = loggerFor<AutoFocusJob>()
    }
}
