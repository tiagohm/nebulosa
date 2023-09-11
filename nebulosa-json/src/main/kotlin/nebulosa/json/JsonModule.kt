package nebulosa.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer

data class JsonModule(
    private val serializers: Iterable<ToJson<*>>,
    private val deserializers: Iterable<FromJson<*>>,
) : SimpleModule() {

    init {
        for (serializer in serializers) {
            addToJson(serializer)
        }

        for (deserializer in deserializers) {
            addFromJson(deserializer)
        }
    }

    fun <T> addToJson(toJson: ToJson<T>) {
        addSerializer(toJson.type, ToJsonSerializer(toJson))
    }

    fun <T> addFromJson(fromJson: FromJson<T>) {
        addDeserializer(fromJson.type, FromJsonDeserializer(fromJson))
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
