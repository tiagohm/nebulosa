package nebulosa.json.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.json.FromJson
import nebulosa.json.ToJson
import java.nio.file.Path

object PathConverter : FromJson<Path>, ToJson<Path> {

    override val type = Path::class.java

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Path? {
        return p.valueAsString?.let(Path::of)
    }

    override fun serialize(value: Path, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString("$value")
    }
}
