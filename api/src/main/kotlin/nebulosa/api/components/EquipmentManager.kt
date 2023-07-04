package nebulosa.api.components

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import org.springframework.stereotype.Component

@Component
class EquipmentManager(
    private val cameraManager: CameraManager,
) : DeviceEventHandler {

    @Synchronized
    override fun onEventReceived(event: DeviceEvent<*>) {
        cameraManager.onEventReceived(event)
    }

    operator fun get(name: String): Device? {
        return cameraManager.firstOrNull { it.name == name }
    }
}
