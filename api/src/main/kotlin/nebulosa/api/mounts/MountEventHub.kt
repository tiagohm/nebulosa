package nebulosa.api.mounts

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceType
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
) : DeviceEventHub<Mount, MountEvent>(DeviceType.MOUNT), MountEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleMountEvent(event: MountEvent) {
        if (event.device.type == DeviceType.MOUNT) {
            when (event) {
            is PropertyChangedEvent -> onNext(event)
            is MountAttached -> onAttached(event.device)
            is MountDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: Mount) {
        messageService.sendMessage(MountMessageEvent(eventName, device))
    }
}
