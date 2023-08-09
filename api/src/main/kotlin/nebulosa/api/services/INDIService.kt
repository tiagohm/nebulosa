package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.api.data.enums.INDISendPropertyType
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.indi.device.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.util.*

@Service("indiService")
class INDIService(
    private val webSocketService: WebSocketService,
    private val eventBus: EventBus,
) : LinkedList<String>() {

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDevicePropertyEvent(event: DevicePropertyEvent) {
        when (event) {
            is DevicePropertyChanged -> webSocketService.sendINDIPropertyChanged(event)
            is DevicePropertyDeleted -> webSocketService.sendINDIPropertyDeleted(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDeviceMessageReceived(event: DeviceMessageReceived) {
        if (event.device == null) {
            addFirst(event.message)
        }

        webSocketService.sendINDIMessageReceived(event)
    }

    fun properties(device: Device): Collection<PropertyVector<*, *>> {
        return device.properties.values
    }

    fun sendProperty(device: Device, vector: INDISendPropertyRequest) {
        when (vector.type) {
            INDISendPropertyType.NUMBER -> {
                val elements = vector.items.map { it.name to "${it.value}".toDouble() }
                device.sendNewNumber(vector.name, elements)
            }
            INDISendPropertyType.SWITCH -> {
                val elements = vector.items.map { it.name to "${it.value}".toBooleanStrict() }
                device.sendNewSwitch(vector.name, elements)
            }
            INDISendPropertyType.TEXT -> {
                val elements = vector.items.map { it.name to "${it.value}" }
                device.sendNewText(vector.name, elements)
            }
        }
    }
}
