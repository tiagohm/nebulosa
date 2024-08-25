package nebulosa.api.dustcap

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.dustcap.DustCap
import org.springframework.stereotype.Component

@Component
class DustCapSerializer : StdSerializer<DustCap>(DustCap::class.java) {

    override fun serialize(value: DustCap, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("type", value.type.name)
        gen.writeStringField("sender", value.sender.id)
        gen.writeStringField("id", value.id)
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.writeBooleanField("canPark", value.canPark)
        gen.writeBooleanField("parking", value.parking)
        gen.writeBooleanField("parked", value.parked)
        gen.writeEndObject()
    }
}
