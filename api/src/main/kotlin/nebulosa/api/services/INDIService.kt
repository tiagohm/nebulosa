package nebulosa.api.services

import nebulosa.api.data.enums.INDISendPropertyType
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.data.responses.INDIPropertyResponse
import org.springframework.stereotype.Service
import java.util.*

@Service("indiService")
class INDIService(
    private val equipmentService: EquipmentService,
) {

    private val messageReceived = LinkedList<String>()

    fun properties(name: String): List<INDIPropertyResponse> {
        val device = equipmentService[name] ?: return emptyList()
        return device.properties.values.map(::INDIPropertyResponse)
    }

    fun sendProperty(name: String, vector: INDISendPropertyRequest) {
        val device = equipmentService[name] ?: return

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

    fun indiLog(name: String?): List<String> {
        if (name.isNullOrBlank()) return messageReceived
        val device = equipmentService[name] ?: return emptyList()
        return device.messages
    }

    internal fun onMessageReceived(message: String) {
        messageReceived.addFirst(message)
    }
}
