package nebulosa.api.autofocus

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class AutoFocusExecutor(
    private val messageService: MessageService,
) : Consumer<MessageEvent> {

    private val jobs = ConcurrentHashMap.newKeySet<AutoFocusJob>(2)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FocuserEvent) {
        jobs.find { it.task.focuser === event.device }?.handleFocuserEvent(event)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    @Synchronized
    fun execute(camera: Camera, focuser: Focuser, request: AutoFocusRequest) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(focuser.connected) { "${focuser.name} Camera is not connected" }
        check(jobs.none { it.task.camera === camera }) { "${camera.name} Auto Focus is already in progress" }
        check(jobs.none { it.task.focuser === focuser }) { "${camera.name} Auto Focus is already in progress" }

        val starDetector = request.starDetector.get()
        val task = AutoFocusTask(camera, focuser, request, starDetector)
        task.subscribe(this)

        with(AutoFocusJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this) }
            start()
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.task.camera === camera }?.stop()
    }

    fun status(camera: Camera): AutoFocusEvent? {
        return jobs.find { it.task.camera === camera }?.task?.get() as? AutoFocusEvent
    }
}
