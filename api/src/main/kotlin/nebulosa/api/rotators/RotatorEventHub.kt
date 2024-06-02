package nebulosa.api.rotators

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.rotator.RotatorAttached
import nebulosa.indi.device.rotator.RotatorDetached
import nebulosa.indi.device.rotator.RotatorEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class RotatorEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<Rotator, RotatorEvent>(), RotatorEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleRotatorEvent(event: RotatorEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is RotatorAttached -> sendMessage(ROTATOR_ATTACHED, event.device)
            is RotatorDetached -> sendMessage(ROTATOR_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: Rotator) {
        messageService.sendMessage(RotatorMessageEvent(eventName, device))
    }

    override fun sendUpdate(device: Rotator) {
        sendMessage(ROTATOR_UPDATED, device)
    }

    companion object {

        const val ROTATOR_UPDATED = "ROTATOR.UPDATED"
        const val ROTATOR_ATTACHED = "ROTATOR.ATTACHED"
        const val ROTATOR_DETACHED = "ROTATOR.DETACHED"
    }
}
