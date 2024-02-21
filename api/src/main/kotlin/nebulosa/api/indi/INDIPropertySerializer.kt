package nebulosa.api.indi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.Property
import org.springframework.stereotype.Component

@Component
class INDIPropertySerializer : StdSerializer<Property<*>>(Property::class.java) {

    override fun serialize(value: Property<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", value.name)
        gen.writeStringField("label", value.label)
        gen.writeObjectField("value", value.value)
        gen.writeEndObject()
    }
}
