package nebulosa.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.nio.file.Path

class PathSerializer : StdSerializer<Path>(Path::class.java) {

    override fun serialize(value: Path?, gen: JsonGenerator, provider: SerializerProvider) {
        value?.also { gen.writeString("$it") } ?: gen.writeNull()
    }
}
