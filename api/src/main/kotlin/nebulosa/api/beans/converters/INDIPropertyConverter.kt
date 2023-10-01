package nebulosa.api.beans.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.indi.device.Property
import nebulosa.json.modules.ToJson
import org.springframework.stereotype.Component

@Component
class INDIPropertyConverter : ToJson<Property<*>> {

    override val type = Property::class.java

    override fun serialize(value: Property<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", value.name)
        gen.writeStringField("label", value.label)
        gen.writeObjectField("value", value.value)
        gen.writeEndObject()
    }
}
