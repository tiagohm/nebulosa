package nebulosa.api.connection

import nebulosa.api.cameras.CameraEventHandler
import nebulosa.api.focusers.FocuserEventHandler
import nebulosa.api.guiding.GuideOutputEventHandler
import nebulosa.api.mounts.MountEventHandler
import nebulosa.api.wheels.WheelEventHandler
import nebulosa.indi.device.ConnectionEvent
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.stereotype.Component

@Component
class ConnectionEventHandler(
    private val cameraEventHandler: CameraEventHandler,
    private val mountEventHandler: MountEventHandler,
    private val focuserEventHandler: FocuserEventHandler,
    private val wheelEventHandler: WheelEventHandler,
    private val guideOutputEventHandler: GuideOutputEventHandler,
) : DeviceEventHandler {

    @Suppress("CascadeIf")
    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is ConnectionEvent) {
            val device = event.device ?: return

            if (device is Camera) cameraEventHandler.sendUpdate(device)
            else if (device is Mount) mountEventHandler.sendUpdate(device)
            else if (device is Focuser) focuserEventHandler.sendUpdate(device)
            else if (device is FilterWheel) wheelEventHandler.sendUpdate(device)

            if (device is GuideOutput) guideOutputEventHandler.sendUpdate(device)
        }
    }
}