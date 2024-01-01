package nebulosa.api.cameras

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.io.Closeable
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class CameraEventHandler(
    private val messageService: MessageService,
) : Closeable {

    private val throttler = PublishSubject.create<CameraEvent>()

    init {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        when (event) {
            is PropertyChangedEvent -> throttler.onNext(event)
            is CameraAttached -> sendMessage(CAMERA_ATTACHED, event.device)
            is CameraDetached -> sendMessage(CAMERA_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: Camera) {
        messageService.sendMessage(CameraMessageEvent(eventName, device))
    }

    fun sendUpdate(device: Camera) {
        sendMessage(CAMERA_UPDATED, device)
    }

    override fun close() {
        throttler.onComplete()
    }

    companion object {

        const val CAMERA_UPDATED = "CAMERA.UPDATED"
        const val CAMERA_ATTACHED = "CAMERA.ATTACHED"
        const val CAMERA_DETACHED = "CAMERA.DETACHED"
    }
}
