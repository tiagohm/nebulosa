package nebulosa.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext

interface FromJson<T> {

    val type: Class<T>

    fun deserialize(p: JsonParser, ctxt: DeserializationContext): T?
}
