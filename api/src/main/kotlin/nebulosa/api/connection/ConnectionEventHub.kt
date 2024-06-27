package nebulosa.api.connection

import nebulosa.api.cameras.CameraEventHub
import nebulosa.api.focusers.FocuserEventHub
import nebulosa.api.guiding.GuideOutputEventHub
import nebulosa.api.mounts.MountEventHub
import nebulosa.api.rotators.RotatorEventHub
import nebulosa.api.wheels.WheelEventHub
import nebulosa.indi.device.DeviceConnectionEvent
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guide.GuideOutput
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
            when (device) {
                is Camera -> cameraEventHub.onConnectionChanged(device)
                is Mount -> mountEventHub.onConnectionChanged(device)
                is Focuser -> focuserEventHub.onConnectionChanged(device)
                is FilterWheel -> wheelEventHub.onConnectionChanged(device)
                is Rotator -> rotatorEventHub.onConnectionChanged(device)
            }

            if (device is GuideOutput) guideOutputEventHub.onConnectionChanged(device)
        }
    }
}
