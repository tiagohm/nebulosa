package nebulosa.api.mounts

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.mount.MountEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class MountEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<Mount, MountEvent>(), MountEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleMountEvent(event: MountEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is MountAttached -> sendMessage(MOUNT_ATTACHED, event.device)
            is MountDetached -> sendMessage(MOUNT_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: Mount) {
        messageService.sendMessage(MountMessageEvent(eventName, device))
    }

    override fun sendUpdate(device: Mount) {
        sendMessage(MOUNT_UPDATED, device)
    }

    companion object {

        const val MOUNT_UPDATED = "MOUNT.UPDATED"
        const val MOUNT_ATTACHED = "MOUNT.ATTACHED"
        const val MOUNT_DETACHED = "MOUNT.DETACHED"
    }
}
