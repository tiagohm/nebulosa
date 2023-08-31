package nebulosa.api.indi

import nebulosa.api.data.enums.INDISendPropertyType
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector
import org.springframework.stereotype.Service

@Service("indiService")
class INDIService(
    private val indiEventHandler: INDIEventHandler,
) {

    fun messages(): List<String> {
        return indiEventHandler
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
