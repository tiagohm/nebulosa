package nebulosa.api.indi

import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.protocol.PropertyType
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

    fun sendProperty(device: Device, vector: INDISendProperty) {
        when (vector.type) {
            PropertyType.NUMBER -> {
                val elements = vector.items.map { it.name to "${it.value}".toDouble() }
                device.sendNewNumber(vector.name, elements)
            }
            PropertyType.SWITCH -> {
                val elements = vector.items.map { it.name to "${it.value}".toBooleanStrict() }
                device.sendNewSwitch(vector.name, elements)
            }
            PropertyType.TEXT -> {
                val elements = vector.items.map { it.name to "${it.value}" }
                device.sendNewText(vector.name, elements)
            }
            else -> Unit
        }
    }
}
