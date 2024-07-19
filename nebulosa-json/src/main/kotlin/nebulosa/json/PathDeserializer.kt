package nebulosa.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.nio.file.Path

class PathDeserializer : StdDeserializer<Path>(Path::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Path? {
        return p.valueAsString?.let(Path::of)
    }
}
