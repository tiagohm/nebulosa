package nebulosa.api.cameras

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class CameraEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<Camera, CameraEvent>(), CameraEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is CameraAttached -> sendMessage(CAMERA_ATTACHED, event.device)
            is CameraDetached -> sendMessage(CAMERA_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, camera: Camera) {
        messageService.sendMessage(CameraMessageEvent(eventName, camera))
    }

    override fun sendUpdate(device: Camera) {
        sendMessage(CAMERA_UPDATED, device)
    }

    companion object {

        const val CAMERA_UPDATED = "CAMERA.UPDATED"
        const val CAMERA_ATTACHED = "CAMERA.ATTACHED"
        const val CAMERA_DETACHED = "CAMERA.DETACHED"
    }
}
