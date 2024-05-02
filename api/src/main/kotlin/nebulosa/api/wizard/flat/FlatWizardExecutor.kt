package nebulosa.api.wizard.flat

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Subscriber
class FlatWizardExecutor(
    private val messageService: MessageService,
) : Consumer<FlatWizardEvent> {

    private val jobs = ConcurrentHashMap.newKeySet<FlatWizardJob>(1)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        jobs.find { it.task.camera === event.device }?.handleCameraEvent(event)
    }

    override fun accept(event: FlatWizardEvent) {
        messageService.sendMessage(event)
    }

    fun execute(camera: Camera, request: FlatWizardRequest) {
        check(camera.connected) { "camera is not connected" }
        check(jobs.any { it.task.camera === camera }) { "${camera.name} Flat Wizard is already in progress" }

        val task = FlatWizardTask(camera, request)
        task.subscribe(this)

        with(FlatWizardJob(task)) {
            jobs.add(this)
            whenComplete { _, _ -> jobs.remove(this) }
            start()
        }
    }

    fun stop(camera: Camera) {
        jobs.find { it.task.camera === camera }?.stop()
    }
}
