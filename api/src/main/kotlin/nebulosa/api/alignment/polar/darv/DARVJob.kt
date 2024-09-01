package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.cameras.CameraExposureEvent
import nebulosa.api.cameras.CameraExposureFinished
import nebulosa.api.cameras.CameraExposureStarted
import nebulosa.api.cameras.CameraExposureTask
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseTask
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.Job
import nebulosa.job.manager.SplitTask
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayEvent
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.nio.file.Files
import java.time.Duration

data class DARVJob(
    @JvmField val darvExecutor: DARVExecutor,
    @JvmField val camera: Camera,
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: DARVStartRequest,
) : AbstractJob(), CameraEventAware {

    @JvmField val cameraExposureRequest = request.capture.copy(
        exposureTime = request.capture.exposureTime + request.capture.exposureDelay,
        savePath = CAPTURE_SAVE_PATH, exposureAmount = 1, exposureDelay = Duration.ZERO,
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val direction = if (request.reversed) request.direction.reversed else request.direction
    private val guidePulseDuration = request.capture.exposureTime.dividedBy(2L)

    private val cameraExposureTask = CameraExposureTask(camera, cameraExposureRequest)
    private val delayTask = DelayTask(request.capture.exposureDelay)
    private val forwardGuidePulseTask = GuidePulseTask(guideOutput, GuidePulseRequest(direction, guidePulseDuration))
    private val backwardGuidePulseTask = GuidePulseTask(guideOutput, GuidePulseRequest(direction.reversed, guidePulseDuration))
    private val delayAndGuidePulseTask = DelayAndGuidePulseTask()
    private val task = SplitTask(listOf(cameraExposureTask, delayAndGuidePulseTask), darvExecutor)

    @JvmField val status = DARVEvent(camera)

    init {
        status.capture.exposureAmount = 1

        add(task)
    }

    @Synchronized
    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> {
                status.state = if (event.task === delayTask) DARVState.INITIAL_PAUSE
                else if (event.task === forwardGuidePulseTask.delayTask) DARVState.FORWARD
                else DARVState.BACKWARD

                with(status.capture) {
                    stepElapsedTime = event.elapsedTime
                    stepRemainingTime = event.remainingTime
                    stepProgress = event.progress
                }
            }
            is CameraExposureEvent -> {
                with(status.capture) {
                    captureRemainingTime = event.remainingTime
                    captureElapsedTime = event.elapsedTime
                    captureProgress = event.progress
                }

                if (event is CameraExposureStarted) {
                    status.capture.exposureCount++
                } else if (event is CameraExposureFinished) {
                    status.capture.savedPath = event.savedPath
                    darvExecutor.accept(status.capture)
                }
            }
        }

        darvExecutor.accept(status)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun beforeStart() {
        LOG.debug { "DARV started. camera=$camera, guideOutput=$guideOutput, request=$request" }
    }

    override fun afterFinish() {
        LOG.debug { "DARV finished. camera=$camera, guideOutput=$guideOutput, request=$request" }
    }

    private inner class DelayAndGuidePulseTask : Task {

        override fun execute(job: Job) {
            delayTask.execute(job)

            status.direction = forwardGuidePulseTask.request.direction
            forwardGuidePulseTask.execute(job)

            status.direction = backwardGuidePulseTask.request.direction
            backwardGuidePulseTask.execute(job)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVJob>()
        @JvmStatic private val CAPTURE_SAVE_PATH = Files.createTempDirectory("darv-")
    }
}
