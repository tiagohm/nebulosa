package nebulosa.api.services

import nebulosa.api.data.enums.INDIPropertyType
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.data.responses.INDIPropertyResponse
import org.springframework.stereotype.Service

@Service("indiService")
class INDIService(
    private val equipmentService: EquipmentService,
) {

    fun properties(name: String): List<INDIPropertyResponse> {
        val device = equipmentService[name]!!
        return device.properties.values.map(::INDIPropertyResponse)
    }

    fun sendProperty(name: String, vector: INDISendPropertyRequest) {
        val device = equipmentService[name]!!

        when (vector.type) {
            INDIPropertyType.NUMBER -> {
                val elements = vector.properties.map { it.name to "${it.value}".toDouble() }
                device.sendNewNumber(vector.name, elements)
            }
            INDIPropertyType.SWITCH -> {
                val elements = vector.properties.map { it.name to "${it.value}".toBooleanStrict() }
                device.sendNewSwitch(vector.name, elements)
            }
            INDIPropertyType.TEXT -> {
                val elements = vector.properties.map { it.name to "${it.value}" }
                device.sendNewText(vector.name, elements)
            }
        }
    }
}
