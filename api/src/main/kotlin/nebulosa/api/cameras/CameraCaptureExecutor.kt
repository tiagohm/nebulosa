package nebulosa.api.cameras

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class CameraCaptureExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
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
        check(jobs.none { it.task.camera === camera }) { "${camera.name} Camera Capture is already in progress" }

        val task = CameraCaptureTask(camera, request, guider, executor = threadPoolTaskExecutor)
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

    fun status(camera: Camera): CameraCaptureEvent? {
        return jobs.find { it.task.camera === camera }?.task?.get()
    }
}
