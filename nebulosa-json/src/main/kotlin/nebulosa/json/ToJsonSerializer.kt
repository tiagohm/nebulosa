package nebulosa.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

data class ToJsonSerializer<T>(private val serializer: ToJson<T>) : StdSerializer<T>(serializer.type) {

    override fun serialize(value: T?, gen: JsonGenerator, provider: SerializerProvider) {
        if (value != null) serializer.serialize(value, gen, provider)
        else gen.writeNull()
    }
}
