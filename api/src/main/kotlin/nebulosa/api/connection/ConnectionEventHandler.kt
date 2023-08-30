package nebulosa.api.connection

import jakarta.annotation.PostConstruct
import nebulosa.api.focusers.FocuserEventHandler
import nebulosa.api.guiding.GuideOutputEventHandler
import nebulosa.api.mounts.MountEventHandler
import nebulosa.api.wheels.WheelEventHandler
import nebulosa.indi.device.ConnectionEvent
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.springframework.stereotype.Component

@Component
class ConnectionEventHandler(
    private val eventBus: EventBus,
    private val mountEventHandler: MountEventHandler,
    private val focuserEventHandler: FocuserEventHandler,
    private val wheelEventHandler: WheelEventHandler,
    private val guideOutputEventHandler: GuideOutputEventHandler,
) : DeviceEventHandler {

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe
    @Suppress("CascadeIf")
    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is ConnectionEvent) {
            val device = event.device ?: return

            if (device is Mount) mountEventHandler.sendUpdate(device)
            else if (device is Focuser) focuserEventHandler.sendUpdate(device)
            else if (device is FilterWheel) wheelEventHandler.sendUpdate(device)

            if (device is GuideOutput) guideOutputEventHandler.sendUpdate(device)
        }
    }
}
