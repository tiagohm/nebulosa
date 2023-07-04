package nebulosa.api.components

import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import org.springframework.stereotype.Component

@Component
class CameraManager : DeviceManager<Camera>() {

    override fun canHandleEvent(event: DeviceEvent<*>) = event is CameraEvent

    override fun onDeviceEventReceived(event: DeviceEvent<Camera>) = Unit
}
