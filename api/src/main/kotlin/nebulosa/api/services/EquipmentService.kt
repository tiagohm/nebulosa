package nebulosa.api.services

import nebulosa.indi.device.ConnectionEvent
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
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Service

@Service
class EquipmentService(
    private val eventBus: EventBus,
    private val webSocketService: WebSocketService,
) : DeviceEventHandler {

    private val cameras = ArrayList<Camera>(2)
    private val mounts = ArrayList<Mount>(2)
    private val focusers = ArrayList<Focuser>(2)
    private val filterWheels = ArrayList<FilterWheel>(2)
    private val guideOutputs = ArrayList<GuideOutput>(2)

    @Synchronized
    override fun onEventReceived(event: DeviceEvent<*>) {
        when (event) {
            is CameraAttached -> cameras.add(event.device)
            is CameraDetached -> cameras.remove(event.device)
            is MountAttached -> mounts.add(event.device)
            is MountDetached -> mounts.remove(event.device)
            is FocuserAttached -> focusers.add(event.device)
            is FocuserDetached -> focusers.remove(event.device)
            is FilterWheelAttached -> filterWheels.add(event.device)
            is FilterWheelDetached -> filterWheels.remove(event.device)
            is GuideOutputAttached -> guideOutputs.add(event.device)
            is GuideOutputDetached -> guideOutputs.remove(event.device)
            is ConnectionEvent -> webSocketService.sendConnectionEvent(event)
        }

        eventBus.post(event)
    }

    fun cameras(): List<Camera> {
        return cameras
    }

    fun camera(name: String): Camera? {
        return cameras.firstOrNull { it.name == name }
    }

    fun mounts(): List<Mount> {
        return mounts
    }

    fun mount(name: String): Mount? {
        return mounts.firstOrNull { it.name == name }
    }

    fun focusers(): List<Focuser> {
        return focusers
    }

    fun focuser(name: String): Focuser? {
        return focusers.firstOrNull { it.name == name }
    }

    fun filterWheels(): List<FilterWheel> {
        return filterWheels
    }

    fun filterWheel(name: String): FilterWheel? {
        return filterWheels.firstOrNull { it.name == name }
    }

    fun guideOutputs(): List<GuideOutput> {
        return guideOutputs
    }

    fun guideOutput(name: String): GuideOutput? {
        return guideOutputs.firstOrNull { it.name == name }
    }

    operator fun get(name: String): Device? {
        return camera(name) ?: mount(name) ?: focuser(name) ?: filterWheel(name)
        ?: guideOutput(name)
    }
}
