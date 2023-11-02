package nebulosa.api.focusers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.focuser.Focuser
import org.springframework.stereotype.Component

@Component
class FocuserSerializer : StdSerializer<Focuser>(Focuser::class.java) {

    override fun serialize(value: Focuser, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.writeBooleanField("moving", value.moving)
        gen.writeNumberField("position", value.position)
        gen.writeBooleanField("canAbsoluteMove", value.canAbsoluteMove)
        gen.writeBooleanField("canRelativeMove", value.canRelativeMove)
        gen.writeBooleanField("canAbort", value.canAbort)
        gen.writeBooleanField("canReverse", value.canReverse)
        gen.writeBooleanField("reverse", value.reverse)
        gen.writeBooleanField("canSync", value.canSync)
        gen.writeBooleanField("hasBacklash", value.hasBacklash)
        gen.writeNumberField("maxPosition", value.maxPosition)
        gen.writeBooleanField("hasThermometer", value.hasThermometer)
        gen.writeNumberField("temperature", value.temperature)
        gen.writeEndObject()
    }
}
