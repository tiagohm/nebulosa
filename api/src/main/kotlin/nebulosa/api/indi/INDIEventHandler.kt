package nebulosa.api.indi

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.*

@Component
@Subscriber
class INDIEventHandler(
    private val messageService: MessageService,
) : LinkedList<String>() {

    val canSendEvents = HashSet<Device>()

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDeviceEvent(event: DeviceEvent<*>) {
        when (event) {
            is DevicePropertyChanged -> sendINDIPropertyChanged(event)
            is DevicePropertyDeleted -> sendINDIPropertyDeleted(event)
            is DeviceMessageReceived -> {
                if (event.device == null) {
                    addFirst(event.message)
                }

                sendINDIMessageReceived(event)
            }
        }
    }

    fun sendINDIPropertyChanged(event: DevicePropertyEvent) {
        if (event.device in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_PROPERTY_CHANGED, event))
        }
    }

    fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        if (event.device in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_PROPERTY_DELETED, event))
        }
    }

    fun sendINDIMessageReceived(event: DeviceMessageReceived) {
        if (event.device in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_MESSAGE_RECEIVED, event))
        }
    }

    companion object {

        const val DEVICE_PROPERTY_CHANGED = "DEVICE_PROPERTY_CHANGED"
        const val DEVICE_PROPERTY_DELETED = "DEVICE_PROPERTY_DELETED"
        const val DEVICE_MESSAGE_RECEIVED = "DEVICE_MESSAGE_RECEIVED"
    }
}
