package nebulosa.api.rotators

import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.rotator.RotatorAttached
import nebulosa.indi.device.rotator.RotatorDetached
import nebulosa.indi.device.rotator.RotatorEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RotatorEventHub(
    private val messageService: MessageService,
    eventBus: EventBus,
) : DeviceEventHub<Rotator, RotatorEvent>(DeviceType.ROTATOR), RotatorEventAware {

    init {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleRotatorEvent(event: RotatorEvent) {
        if (event.device.type == DeviceType.ROTATOR) {
            when (event) {
                is PropertyChangedEvent -> onNext(event)
                is RotatorAttached -> onAttached(event.device)
                is RotatorDetached -> onDetached(event.device)
            }
        }
    }

    override fun sendMessage(eventName: String, device: Rotator) {
        messageService.sendMessage(RotatorMessageEvent(eventName, device))
    }
}
