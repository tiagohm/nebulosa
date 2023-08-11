package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.api.data.enums.INDISendPropertyType
import nebulosa.indi.device.Property
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.SwitchPropertyVector
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("serializer")
class PropertyVectorSerializer : StdSerializer<PropertyVector<*, *>>(PropertyVector::class.java) {

    override fun serialize(
        vector: PropertyVector<*, *>,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField("device", vector.device.name)
        gen.writeStringField("name", vector.name)
        gen.writeStringField("label", vector.label)
        gen.writeStringField("type", INDISendPropertyType.of(vector).name)
        gen.writeStringField("group", vector.group)
        gen.writeStringField("perm", vector.perm.name)
        gen.writeStringField("state", vector.state.name)
        gen.writeStringField("rule", (vector as? SwitchPropertyVector)?.rule?.name)
        gen.writeArrayFieldStart("items")
        vector.values.forEach { gen.writeProperty(it) }
        gen.writeEndArray()
        gen.writeEndObject()
    }

    companion object {

        @JvmStatic
        private fun JsonGenerator.writeProperty(property: Property<*>) {
            writeStartObject()
            writeStringField("name", property.name)
            writeStringField("label", property.label)
            writeObjectField("value", property.value)
            writeEndObject()
        }
    }
}
