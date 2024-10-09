package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.focusers.FocuserEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.message.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.function.Consumer

class AutoFocusExecutor(
    private val messageService: MessageService,
    private val executorService: ExecutorService,
    eventBus: EventBus,
) : Consumer<MessageEvent>, CameraEventAware, FocuserEventAware {

    private val jobs = ConcurrentHashMap.newKeySet<AutoFocusJob>(2)

    init {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.camera === event.device }?.handleCameraEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFocuserEvent(event: FocuserEvent) {
        jobs.find { it.focuser === event.device }?.handleFocuserEvent(event)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    @Synchronized
    fun execute(camera: Camera, focuser: Focuser, request: AutoFocusRequest) {
        check(camera.connected) { "${camera.name} Camera is not connected" }
        check(focuser.connected) { "${focuser.name} Camera is not connected" }
        check(jobs.none { it.camera === camera }) { "${camera.name} Auto Focus is already in progress" }
        check(jobs.none { it.focuser === focuser }) { "${camera.name} Auto Focus is already in progress" }

        val starDetector = request.starDetector.get()

        with(AutoFocusJob(this, camera, focuser, request, starDetector)) {
            val completable = runAsync(executorService)
            jobs.add(this)
            completable.whenComplete { _, _ -> jobs.remove(this) }
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.camera === camera }?.stop()
    }

    fun status(camera: Camera): AutoFocusEvent? {
        return jobs.find { it.camera === camera }?.status
    }
}
