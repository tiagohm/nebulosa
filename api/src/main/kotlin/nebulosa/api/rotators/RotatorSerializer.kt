package nebulosa.api.rotators

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.rotator.Rotator
import org.springframework.stereotype.Component

@Component
class RotatorSerializer : DeviceSerializer<Rotator>(Rotator::class.java) {

    override fun JsonGenerator.serialize(value: Rotator) {
        writeBooleanField("moving", value.moving)
        writeNumberField("angle", value.angle)
        writeBooleanField("canAbort", value.canAbort)
        writeBooleanField("canReverse", value.canReverse)
        writeBooleanField("reversed", value.reversed)
        writeBooleanField("canHome", value.canHome)
        writeBooleanField("canSync", value.canSync)
        writeBooleanField("hasBacklashCompensation", value.hasBacklashCompensation)
        writeNumberField("maxAngle", value.maxAngle)
        writeNumberField("minAngle", value.minAngle)
    }
}
