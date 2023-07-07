package nebulosa.api.services

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import org.springframework.stereotype.Service
import java.util.*

@Service
class EquipmentService : DeviceEventHandler {

    private val handlers = Collections.synchronizedSet(HashSet<DeviceEventHandler>())
    private val cameras = ArrayList<Camera>(2)

    fun registerDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.add(handler)
    }

    fun unregisterDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.remove(handler)
    }

    @Synchronized
    override fun onEventReceived(event: DeviceEvent<*>) {
        when (event) {
            is CameraAttached -> cameras.add(event.device)
            is CameraDetached -> cameras.remove(event.device)
            else -> handlers.forEach { it.onEventReceived(event) }
        }
    }

    fun cameras(): List<Camera> {
        return Collections.unmodifiableList(cameras)
    }

    fun camera(name: String): Camera? {
        return cameras.firstOrNull { it.name == name }
    }

    operator fun get(name: String): Device? {
        return camera(name)
    }
}
