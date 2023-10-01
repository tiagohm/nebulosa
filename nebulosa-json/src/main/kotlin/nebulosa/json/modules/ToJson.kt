package nebulosa.json.modules

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider

interface ToJson<T> {

    val type: Class<T>

    fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider)
}
