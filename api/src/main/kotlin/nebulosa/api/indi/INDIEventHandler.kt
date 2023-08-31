package nebulosa.api.indi

import nebulosa.api.services.MessageService
import nebulosa.indi.device.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class INDIEventHandler(
    private val messageService: MessageService,
) : LinkedList<String>(), DeviceEventHandler {

    var canSendEvents = false
        internal set

    override fun onEventReceived(event: DeviceEvent<*>) {
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
        if (canSendEvents) {
            messageService.sendMessage(DEVICE_PROPERTY_CHANGED, event.property)
        }
    }

    fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        if (canSendEvents) {
            messageService.sendMessage(DEVICE_PROPERTY_DELETED, event.property)
        }
    }

    fun sendINDIMessageReceived(event: DeviceMessageReceived) {
        if (canSendEvents) {
            messageService.sendMessage(DEVICE_MESSAGE_RECEIVED, event)
        }
    }

    companion object {

        const val DEVICE_PROPERTY_CHANGED = "DEVICE_PROPERTY_CHANGED"
        const val DEVICE_PROPERTY_DELETED = "DEVICE_PROPERTY_DELETED"
        const val DEVICE_MESSAGE_RECEIVED = "DEVICE_MESSAGE_RECEIVED"
    }
}
