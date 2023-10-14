package nebulosa.api.beans.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.SwitchPropertyVector
import nebulosa.json.ToJson
import org.springframework.stereotype.Component

@Component
class INDIPropertyVectorConverter : ToJson<PropertyVector<*, *>> {

    override val type = PropertyVector::class.java

    override fun serialize(value: PropertyVector<*, *>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("device", value.device.name)
        gen.writeStringField("name", value.name)
        gen.writeStringField("label", value.label)
        gen.writeStringField("group", value.group)
        gen.writeStringField("perm", value.perm.name)
        gen.writeStringField("state", value.state.name)
        gen.writeObjectField("items", value.values)
        gen.writeStringField("type", value.type.name)

        if (value is SwitchPropertyVector) {
            gen.writeStringField("rule", value.rule.name)
        }

        gen.writeEndObject()
    }
}
