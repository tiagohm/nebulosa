package nebulosa.api.services

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Service
import java.util.*

@Service
class EquipmentService(private val eventBus: EventBus) : DeviceEventHandler {

    private val cameras = ArrayList<Camera>(2)
    private val focusers = ArrayList<Focuser>(2)

    @Synchronized
    override fun onEventReceived(event: DeviceEvent<*>) {
        when (event) {
            is CameraAttached -> cameras.add(event.device)
            is CameraDetached -> cameras.remove(event.device)
            is FocuserAttached -> focusers.add(event.device)
            is FocuserDetached -> focusers.remove(event.device)
        }

        eventBus.post(event)
    }

    fun cameras(): List<Camera> {
        return Collections.unmodifiableList(cameras)
    }

    fun camera(name: String): Camera? {
        return cameras.firstOrNull { it.name == name }
    }

    fun focusers(): List<Focuser> {
        return Collections.unmodifiableList(focusers)
    }

    fun focuser(name: String): Focuser? {
        return focusers.firstOrNull { it.name == name }
    }

    operator fun get(name: String): Device? {
        return camera(name) ?: focuser(name)
    }
}
