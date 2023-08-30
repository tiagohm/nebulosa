package nebulosa.api.indi

import nebulosa.api.services.MessageService
import nebulosa.indi.device.*
import org.springframework.stereotype.Component

@Component
class INDIEventHandler(
    private val messageService: MessageService,
    private val indiService: INDIService,
) : DeviceEventHandler {

    override fun onEventReceived(event: DeviceEvent<*>) {
        when (event) {
            is DevicePropertyChanged -> sendINDIPropertyChanged(event)
            is DevicePropertyDeleted -> sendINDIPropertyDeleted(event)
            is DeviceMessageReceived -> {
                if (event.device == null) {
                    indiService.addFirst(event.message)
                }

                sendINDIMessageReceived(event)
            }
        }
    }

    fun sendINDIPropertyChanged(event: DevicePropertyEvent) {
        if (indiService.canSendEvents()) {
            messageService.sendMessage(DEVICE_PROPERTY_CHANGED, event.property)
        }
    }

    fun sendINDIPropertyDeleted(event: DevicePropertyEvent) {
        if (indiService.canSendEvents()) {
            messageService.sendMessage(DEVICE_PROPERTY_DELETED, event.property)
        }
    }

    fun sendINDIMessageReceived(event: DeviceMessageReceived) {
        if (indiService.canSendEvents()) {
            messageService.sendMessage(DEVICE_MESSAGE_RECEIVED, event)
        }
    }

    companion object {

        const val DEVICE_PROPERTY_CHANGED = "DEVICE_PROPERTY_CHANGED"
        const val DEVICE_PROPERTY_DELETED = "DEVICE_PROPERTY_DELETED"
        const val DEVICE_MESSAGE_RECEIVED = "DEVICE_MESSAGE_RECEIVED"
    }
}
