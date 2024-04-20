package nebulosa.api.alignment.polar.darv

import nebulosa.api.cameras.AutoSubFolderMode
import nebulosa.api.cameras.CameraCaptureTask
import nebulosa.api.guiding.GuidePulseRequest
import nebulosa.api.messages.MessageService
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.api.guiding.GuidePulseTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Component
class DARVTask(
    private val cameraCaptureTask: CameraCaptureTask,
    private val guidePulseTask: GuidePulseTask,
    private val delayTask: DelayTask,
    private val messageService: MessageService,
) : ListenableTask<Any>() {

    @Volatile private var task: DARVTaskContext? = null

    @Synchronized
    fun align(camera: Camera, guideOutput: GuideOutput, request: DARVStartRequest) {
        val cameraRequest = request.capture.copy(
            exposureTime = request.capture.exposureTime + request.capture.exposureDelay,
            savePath = Files.createTempDirectory("darv"),
            exposureAmount = 1, exposureDelay = Duration.ZERO,
            frameType = FrameType.LIGHT, autoSave = false, autoSubFolderMode = AutoSubFolderMode.OFF
        )

        val cancellationToken = CancellationToken()
        val context = DARVTaskContext(camera, guideOutput, request, cameraRequest, cancellationToken)

        captureAndPulse(context)

        task = context
    }

    @Synchronized
    fun stop() {
        task?.cameraCaptureTaskSubscription?.dispose()
        task?.cameraCaptureTaskSubscription = null

        task?.cancellationToken?.cancel()
        task = null
    }

    private fun captureAndPulse(context: DARVTaskContext) {
        val a = CompletableFuture.runAsync {
            // CAPTURE.
            context.cameraCaptureTaskSubscription = cameraCaptureTask.subscribe(messageService::sendMessage)
            context.cameraCaptureTaskId = cameraCaptureTask.startCapture(context.cameraRequest, context.camera, null, context.cancellationToken)
        }

        val b = CompletableFuture.runAsync {
            // INITIAL PAUSE.
            delayTask.delay(context.request.capture.exposureDelay, context.cancellationToken)

            val direction = if (context.request.reversed) context.request.direction.reversed else context.request.direction
            val guidePulseDuration = context.request.capture.exposureTime.dividedBy(2L)

            // FORWARD GUIDE PULSE.
            val forwardGuidePulseRequest = GuidePulseRequest(direction, guidePulseDuration)
            guidePulseTask.pulse(context.guideOutput, forwardGuidePulseRequest, context.cancellationToken)

            // BACKWARD GUIDE PULSE.
            val backwardGuidePulseRequest = GuidePulseRequest(direction.reversed, guidePulseDuration)
            guidePulseTask.pulse(context.guideOutput, backwardGuidePulseRequest, context.cancellationToken)
        }

        CompletableFuture.allOf(a, b)
            .whenComplete { _, _ -> stop() }
    }
}
