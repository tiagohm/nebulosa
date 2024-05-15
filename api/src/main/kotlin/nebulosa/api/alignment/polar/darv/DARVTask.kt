package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.cameras.CameraCaptureState
import nebulosa.api.cameras.CameraCaptureTask
import nebulosa.api.guiding.GuidePulseEvent
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseTask
import nebulosa.api.messages.MessageEvent
import nebulosa.api.tasks.AbstractTask
import nebulosa.api.tasks.SplitTask
import nebulosa.api.tasks.Task
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.loggerFor
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.Executor

data class DARVTask(
    @JvmField val camera: Camera,
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: DARVStartRequest,
    private val executor: Executor,
) : AbstractTask<MessageEvent>(), Consumer<Any> {

    @JvmField val cameraRequest = request.capture.copy(
        exposureTime = request.capture.exposureTime + request.capture.exposureDelay,
        savePath = Files.createTempDirectory("darv"),
        exposureAmount = 1, exposureDelay = Duration.ZERO,
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val cameraCaptureTask = CameraCaptureTask(camera, cameraRequest)
    private val delayTask = DelayTask(request.capture.exposureDelay)
    private val forwardGuidePulseTask: GuidePulseTask
    private val backwardGuidePulseTask: GuidePulseTask

    @Volatile private var state = DARVState.IDLE
    @Volatile private var direction: GuideDirection? = null

    init {
        val direction = if (request.reversed) request.direction.reversed else request.direction
        val guidePulseDuration = request.capture.exposureTime.dividedBy(2L)

        forwardGuidePulseTask = GuidePulseTask(guideOutput, GuidePulseRequest(direction, guidePulseDuration))
        backwardGuidePulseTask = GuidePulseTask(guideOutput, GuidePulseRequest(direction.reversed, guidePulseDuration))

        cameraCaptureTask.subscribe(this)
        delayTask.subscribe(this)
        forwardGuidePulseTask.subscribe(this)
        backwardGuidePulseTask.subscribe(this)
    }

    fun handleCameraEvent(event: CameraEvent) {
        cameraCaptureTask.handleCameraEvent(event)
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("DARV started. camera={}, guideOutput={}, request={}", camera, guideOutput, request)

        camera.snoop(listOf(guideOutput))

        val task = SplitTask(listOf(CameraCaptureSubTask(), GuidePulseSubTask()), executor)
        task.execute(cancellationToken)

        state = DARVState.IDLE
        sendEvent()

        LOG.info("DARV finished. camera={}, guideOutput={}, request={}", camera, guideOutput, request)
    }

    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> {
                state = DARVState.INITIAL_PAUSE
            }
            is CameraCaptureEvent -> {
                if (event.state == CameraCaptureState.EXPOSURE_FINISHED) {
                    onNext(event)
                }

                sendEvent(event)
            }
            is GuidePulseEvent -> {
                direction = event.task.request.direction
                state = if (direction == forwardGuidePulseTask.request.direction) DARVState.FORWARD else DARVState.BACKWARD
            }
            else -> return LOG.warn("unknown event: {}", event)
        }
    }

    private fun sendEvent(capture: CameraCaptureEvent? = null) {
        onNext(DARVEvent(camera, state, direction, capture))
    }

    override fun reset() {
        state = DARVState.IDLE
        direction = GuideDirection.NORTH

        cameraCaptureTask.reset()
        delayTask.reset()
        forwardGuidePulseTask.reset()
        backwardGuidePulseTask.reset()
    }

    override fun close() {
        cameraCaptureTask.close()
        delayTask.close()
        forwardGuidePulseTask.close()
        backwardGuidePulseTask.close()
        super.close()
    }

    private inner class CameraCaptureSubTask : Task {

        override fun execute(cancellationToken: CancellationToken) {
            cameraCaptureTask.execute(cancellationToken)
        }
    }

    private inner class GuidePulseSubTask : Task {

        override fun execute(cancellationToken: CancellationToken) {
            delayTask.execute(cancellationToken)
            forwardGuidePulseTask.execute(cancellationToken)
            backwardGuidePulseTask.execute(cancellationToken)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVTask>()
    }
}
