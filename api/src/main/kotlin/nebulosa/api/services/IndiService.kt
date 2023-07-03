package nebulosa.api.services

import nebulosa.api.data.enums.DevicePropertyVectorType
import nebulosa.api.data.requests.DevicePropertyVectorRequest
import nebulosa.api.data.responses.DevicePropertyVectorResponse
import nebulosa.api.exceptions.DeviceNotFound
import org.springframework.stereotype.Service

@Service
class IndiService(
    private val equipmentManager: EquipmentManager,
) {

    fun properties(name: String): List<DevicePropertyVectorResponse> {
        val device = equipmentManager[name] ?: throw DeviceNotFound
        return device.properties.values.map(::DevicePropertyVectorResponse)
    }

    fun sendProperty(name: String, vector: DevicePropertyVectorRequest) {
        val device = equipmentManager[name] ?: throw DeviceNotFound

        when (vector.type) {
            DevicePropertyVectorType.NUMBER -> {
                val elements = vector.properties.map { it.name to "${it.value}".toDouble() }
                device.sendNewNumber(vector.name, elements)
            }
            DevicePropertyVectorType.SWITCH -> {
                val elements = vector.properties.map { it.name to "${it.value}".toBooleanStrict() }
                device.sendNewSwitch(vector.name, elements)
            }
            DevicePropertyVectorType.TEXT -> {
                val elements = vector.properties.map { it.name to "${it.value}" }
                device.sendNewText(vector.name, elements)
            }
        }
    }
}
