package nebulosa.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object HasJsonSerializer : StdSerializer<HasJson>(HasJson::class.java) {

    private fun readResolve(): Any = HasJsonSerializer

    override fun serialize(value: HasJson?, gen: JsonGenerator, provider: SerializerProvider) {
        value?.writeToJson(gen, provider) ?: gen.writeNull()
    }
}
