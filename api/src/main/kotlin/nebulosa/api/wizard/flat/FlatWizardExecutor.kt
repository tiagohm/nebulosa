package nebulosa.api.wizard.flat

import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.message.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.function.Consumer

class FlatWizardExecutor(
    private val messageService: MessageService,
    private val executorService: ExecutorService,
    eventBus: EventBus,
) : Consumer<MessageEvent>, CameraEventAware {

    private val jobs = ConcurrentHashMap.newKeySet<FlatWizardJob>(1)

    init {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.camera === event.device }?.handleCameraEvent(event)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    fun execute(camera: Camera, request: FlatWizardRequest, wheel: FilterWheel? = null) {
        check(camera.connected) { "camera is not connected" }
        check(jobs.none { it.camera === camera }) { "${camera.name} Flat Wizard is already in progress" }

        with(FlatWizardJob(this, camera, request, wheel)) {
            val completable = runAsync(executorService)
            jobs.add(this)
            completable.whenComplete { _, _ -> jobs.remove(this) }
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.camera === camera }?.stop()
    }

    fun status(camera: Camera): FlatWizardEvent? {
        return jobs.find { it.camera === camera }?.status
    }
}
