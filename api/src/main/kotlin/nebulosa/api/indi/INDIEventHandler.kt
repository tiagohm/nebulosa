package nebulosa.api.indi

import jakarta.annotation.PostConstruct
import nebulosa.api.services.MessageService
import nebulosa.indi.device.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.*

@Component
class INDIEventHandler(
    private val eventBus: EventBus,
    private val messageService: MessageService,
) : LinkedList<String>() {

    var canSendEvents = false
        internal set

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

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
