package nebulosa.api.wheels

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component

@Component
@Subscriber
class WheelEventHub(
    private val messageService: MessageService,
) : DeviceEventHub<FilterWheel, FilterWheelEvent>(), WheelEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is FilterWheelAttached -> sendMessage(WHEEL_ATTACHED, event.device)
            is FilterWheelDetached -> sendMessage(WHEEL_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: FilterWheel) {
        messageService.sendMessage(WheelMessageEvent(eventName, device))
    }

    override fun sendUpdate(device: FilterWheel) {
        sendMessage(WHEEL_UPDATED, device)
    }

    companion object {

        const val WHEEL_UPDATED = "WHEEL.UPDATED"
        const val WHEEL_ATTACHED = "WHEEL.ATTACHED"
        const val WHEEL_DETACHED = "WHEEL.DETACHED"
    }
}
