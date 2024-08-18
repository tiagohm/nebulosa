package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.*
import nebulosa.api.guiding.GuidePulseEvent
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseTask
import nebulosa.api.message.MessageEvent
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
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.Executor

data class DARVTask(
    @JvmField val camera: Camera,
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: DARVStartRequest,
    private val executor: Executor,
) : AbstractTask<MessageEvent>(), Consumer<Any>, CameraEventAware {

    @JvmField val cameraRequest = request.capture.copy(
        exposureTime = request.capture.exposureTime + request.capture.exposureDelay,
        savePath = CAPTURE_SAVE_PATH,
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

    override fun handleCameraEvent(event: CameraEvent) {
        cameraCaptureTask.handleCameraEvent(event)
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("DARV started. camera={}, guideOutput={}, request={}", camera, guideOutput, request)

        if (guideOutput is Mount) camera.snoop(camera.snoopedDevices.filter { it !is Mount } + guideOutput)

        val task = SplitTask(listOf(cameraCaptureTask, Task.of(delayTask, forwardGuidePulseTask, backwardGuidePulseTask)), executor)
        task.execute(cancellationToken)

        state = DARVState.IDLE
        sendEvent()

        LOG.info("DARV finished. camera={}, guideOutput={}, request={}", camera, guideOutput, request)
    }

    override fun canUseAsLastEvent(event: MessageEvent) = event is DARVEvent

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

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendEvent(capture: CameraCaptureEvent? = null) {
        onNext(DARVEvent(camera, state, direction, capture))
    }

    override fun reset() {
        state = DARVState.IDLE
        direction = null

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

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVTask>()
        @JvmStatic private val CAPTURE_SAVE_PATH = Files.createTempDirectory("darv-")
    }
}
