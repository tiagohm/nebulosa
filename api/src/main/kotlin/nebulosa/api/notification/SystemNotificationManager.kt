package nebulosa.api.notification

import nebulosa.api.message.MessageService
import nebulosa.api.mounts.MountEventAware
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountSlewingChanged
import java.util.concurrent.ConcurrentHashMap

class SystemNotificationManager(private val messageService: MessageService) : MountEventAware {

    private data class MountSlewEvent(override val body: String) : NotificationEvent.System, NotificationEvent.Info

    private val mountSlew = ConcurrentHashMap.newKeySet<Mount>(2)

    override fun handleMountEvent(event: MountEvent) {
        val device = event.device

        if (event is MountSlewingChanged) {
            if (device.slewing) {
                if (mountSlew.add(device)) {
                    messageService.sendMessage(MountSlewEvent("${device.name} slew started"))
                }
            } else {
                if (mountSlew.remove(device)) {
                    messageService.sendMessage(MountSlewEvent("${device.name} slew finished"))
                }
            }
        }
    }
}
