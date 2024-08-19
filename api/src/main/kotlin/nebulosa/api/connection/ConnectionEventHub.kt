package nebulosa.api.connection

import nebulosa.api.cameras.CameraEventHub
import nebulosa.api.focusers.FocuserEventHub
import nebulosa.api.guiding.GuideOutputEventHub
import nebulosa.api.mounts.MountEventHub
import nebulosa.api.rotators.RotatorEventHub
import nebulosa.api.wheels.WheelEventHub
import nebulosa.indi.device.DeviceConnectionEvent
import nebulosa.indi.device.DeviceDetached
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import org.springframework.stereotype.Component

@Component
class ConnectionEventHub(
    private val cameraEventHub: CameraEventHub,
    private val mountEventHub: MountEventHub,
    private val focuserEventHub: FocuserEventHub,
    private val wheelEventHub: WheelEventHub,
    private val guideOutputEventHub: GuideOutputEventHub,
    private val rotatorEventHub: RotatorEventHub,
) : DeviceEventHandler.EventReceived {

    override fun onEventReceived(event: DeviceEvent<*>) {
        val device = event.device ?: return

        if (event is DeviceConnectionEvent) {
            if (device is Camera) cameraEventHub.onConnectionChanged(device)
            if (device is Mount) mountEventHub.onConnectionChanged(device)
            if (device is Focuser) focuserEventHub.onConnectionChanged(device)
            if (device is FilterWheel) wheelEventHub.onConnectionChanged(device)
            if (device is Rotator) rotatorEventHub.onConnectionChanged(device)
            if (device is GuideOutput) guideOutputEventHub.onConnectionChanged(device)
        } else if (event is DeviceDetached<*>) {
            if (device is Camera) cameraEventHub.onDeviceDetached(device)
            if (device is Mount) mountEventHub.onDeviceDetached(device)
            if (device is Focuser) focuserEventHub.onDeviceDetached(device)
            if (device is FilterWheel) wheelEventHub.onDeviceDetached(device)
            if (device is Rotator) rotatorEventHub.onDeviceDetached(device)
            if (device is GuideOutput) guideOutputEventHub.onDeviceDetached(device)
        }
    }
}
