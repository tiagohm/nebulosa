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
) : DeviceEventHub<Camera, CameraEvent>("CAMERA"), CameraEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is CameraAttached -> onAttached(event.device)
            is CameraDetached -> onDetached(event.device)
        }
    }

    override fun sendMessage(eventName: String, device: Camera) {
        messageService.sendMessage(CameraMessageEvent(eventName, device))
    }
}
