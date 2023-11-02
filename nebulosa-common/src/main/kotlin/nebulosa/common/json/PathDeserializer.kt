package nebulosa.common.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.nio.file.Path

data object PathDeserializer : StdDeserializer<Path>(Path::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Path? {
        return p.valueAsString?.let(Path::of)
    }
}
