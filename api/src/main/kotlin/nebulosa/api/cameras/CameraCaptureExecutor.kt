package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class CameraCaptureExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
) : Consumer<CameraCaptureEvent> {

    private val jobs = ConcurrentHashMap<Camera, CameraCaptureJob>(2)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        jobs[event.device]?.task?.handleCameraEvent(event)
    }

    override fun accept(event: CameraCaptureEvent) {
        messageService.sendMessage(event)
    }

    @Synchronized
    fun execute(camera: Camera, request: CameraStartCaptureRequest) {
        check(camera.connected) { "${camera.name} camera is not connected" }
        check(!jobs.contains(camera)) { "${camera.name} camera capture in progress" }

        val cancellationToken = CancellationToken()
        val task = CameraCaptureTask(camera, request, guider)
        task.subscribe(this)

        with(CameraCaptureJob(task, cancellationToken)) {
            jobs[camera] = this
        }
    }

    fun stop(camera: Camera) {
        jobs.remove(camera)?.abort()
    }
}
