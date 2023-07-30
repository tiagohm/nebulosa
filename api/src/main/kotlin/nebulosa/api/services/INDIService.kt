package nebulosa.api.services

import nebulosa.api.data.enums.INDISendPropertyType
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.data.responses.INDIPropertyResponse
import nebulosa.indi.device.Device
import org.springframework.stereotype.Service
import java.util.*

@Service("indiService")
class INDIService : LinkedList<String>() {

    fun properties(device: Device): List<INDIPropertyResponse> {
        return device.properties.values.map(::INDIPropertyResponse)
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
