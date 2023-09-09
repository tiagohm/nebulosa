package nebulosa.indi.device

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.json.HasJson

sealed interface Property<T> : HasJson {

    val name: String

    val label: String

    val value: T

    override fun writeToJson(gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", name)
        gen.writeStringField("label", label)
        gen.writeObjectField("value", value)
        gen.writeEndObject()
    }
}
