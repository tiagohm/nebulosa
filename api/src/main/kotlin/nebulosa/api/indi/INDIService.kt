package nebulosa.api.indi

import nebulosa.api.cameras.CameraService
import nebulosa.api.data.enums.INDISendPropertyType
import nebulosa.api.data.requests.INDISendPropertyRequest
import nebulosa.api.focusers.FocuserService
import nebulosa.api.guiding.GuidingService
import nebulosa.api.mounts.MountService
import nebulosa.api.wheels.WheelService
import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector
import org.springframework.stereotype.Service
import java.util.*

@Service("indiService")
class INDIService(
    private val cameraService: CameraService,
    private val mountService: MountService,
    private val focuserService: FocuserService,
    private val wheelService: WheelService,
    private val guidingService: GuidingService,
) : LinkedList<String>() {

    fun canSendEvents(): Boolean {
        return true
    }

    operator fun get(name: String): Device? {
        return cameraService[name] ?: mountService[name] ?: focuserService[name]
        ?: wheelService[name] ?: guidingService[name]
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
