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
) {

    private val canSendEvents = HashSet<Device>(8)
    private val messages = LinkedList<String>()

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDeviceEvent(event: DeviceEvent<*>) {
        when (event) {
            is DevicePropertyChanged -> sendINDIPropertyChanged(event)
            is DevicePropertyDeleted -> sendINDIPropertyDeleted(event)
            is DeviceMessageReceived -> event.device?.also { sendINDIMessageReceived(event) } ?: messages.addFirst(event.message)
            is DeviceDetached<*> -> unregisterDevice(event.device)
        }
    }

    fun registerDevice(device: Device) {
        canSendEvents.add(device)
    }

    fun unregisterDevice(device: Device) {
        canSendEvents.remove(device)
    }

    fun messages(): List<String> = messages

    private fun sendINDIPropertyChanged(event: DevicePropertyEvent) {
        if (event.device in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_PROPERTY_CHANGED, event))
        }
    }

    private fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        if (event.device in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_PROPERTY_DELETED, event))
        }
    }

    private fun sendINDIMessageReceived(event: DeviceMessageReceived) {
        if (event.device != null && event.device in canSendEvents) {
            messageService.sendMessage(INDIMessageEvent(DEVICE_MESSAGE_RECEIVED, event))
        }
    }

    companion object {

        const val DEVICE_PROPERTY_CHANGED = "DEVICE.PROPERTY_CHANGED"
        const val DEVICE_PROPERTY_DELETED = "DEVICE.PROPERTY_DELETED"
        const val DEVICE_MESSAGE_RECEIVED = "DEVICE.MESSAGE_RECEIVED"
    }
}
