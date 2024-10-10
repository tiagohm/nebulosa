package nebulosa.api.cameras

import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CameraEventHub(
    private val messageService: MessageService,
    eventBus: EventBus,
) : DeviceEventHub<Camera, CameraEvent>(DeviceType.CAMERA), CameraEventAware {

    init {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleCameraEvent(event: CameraEvent) {
        if (event.device.type == DeviceType.CAMERA) {
            when (event) {
                is PropertyChangedEvent -> onNext(event)
                is CameraAttached -> onAttached(event.device)
                is CameraDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: Camera) {
        messageService.sendMessage(CameraMessageEvent(eventName, device))
    }
}
