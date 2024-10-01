package nebulosa.api.wizard.flat

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.cameras.CameraEventAware
import nebulosa.api.message.MessageEvent
import nebulosa.api.message.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class FlatWizardExecutor(
    private val messageService: MessageService,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
) : Consumer<MessageEvent>, CameraEventAware {

    private val jobs = ConcurrentHashMap.newKeySet<FlatWizardJob>(1)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        jobs.find { it.camera === event.device }?.handleCameraEvent(event)
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    fun execute(camera: Camera, request: FlatWizardRequest) {
        check(camera.connected) { "camera is not connected" }
        check(jobs.none { it.camera === camera }) { "${camera.name} Flat Wizard is already in progress" }

        with(FlatWizardJob(this, camera, request)) {
            val completable = runAsync(threadPoolTaskExecutor)
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
