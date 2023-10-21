package nebulosa.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

data class FromJsonDeserializer<T>(private val deserializer: FromJson<T>) : StdDeserializer<T>(deserializer.type) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        return deserializer.deserialize(p, ctxt)
    }
}
