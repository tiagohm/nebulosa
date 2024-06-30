package nebulosa.api.wheels

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.devices.DeviceEventHub
import nebulosa.api.message.MessageService
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
) : DeviceEventHub<FilterWheel, FilterWheelEvent>("WHEEL"), WheelEventAware {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        when (event) {
            is PropertyChangedEvent -> onNext(event)
            is FilterWheelAttached -> onAttached(event.device)
            is FilterWheelDetached -> onDetached(event.device)
        }
    }

    override fun sendMessage(eventName: String, device: FilterWheel) {
        messageService.sendMessage(WheelMessageEvent(eventName, device))
    }
}
