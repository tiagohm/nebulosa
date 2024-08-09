package nebulosa.api.wheels

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.stereotype.Component

@Component
class WheelSerializer : StdSerializer<FilterWheel>(FilterWheel::class.java) {

    override fun serialize(value: FilterWheel, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("type", value.type.name)
        gen.writeStringField("sender", value.sender.id)
        gen.writeStringField("id", value.id)
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.writeNumberField("count", value.count)
        gen.writeNumberField("position", value.position)
        gen.writeBooleanField("moving", value.moving)
        gen.writeArrayFieldStart("names")
        value.names.forEach(gen::writeString)
        gen.writeEndArray()
        gen.writeEndObject()
    }
}
