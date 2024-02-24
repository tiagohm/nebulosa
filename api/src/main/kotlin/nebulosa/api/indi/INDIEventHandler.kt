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

    private val canSendEvents = HashSet<String>()

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDeviceEvent(event: DeviceEvent<*>) {
        when (event) {
            is DevicePropertyChanged -> sendINDIPropertyChanged(event)
            is DevicePropertyDeleted -> sendINDIPropertyDeleted(event)
            is DeviceMessageReceived -> if (event.device == null) addFirst(event.message)
            else sendINDIMessageReceived(event)
            is DeviceDetached<*> -> unregisterDevice(event.device)
        }
    }

    fun registerDevice(device: Device) {
        canSendEvents.add(device.id)
    }

    fun unregisterDevice(device: Device) {
        canSendEvents.remove(device.id)
    }

    fun sendINDIPropertyChanged(event: DevicePropertyEvent) {
        if (event.device.id in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_PROPERTY_CHANGED, event))
        }
    }

    fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        if (event.device.id in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_PROPERTY_DELETED, event))
        }
    }

    fun sendINDIMessageReceived(event: DeviceMessageReceived) {
        if (event.device != null && event.device!!.id in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_MESSAGE_RECEIVED, event))
        }
    }

    companion object {

        const val DEVICE_PROPERTY_CHANGED = "DEVICE.PROPERTY_CHANGED"
        const val DEVICE_PROPERTY_DELETED = "DEVICE.PROPERTY_DELETED"
        const val DEVICE_MESSAGE_RECEIVED = "DEVICE.MESSAGE_RECEIVED"
    }
}
