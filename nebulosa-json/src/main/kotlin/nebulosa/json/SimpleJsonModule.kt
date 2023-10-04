package nebulosa.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class SimpleJsonModule : SimpleModule() {

    fun <T> addSerializer(serializer: ToJson<T>) = apply {
        addSerializer(serializer.type, ToJsonSerializer(serializer))
    }

    fun <T> addDeserializer(deserializer: FromJson<T>) = apply {
        addDeserializer(deserializer.type, FromJsonDeserializer(deserializer))
    }

    fun <T> addConverter(converter: T) where T : FromJson<*>, T : ToJson<*> = apply {
        addSerializer(converter)
        addDeserializer(converter)
    }

    private data class ToJsonSerializer<T>(private val serializer: ToJson<T>) : StdSerializer<T>(serializer.type) {

        override fun serialize(value: T?, gen: JsonGenerator, provider: SerializerProvider) {
            if (value != null) serializer.serialize(value, gen, provider)
            else gen.writeNull()
        }
    }

    private data class FromJsonDeserializer<T>(private val deserializer: FromJson<T>) : StdDeserializer<T>(deserializer.type) {

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
            return deserializer.deserialize(p, ctxt)
        }
    }
}
