package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraExposureEvent
import nebulosa.api.cameras.CameraExposureTask
import nebulosa.api.guiding.GuidePulseEvent
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.guiding.GuidePulseTask
import nebulosa.api.tasks.Task
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.guide.GuideOutput
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.CompletableFuture

data class DARVTask(
    @JvmField val camera: Camera,
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: DARVStartRequest,
) : Task<DARVEvent>(), Consumer<Any> {

    @JvmField val cameraRequest = request.capture.copy(
        exposureTime = request.capture.exposureTime + request.capture.exposureDelay,
        savePath = Files.createTempDirectory("darv"),
        exposureAmount = 1, exposureDelay = Duration.ZERO,
        frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
    )

    private val cameraExposureTask = CameraExposureTask(camera, cameraRequest)
    private val delayTask = DelayTask(request.capture.exposureDelay)
    private val forwardGuidePulseTask: GuidePulseTask
    private val backwardGuidePulseTask: GuidePulseTask

    init {
        val direction = if (request.reversed) request.direction.reversed else request.direction
        val guidePulseDuration = request.capture.exposureTime.dividedBy(2L)

        forwardGuidePulseTask = GuidePulseTask(guideOutput, GuidePulseRequest(direction, guidePulseDuration))
        backwardGuidePulseTask = GuidePulseTask(guideOutput, GuidePulseRequest(direction.reversed, guidePulseDuration))

        cameraExposureTask.subscribe(this)
        delayTask.subscribe(this)
        forwardGuidePulseTask.subscribe(this)
        backwardGuidePulseTask.subscribe(this)
    }

    fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun execute(cancellationToken: CancellationToken) {
        val a = CompletableFuture.runAsync {
            // CAPTURE.
            cameraExposureTask.execute(cancellationToken)
        }

        val b = CompletableFuture.runAsync {
            // INITIAL PAUSE.
            delayTask.execute(cancellationToken)

            // FORWARD GUIDE PULSE.
            forwardGuidePulseTask.execute(cancellationToken)

            // BACKWARD GUIDE PULSE.
            backwardGuidePulseTask.execute(cancellationToken)
        }

        CompletableFuture.allOf(a, b).join()
    }

    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> Unit
            is CameraExposureEvent -> Unit
            is GuidePulseEvent -> Unit
        }
    }

    override fun close() {
        cameraExposureTask.close()
        delayTask.close()
        forwardGuidePulseTask.close()
        backwardGuidePulseTask.close()
        super.close()
    }
}
