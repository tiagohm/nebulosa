package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
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

    private val jobs = ConcurrentHashMap.newKeySet<CameraCaptureJob>(2)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    override fun accept(event: CameraCaptureEvent) {
        messageService.sendMessage(event)
    }

    @Synchronized
    fun execute(camera: Camera, request: CameraStartCaptureRequest) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(jobs.any { it.task.camera === camera }) { "${camera.name} Camera Capture in progress" }

        val task = CameraCaptureTask(camera, request, guider)
        task.subscribe(this)

        with(CameraCaptureJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this) }
            start()
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.task.camera === camera }?.stop()
    }
}
