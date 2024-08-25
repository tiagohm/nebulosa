package nebulosa.api.dustcap

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.dustcap.DustCap
import nebulosa.indi.device.dustcap.DustCapAttached
import nebulosa.indi.device.dustcap.DustCapDetached
import nebulosa.indi.device.dustcap.DustCapEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class DustCapEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<DustCap, DustCapEvent>(DeviceType.DUST_CAP), DustCapEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleDustCapEvent(event: DustCapEvent) {
        if (event.device.type == DeviceType.DUST_CAP) {
            when (event) {
                is PropertyChangedEvent -> onNext(event)
                is DustCapAttached -> onAttached(event.device)
                is DustCapDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: DustCap) {
        messageService.sendMessage(DustCapMessageEvent(eventName, device))
    }
}
