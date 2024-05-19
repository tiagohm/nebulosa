package nebulosa.api.rotators

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.rotator.Rotator
import org.springframework.stereotype.Component

@Component
class RotatorSerializer : StdSerializer<Rotator>(Rotator::class.java) {

    override fun serialize(value: Rotator, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("sender", value.sender.id)
        gen.writeStringField("id", value.id)
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        // gen.writeBooleanField("moving", value.moving)
        gen.writeNumberField("angle", value.angle)
        gen.writeBooleanField("canAbort", value.canAbort)
        gen.writeBooleanField("canReverse", value.canReverse)
        gen.writeBooleanField("canHome", value.canHome)
        gen.writeBooleanField("canSync", value.canSync)
        gen.writeBooleanField("hasBacklashCompensation", value.hasBacklashCompensation)
        gen.writeNumberField("maxAngle", value.maxAngle)
        gen.writeNumberField("minAngle", value.minAngle)
        gen.writeEndObject()
    }
}
