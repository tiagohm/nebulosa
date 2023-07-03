package nebulosa.api.components

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.CameraEvent
import org.springframework.stereotype.Component

@Component
class EquipmentManager(
    private val cameraManager: CameraManager,
) : DeviceEventHandler {

    @Synchronized
    override fun onEventReceived(event: DeviceEvent<*>) {
        when (event) {
            is CameraEvent -> cameraManager.onCameraEventReceived(event)
        }
    }

    operator fun get(name: String): Device? {
        return cameraManager.firstOrNull { it.name == name }
    }
}
