package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.focuser.Focuser
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("serializer")
class FocuserSerializer : StdSerializer<Focuser>(Focuser::class.java) {

    override fun serialize(
        focuser: Focuser,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField("name", focuser.name)
        gen.writeBooleanField("connected", focuser.connected)
        gen.writeBooleanField("moving", focuser.moving)
        gen.writeNumberField("position", focuser.position)
        gen.writeBooleanField("canAbsoluteMove", focuser.canAbsoluteMove)
        gen.writeBooleanField("canRelativeMove", focuser.canRelativeMove)
        gen.writeBooleanField("canAbort", focuser.canAbort)
        gen.writeBooleanField("canReverse", focuser.canReverse)
        gen.writeBooleanField("reverse", focuser.reverse)
        gen.writeBooleanField("canSync", focuser.canSync)
        gen.writeBooleanField("hasBacklash", focuser.hasBacklash)
        gen.writeNumberField("maxPosition", focuser.maxPosition)
        gen.writeBooleanField("hasThermometer", focuser.hasThermometer)
        gen.writeNumberField("temperature", focuser.temperature)
        gen.writeEndObject()
    }
}
