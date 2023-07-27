package nebulosa.api.services

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
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
    private val filterWheels = ArrayList<FilterWheel>(2)

    @Synchronized
    override fun onEventReceived(event: DeviceEvent<*>) {
        when (event) {
            is CameraAttached -> cameras.add(event.device)
            is CameraDetached -> cameras.remove(event.device)
            is FocuserAttached -> focusers.add(event.device)
            is FocuserDetached -> focusers.remove(event.device)
            is FilterWheelAttached -> filterWheels.add(event.device)
            is FilterWheelDetached -> filterWheels.remove(event.device)
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

    fun filterWheels(): List<FilterWheel> {
        return Collections.unmodifiableList(filterWheels)
    }

    fun filterWheel(name: String): FilterWheel? {
        return filterWheels.firstOrNull { it.name == name }
    }

    operator fun get(name: String): Device? {
        return camera(name)
            ?: focuser(name)
            ?: filterWheel(name)
    }
}
