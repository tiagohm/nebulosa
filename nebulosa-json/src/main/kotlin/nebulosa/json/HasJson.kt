package nebulosa.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider

interface HasJson {

    fun writeToJson(gen: JsonGenerator, provider: SerializerProvider)
}
