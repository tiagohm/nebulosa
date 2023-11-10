package nebulosa.common.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.nio.file.Path

data object PathSerializer : StdSerializer<Path>(Path::class.java) {

    private fun readResolve(): Any = PathSerializer

    override fun serialize(value: Path?, gen: JsonGenerator, provider: SerializerProvider) {
        value?.also { gen.writeString("$it") } ?: gen.writeNull()
    }
}
