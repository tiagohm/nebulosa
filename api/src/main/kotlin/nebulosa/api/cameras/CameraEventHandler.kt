package nebulosa.api.cameras

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.services.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class CameraEventHandler(
    private val messageService: MessageService,
) {

    private val throttler = PublishSubject.create<CameraEvent>()

    @PostConstruct
    private fun initialize() {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        when (event) {
            is PropertyChangedEvent -> {
                throttler.onNext(event)
            }
            is CameraAttached -> {
                messageService.sendMessage(CAMERA_ATTACHED, event.device)
            }
            is CameraDetached -> {
                messageService.sendMessage(CAMERA_DETACHED, event.device)
            }
        }
    }

    fun sendUpdate(device: Camera) {
        messageService.sendMessage(CAMERA_UPDATED, device)
    }

    companion object {

        const val CAMERA_UPDATED = "CAMERA_UPDATED"
        const val CAMERA_ATTACHED = "CAMERA_ATTACHED"
        const val CAMERA_DETACHED = "CAMERA_DETACHED"
    }
}
